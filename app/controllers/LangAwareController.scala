package controllers

import scala.annotation.implicitNotFound

import play.api.i18n.Lang
import play.api.mvc.RequestHeader

import org.squeryl.PrimitiveTypeMode.inTransaction

import securesocial.core.SecureSocial

class LangAwareController extends BaseController {

  implicit override def lang(implicit request: RequestHeader): Lang = {
    val session = request.session

    val sessionLanguage = session.get(LangAwareController.SESSION_LANG_KEY)
    
    sessionLanguage.map { langString =>
      Lang(langString)
    }.getOrElse(super.lang)
    
  }

}

object LangAwareController {
  val SESSION_LANG_KEY = "lang"
  val SESSION_LANG_CHANGED_AFTER_LOGIN = "lang-changed-after-login"
  val DEFAULT_LANG = "en"
}
