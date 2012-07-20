package controllers

import jsmessages.api.JsMessages
import play.api.Play.current
import play.api.mvc.Action
import play.api.mvc.Controller
import org.apache.commons.codec.digest.DigestUtils

object JsMessagesFeed extends Controller  {


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