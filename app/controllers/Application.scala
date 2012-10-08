package controllers

import play.api.mvc.Controller
import play.api.cache.Cached
import play.api.Play.current
import play.api.mvc.Action
import play.api.Logger
import play.api.mvc.RequestHeader
import play.api.i18n.Lang
import core.LangAwareController
import play.api.cache.Cache

object Application extends LangAwareController with securesocial.core.SecureSocial {

  val key = "key"
  val value = "this is a text"
  // in seconds
  val expiration = 100

  def index = UserAwareAction { implicit request =>
    Cache.set(key, value, expiration)
    val valueOption = Cache.getAs[String](key)
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
    Redirect(request.headers.get(REFERER).getOrElse("/")).withSession(request.session + (SESSION_LANG_KEY -> lang))
  }

}