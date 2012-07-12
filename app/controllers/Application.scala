package controllers

import play.api.mvc.Controller

object Application extends Controller with securesocial.core.SecureSocial {

  def index = UserAwareAction { implicit request =>
    Ok(views.html.index(request.user))
  }

  def about = UserAwareAction { implicit request =>
    Ok(views.html.about(request.user))
  }

  def contact = UserAwareAction { implicit request =>
    Ok(views.html.contact(request.user))
  }

  def profile = SecuredAction() { implicit request =>
    Ok(views.html.profile(request.user))
  }

}