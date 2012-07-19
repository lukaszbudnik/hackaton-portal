package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import jsmessages.api.JsMessages
import play.api.Play.current
import org.apache.commons.codec.digest.DigestUtils
import play.api.mvc.Results

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

  def jsMessages = Action { implicit request =>

    val content = JsMessages(Some("Messages"))
    val hash = DigestUtils.md5Hex(content)

    if (hash.compareTo(request.headers.get(IF_NONE_MATCH).getOrElse("")) == 0) {
      NotModified
    } else {
      Ok(content).as(JAVASCRIPT).withHeaders(ETAG -> hash)
    }
  }

}