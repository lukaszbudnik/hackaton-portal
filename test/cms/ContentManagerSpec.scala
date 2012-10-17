package cms

import com.novus.salat._
import com.novus.salat.global._
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import cms.dto.Content
import cms.dto.Entry
import cms.dto.EntryType
import play.api.Play

class ContentManagerSpec extends Specification {

  running(FakeApplication()) {
    val skipAll = Play.current.configuration.getString("mongodb.uri").get == "mock"
    args(skipAll = skipAll)
  }

  "ContentManager" should {

    "remove entries" in {
      running(FakeApplication()) {
        for (e <- ContentManager.all) {
          ContentManager.remove(e)
          // should be flushed from cache as well
          val old = ContentManager.find(e.key)
          old must beNone
        }

        ContentManager.all.size must equalTo(0)
      }
    }

    "add entries" in {
      running(FakeApplication()) {
        val e1 = Entry("about_title", EntryType.HTML, List(Content("pl", "O"), Content("en", "About")))
        val e2 = Entry("about_text", EntryType.HTML, List(Content("pl", "Tekst"), Content("en", "Text")))
        val e3 = Entry("global_cancel", EntryType.Message, List(Content("pl", "Anuluj"), Content("en", "Cancel")))

        ContentManager.create(e1)
        ContentManager.create(e2)
        ContentManager.create(e3)

        ContentManager.all.size must equalTo(3)
      }
    }

    "get all entries" in {
      running(FakeApplication()) {
        val contents = ContentManager.all
        contents.size must equalTo(3)
      }
    }

    "edit entry" in {
      running(FakeApplication()) {
        val entry = ContentManager.find("about_title")
        val newContent = entry.get.copy(contents = List(Content("pl-PL", "testing")))

        ContentManager.update(newContent)

        val updated = ContentManager.find("about_title")
        updated must beSome.which {
          _ match {
            case e: Entry if e.contents.forall(x => x.lang == "pl-PL") => true
            case _ => false
          }
        }
      }
    }

    "find entry by key" in {
      running(FakeApplication()) {
        val key = "about_text"
        val entry = ContentManager.find(key)

        entry must beSome.which {
          _ match {
            case e: Entry if e.key == key && e.contents.map(_.lang) == List("pl", "en") => true
            case _ => false
          }
        }

        val entry2 = ContentManager.find(key)

        entry2 must beSome.which {
          _ match {
            case e: Entry if e.key == key && e.contents.map(_.lang) == List("pl", "en") => true
            case _ => false
          }
        }
      }
    }

    "filter by event type" in {
      running(FakeApplication()) {
        val key = "about_text"
        val messages = ContentManager.filtered(EntryType.Message)

        messages.size must equalTo(1)

        val html = ContentManager.filtered(EntryType.HTML)

        html.size must equalTo(2)
      }
    }

  }

}