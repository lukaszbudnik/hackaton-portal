package plugins.smsnotifier

abstract class SmsNotifierResponse

case class SmsNotifierErrorResponse(val message: String) extends SmsNotifierResponse

case class SmsNotifierSuccessResponse(val messageId: String) extends SmsNotifierResponse

trait SmsNotifierService {
  def send(from: String, to: String, text: String): SmsNotifierResponse
}

trait SmsNotifierPlugin extends play.api.Plugin {
  def smsNotifierService: SmsNotifierService
}

class MockSmsNotifierService extends SmsNotifierService {
  def send(from: String, to: String, text: String): SmsNotifierResponse = {
    SmsNotifierSuccessResponse("1")
  }
}