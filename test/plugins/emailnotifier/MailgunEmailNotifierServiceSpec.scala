package plugins.emailnotifier

import org.specs2.mutable._

import play.api.Play.current
import play.api.test.Helpers.inMemoryDatabase
import play.api.test.Helpers.running
import play.api.test.FakeApplication
import plugins.use

class MailgunEmailNotifierServiceSpec extends Specification {

  "MailgunEmailNotifierService" should {
    "send text message" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
    	  val subject = "test ęąść"
          val to = "lukasz.budnik@gmail.com"
          val from = "Lukasz Budnik <l.budnik@kainos.com>"
          val textMessage = "Hello, testing text message with UTF-8 characters like Polish: ęąść.\nBest regards"
          val emailNotifierService = use[EmailNotifierPlugin].emailNotifierService
          val id = emailNotifierService.send(from, to, subject, textMessage)
          id must haveClass[EmailNotifierSuccessResponse]
      }
    }
    "send both text and html messages" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
    	  val subject = "test ęąść"
          val to = "lukasz.budnik@gmail.com"
          val from = "Lukasz Budnik <l.budnik@kainos.com>"
          val textMessage = "Hello, testing text message with UTF-8 characters like Polish: ęąść\nBest regards"
          val htmlMessage = Some("<html><body><h1>Hello ęąść!</h1>and best regards :)</body></html>")
          val emailNotifierService = use[EmailNotifierPlugin].emailNotifierService
          val id = emailNotifierService.send(from, to, subject, textMessage, htmlMessage)
          id must haveClass[EmailNotifierSuccessResponse]
      }
    }
  }
}
