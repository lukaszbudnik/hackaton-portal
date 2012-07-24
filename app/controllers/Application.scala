package controllers

import play.api.mvc.Controller
import play.api.cache.Cached
import play.api.Play.current
import play.api.mvc.Action
import play.api.Logger

object Application extends Controller with securesocial.core.SecureSocial {

  def index = Cached("application.index") {
    UserAwareAction { implicit request =>
      Ok(views.html.index(request.user))
    }
  }

  def about = Cached("application.about") {
    UserAwareAction { implicit request =>
      Ok(views.html.about(request.user))
    }
  }

  def contact = Cached("application.contact") {
    UserAwareAction { implicit request =>
      Ok(views.html.contact(request.user))
    }
  }

  def profile = SecuredAction() { implicit request =>
    Ok(views.html.profile(request.user))
  }

}