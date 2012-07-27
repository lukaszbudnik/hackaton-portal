package controllers

import play.api.mvc.Controller
import play.api.cache.Cached
import play.api.Play.current
import play.api.mvc.Action
import play.api.Logger
import play.api.mvc.RequestHeader
import play.api.i18n.Lang
import core.LangAwareController

object Application extends LangAwareController with securesocial.core.SecureSocial {

  def index = UserAwareAction { implicit request =>
    this.lang(request)
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

  def changeLanguage(lang: String) = UserAwareAction { implicit request =>
    Redirect(request.headers.get("referer").getOrElse("/")).withSession(request.session + (SESSION_LANG_KEY -> lang))
  }

}