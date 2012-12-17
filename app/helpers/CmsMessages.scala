package helpers
import play.api.i18n.Lang
import play.api.Application
import cms.ContentManager
import cms.dto.Entry
import cms.dto.Content
import play.api.Play
import play.api.Mode

object CmsMessages {

  def apply(key: String, args: Object*)(implicit lang: Lang) = {

    Play.current.mode match {
      case Mode.Test => key
      case _ => {
        if (args.size > 0) {
          String.format(getMessage(key), args.map{a => a}:_*)
        } else {
          getMessage(key)
        }
      }
    }

  }

  def getMessage(key: String)(implicit lang: Lang) = {

    val opEntry: Option[Entry] = ContentManager.find(key)

    opEntry match {
      case Some(e: Entry) => {
        e.contents.find(_.lang == lang.language).orElse(e.contents.find(_.lang == ContentManager.defaultLanguage)) match {
          case Some(c: Content) => c.value
          case _ => key
        }
      }
      case _ => key
    }
  }

}