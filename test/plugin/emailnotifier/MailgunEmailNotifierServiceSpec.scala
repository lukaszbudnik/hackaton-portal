package plugin.emailnotifier

import org.specs2.mutable.Specification

import play.api.Play.current
import play.api.test.Helpers.inMemoryDatabase
import play.api.test.Helpers.running
import play.api.test.FakeApplication
import plugin.emailnotifier.EmailNotifierPlugin
import plugin.use

class MailgunEmailNotifierServiceSpec extends Specification {

  "MailgunEmailNotifierService" should {
    "send text message" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
    	  val subject = "test"
          val to = "lukasz.budnik@gmail.com"
          val from = "Lukasz Budnik <l.budnik@kainos.com>"
          val textMessage = "Hello, testing text message.\nBest regards"
          val emailNotifierService = use[EmailNotifierPlugin].emailNotifierService
          val id = emailNotifierService.send(from, to, subject, textMessage)
          id must equalTo(id)
      }
    }
    "send both text and html messages" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
    	  val subject = "test"
          val to = "lukasz.budnik@gmail.com"
          val from = "Lukasz Budnik <l.budnik@kainos.com>"
          val textMessage = "Hello, testing text message.\nBest regards"
          val htmlMessage = Some("<html><body><h1>Hello!</h1>and best regards :)</body></html>")
          val emailNotifierService = use[EmailNotifierPlugin].emailNotifierService
          val id = emailNotifierService.send(from, to, subject, textMessage, htmlMessage)
          id must equalTo(id)
      }
    }
  }

}