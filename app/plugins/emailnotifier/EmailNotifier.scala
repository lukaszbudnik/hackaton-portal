package plugins.emailnotifier

abstract class EmailNotifierResponse

case class EmailNotifierErrorResponse(val message: String) extends EmailNotifierResponse

case class EmailNotifierSuccessResponse(val messageId: String) extends EmailNotifierResponse

trait EmailNotifierService {
  def send(from: String, to: String, subject: String, textMessage: String, htmlMessage: Option[String] = None): EmailNotifierResponse
}

trait EmailNotifierPlugin extends play.api.Plugin {
  def emailNotifierService: EmailNotifierService
}

class MockEmailNotifierService extends EmailNotifierService {
  def send(from: String, to: String, subject: String, textMessage: String, htmlMessage: Option[String] = None): EmailNotifierResponse = {
    EmailNotifierSuccessResponse("1")
  }
}
