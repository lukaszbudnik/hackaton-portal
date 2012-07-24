package plugins.smsnotifier

import play.api.Play
import play.api.Play.current
import play.api.Logger
import play.api.Mode
import play.api.Application
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.HttpResponse
import play.api.libs.json.Json
import org.apache.commons.io.IOUtils
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.auth.AuthState
import org.apache.http.client.protocol.ClientContext
import org.apache.http.client.CredentialsProvider
import org.apache.http.HttpException
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScheme
import org.apache.http.protocol.ExecutionContext
import org.apache.http.auth.Credentials
import org.apache.http.protocol.SyncBasicHttpContext
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.params.HttpParams
import org.apache.http.params.BasicHttpParams
import plugins.utils.HttpUtils
import org.apache.http.client.entity.UrlEncodedFormEntity
import java.util.ArrayList
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair

class TwilioSmsNotifierPlugin(app: Application) extends SmsNotifierPlugin {

  lazy val smsNotifierServiceInstance: SmsNotifierService = {
    val applicationSid = app.configuration.getString("twilio.applicationSid").getOrElse {
      throw new RuntimeException("TWILIO_APPLICATION_SID not set")
    }
    lazy val authToken = app.configuration.getString("twilio.authToken").getOrElse {
      throw new RuntimeException("TWILIO_AUTH_TOKEN not set")
    }
    
    applicationSid match {
      case "mock" => new MockSmsNotifierService()
      case _ => {

		Logger.debug(String.format("TwilioSmsNotifierService about to be created: application sid: %s", applicationSid))
    
		new TwilioSmsNotifierService(applicationSid, authToken)
      }
    }
  }

  def smsNotifierService = smsNotifierServiceInstance
  
}

class TwilioSmsNotifierService(applicationSid: String, authToken: String) extends SmsNotifierService {

  private val SEND_URL_PATTERN = "https://api.twilio.com/2010-04-01/Accounts/%s/SMS/Messages.json"
  
  val sendUrl = SEND_URL_PATTERN.format(applicationSid)
  
  def send(from: String, to: String, text: String) = {
    val httpClient = new DefaultHttpClient

    val execContext = HttpUtils.createPreemptiveAuthHttpContext(httpClient, new UsernamePasswordCredentials(applicationSid, authToken))

    val post = new HttpPost(sendUrl)
    
    val nameValuePairs = new ArrayList[NameValuePair](3)
	nameValuePairs.add(new BasicNameValuePair("From", from))  
	nameValuePairs.add(new BasicNameValuePair("To", to))
	nameValuePairs.add(new BasicNameValuePair("Body", text))
	post.setEntity(new UrlEncodedFormEntity(nameValuePairs))

    val r: HttpResponse = httpClient.execute(post, execContext)
    val responseBody = IOUtils.toString(r.getEntity().getContent())
    
    Logger.debug("Got Twilio Response: " + responseBody)
    
    val jsonResponse = Json.parse(responseBody)

    (jsonResponse \ "sid").asOpt[String] match {
      case None => SmsNotifierErrorResponse((jsonResponse \ "message").as[String])
      case Some(sid) => SmsNotifierSuccessResponse(sid)
    }
  }
  
}

