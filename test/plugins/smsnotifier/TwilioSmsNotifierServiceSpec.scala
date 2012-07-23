package plugins.smsnotifier

import org.specs2.mutable.Specification

import play.api.Play.current
import play.api.test.Helpers.inMemoryDatabase
import play.api.test.Helpers.running
import play.api.test.FakeApplication
import plugins.use

class TwilioSmsNotifierServiceSpec extends Specification {

  "TwilioSmsNotifierService" should {
    "send sms text message" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
    	  val subject = "test"
          val to = "+48604813912"
          val from = "+14155992671"
          val text = "Hello, testing text message. Best regards"
          val smsNotifierService = use[SmsNotifierPlugin].smsNotifierService
          val id = smsNotifierService.send(from, to, text)
          id must haveClass[SmsNotifierSuccessResponse]
      }
    }
  }

}