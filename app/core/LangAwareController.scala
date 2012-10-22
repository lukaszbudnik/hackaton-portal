package core

import play.api.mvc.Controller
import play.api.mvc.RequestHeader
import play.api.i18n.Lang

class LangAwareController extends Controller {

  implicit override def lang(implicit request: RequestHeader) = {
    request.session.get(LangAwareController.SESSION_LANG_KEY).map { langString =>
      Lang(langString)
    }.getOrElse(super.lang)
  }

}

object LangAwareController {
  val SESSION_LANG_KEY = "lang"
  val DEFAULT_LANG = "en"
}