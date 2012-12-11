import org.squeryl.adapters.H2Adapter
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.internals.DatabaseAdapter
import org.squeryl.Session
import org.squeryl.SessionFactory
import core.SecurityAbuseException
import play.api.db._
import play.api.mvc.Results.Forbidden
import play.api.mvc.Results.InternalServerError
import play.api.mvc.Results.NotFound
import play.api.mvc.Results.BadRequest
import play.api.mvc.Handler
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.Application
import play.api.GlobalSettings
import play.api.Mode
import play.api.Play
import play.api.PlayException
import play.api.UnexpectedException
import play.api.Logger
import scala.io.Source
import scala.io.Codec
import play.api.libs.Codecs._
import java.sql.Date

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    SessionFactory.concreteFactory = app.configuration.getString("db.default.driver") match {
      case Some("org.h2.Driver") => Some(() => getSession(new H2Adapter, app))
      case Some("org.postgresql.Driver") => Some(() => getSession(new PostgreSqlAdapter, app))
      case _ => sys.error("Database driver must be either org.h2.Driver or org.postgresql.Driver")
    }
  }

  override def onError(request: RequestHeader, ex: Throwable): Result = {
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
    } else {
      super.onRouteRequest(request)
    }
  }

  private def getSession(adapter: DatabaseAdapter, app: Application) = {
    val connection = DB.getConnection()(app)
    val session = Session.create(connection, adapter)

    if (!play.Play.isProd) {
      session.setLogger(msg => Logger.debug(msg))

      applyTestEvolutions(connection, app)
    }

    session
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

}
