package core

import play.api.mvc.Controller
import play.api.mvc.RequestHeader
import play.api.i18n.Lang

class LangAwareController extends Controller {
  
  val SESSION_LANG_KEY = "lang"

  implicit override def lang(implicit request: RequestHeader) = {
    request.session.get(SESSION_LANG_KEY).map { langString =>
      Lang(langString)
    }.getOrElse(super.lang)
  }

}