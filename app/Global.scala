import org.squeryl.adapters.H2Adapter
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.internals.DatabaseAdapter
import org.squeryl.Session
import org.squeryl.SessionFactory
import core.SecurityAbuseException
import play.api.db.DB
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
    if (play.Play.isProd() &&
      (request.path.contains("hackathons") ||
        request.path.contains("sponsors") ||
        request.path.contains("locations"))) {
      return None
    }
    super.onRouteRequest(request)
  }
  
  private def getSession(adapter: DatabaseAdapter, app: Application) = {
    val session = Session.create(DB.getConnection()(app), adapter)
    if (!play.Play.isProd) {
    	session.setLogger(msg => Logger.debug(msg))
    }
    session
  }

}