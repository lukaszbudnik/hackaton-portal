import java.io.PrintWriter
import java.io.StringWriter
import java.sql.Date

import scala.annotation.implicitNotFound
import scala.io.Codec.charset2codec
import scala.io.Codec
import scala.io.Source

import play.api.db.DB
import play.api.libs.Codecs.sha1
import play.api.mvc.Results.BadRequest
import play.api.mvc.Results.Forbidden
import play.api.mvc.Results.InternalServerError
import play.api.mvc.Results.NotFound
import play.api.mvc.Action
import play.api.mvc.Handler
import play.api.mvc.PlainResult
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import play.api.Mode
import play.api.Play
import play.api.PlayException
import play.api.UnexpectedException

import org.squeryl.PrimitiveTypeMode.inTransaction
import org.squeryl.adapters.H2Adapter
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.internals.DatabaseAdapter
import org.squeryl.Session
import org.squeryl.SessionFactory

import controllers.LangAwareController
import helpers.EmailSender
import securesocial.core.SecureSocial
import security.SecurityAbuseException

object Global extends GlobalSettings {

  override def onStart(app: Application) {

    val sessionFactory = app.configuration.getString("db.default.driver") match {
      case Some("org.h2.Driver") => Some(() => getSessionFactory(new H2Adapter, app))
      case Some("org.postgresql.Driver") => Some(() => getSessionFactory(new PostgreSqlAdapter, app))
      case _ => sys.error("Database driver must be either org.h2.Driver or org.postgresql.Driver")
    }

    if (!play.Play.isProd) {
      sessionFactory map { sessionFactory =>
        val session = sessionFactory()
        session.setLogger(msg => Logger.debug(msg))
        applyTestEvolutions(session.connection, app)
      }
    }

    SessionFactory.concreteFactory = sessionFactory
  }

  override def onError(request: RequestHeader, ex: Throwable): Result = {

    if (play.Play.isProd) {
      tryToNotifyAdministrators(ex)
    }

    ex.getCause() match {
      case e: SecurityAbuseException => Forbidden(views.html.errors.securityAbuse(e))
      case _ => InternalServerError(Play.maybeApplication.map {
        case app if app.mode == Mode.Dev => views.html.defaultpages.devError.f
        case app => views.html.errors.generalError.f
      }.getOrElse(views.html.defaultpages.devError.f) {
        ex match {
          case e: PlayException.UsefulException => e
          case e => UnexpectedException(unexpected = Some(e))
        }
      })
    }
  }

  override def onHandlerNotFound(request: RequestHeader): Result = {
    NotFound(
      Play.maybeApplication.map {
        case app if app.mode == Mode.Dev => views.html.defaultpages.devNotFound.f
        case app => views.html.errors.notFound.f
      }.getOrElse(views.html.errors.notFound.f)(request, Play.maybeApplication.flatMap(_.routes)))
  }

  override def onBadRequest(request: RequestHeader, error: String): Result = {
    BadRequest(
      Play.maybeApplication.map {
        case app if app.mode == Mode.Dev => views.html.defaultpages.badRequest.f
        case app => views.html.errors.badRequest.f
      }.getOrElse(views.html.errors.badRequest.f)(request, error))
  }

  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    if (play.Play.isProd() && request.path.contains("sponsors")) {
      None
    } else if (request.path.startsWith("/authenticate")) {
      super.onRouteRequest(request) map {
        case a: Action[_] => Action(a.parser) { request =>
          removeLangVarFromSession(request, a(request))
        }
        case h => h
      }
    } else {
      super.onRouteRequest(request) map {
        case a: Action[_] => Action(a.parser) { request =>
          addLangVarToSession(request, a(request))
        }
        case h => h
      }
    }
  }

  private def removeLangVarFromSession(request: RequestHeader, r: Result): Result = {
    r match {
      case sr: PlainResult => {
        inTransaction {
          val session = request.session
          val sessionLanguage = session.get(LangAwareController.SESSION_LANG_KEY)

          sessionLanguage.map { sessionLanguage =>
            sr.withSession(session - LangAwareController.SESSION_LANG_KEY)
          }.getOrElse(sr)

        }
      }
      //case asr @ AsyncResult(promise) => asr.copy(result = promise.map(addVarToSession))
      case any => any
    }
  }

  private def addLangVarToSession(request: RequestHeader, r: Result): Result = {
    r match {
      case sr: PlainResult => {
        inTransaction {
          val session = request.session
          val sessionLanguage = session.get(LangAwareController.SESSION_LANG_KEY)

          val userSetLanguage = for (
            authenticator <- SecureSocial.authenticatorFromRequest(request);
            user <- model.User.lookupByOpenId(authenticator.userId.id + authenticator.userId.providerId) if !user.language.trim().isEmpty
          ) yield user.language

          userSetLanguage.map { newLanguage =>
            sr.withSession(session + (LangAwareController.SESSION_LANG_KEY -> newLanguage))
          }.getOrElse(sr)

        }
      }
      //case asr @ AsyncResult(promise) => asr.copy(result = promise.map(addVarToSession))
      case any => any
    }
  }

  private def getSessionFactory(adapter: DatabaseAdapter, app: Application) = {
    val connection = DB.getConnection()(app)
    Session.create(connection, adapter)
  }

  private def applyTestEvolutions(connection: java.sql.Connection, app: Application) = {
    app.getExistingFile("conf/evolutions/test").filter(_.exists).map { testEvolutions =>

      if (!connection.createStatement().executeQuery("select id from play_evolutions where id = -1").next()) {

        val sqlCommands = for (
          sqlFile <- testEvolutions.listFiles().toSeq.filter(_.getName().endsWith(".sql")).sortWith(_.getName() < _.getName());
          sqlCommand <- Source.fromFile(sqlFile)(Codec.UTF8).getLines() if !sqlCommand.startsWith("#") && sqlCommand.trim().length > 0
        ) yield sqlCommand

        val script = sqlCommands.mkString("\n")

        connection.createStatement().executeUpdate(script)

        val ps = connection.prepareStatement("insert into play_evolutions values(?, ?, ?, ?, ?, ?, ?)")
        ps.setInt(1, -1)
        ps.setString(2, sha1(script))
        ps.setDate(3, new Date(System.currentTimeMillis()))
        ps.setString(4, script)
        ps.setString(5, "")
        ps.setString(6, "applied")
        ps.setString(7, "")
        ps.execute()
      }
    }

    connection.commit()
  }

  private def tryToNotifyAdministrators(ex: Throwable) = {
    try {
      inTransaction {
        Logger.info("Notifing administrators about " + ex.toString())
        val stringWriter = new StringWriter()
        val printWriter = new PrintWriter(stringWriter)
        ex.printStackTrace(printWriter)
        EmailSender.sendPlainEmailToUsers(model.User.admins, "Exception: " + ex.toString, stringWriter.toString)
      }
    } catch {
      case e => Logger.error("Not able to notify administrators about " + ex.toString() + " got new exception " + e.toString())
    }
  }

}
