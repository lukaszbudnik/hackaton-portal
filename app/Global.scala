import org.squeryl.adapters.{H2Adapter, PostgreSqlAdapter}
import org.squeryl.internals.DatabaseAdapter
import org.squeryl.{Session, SessionFactory}
import org.squeryl.PrimitiveTypeMode._
import play.api._
import play.api.db.DB
import play.api.mvc._
import play.api.mvc.Results._
import core.SecurityAbuseException

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
		      case app => views.html.defaultpages.error.f
		      }.getOrElse(views.html.defaultpages.devError.f) {
		    	  ex match {
		    	  case e: PlayException.UsefulException => e
		    	  case e => UnexpectedException(unexpected = Some(e))
		    	  }
		      })
    }
  }
  
  override def onHandlerNotFound(request: RequestHeader): Result = {
    
    NotFound(Play.maybeApplication.map {
      case app if app.mode == Mode.Dev => views.html.defaultpages.devNotFound.f
      case app => views.html.errors.notFound.f
    }.getOrElse(views.html.errors.notFound.f)(request, Play.maybeApplication.flatMap(_.routes)))
  }

  private def getSession(adapter: DatabaseAdapter, app: Application) = Session.create(DB.getConnection()(app), adapter)

}