package plugin.emailnotifier

import play.api.Play
import play.api.Play.current
import play.api.Logger
import play.api.Mode
import play.api.Application
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.content.ByteArrayBody
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

class MailgunEmailNotifierPlugin(app: Application) extends EmailNotifierPlugin {

  private val loginDomainParsingRegex = """(.*)[@](.*)""".r

  lazy val emailNotifierServiceInstance: EmailNotifierService = {
    val loginDomain = app.configuration.getString("mailgun.loginDomain").getOrElse {
      throw new RuntimeException("MAILGUN_SMTP_LOGIN not set")
    }
    lazy val apiKey = app.configuration.getString("mailgun.apiKey").getOrElse {
      throw new RuntimeException("MAILGUN_API_KEY not set")
    }
    
    loginDomain match {
      case "mock" => new MockEmailNotifierService()
      case _ => {
        val loginDomainParsingRegex(login, domain) = loginDomain

		Logger.debug(String.format("MailgunEmailNotifierService about to be created: api_key: %s, login: %s, domain: %s", apiKey, login, domain))
    
		new MailgunEmailNotifierService(apiKey, login, domain)
      }
    }
  }

  def emailNotifierService = emailNotifierServiceInstance
  
}

class MailgunEmailNotifierService(apiKey: String, login: String, domain: String) extends EmailNotifierService {

  private val SEND_URL_PATTERN = "https://api.mailgun.net/v2/%s/messages"
  
  val sendUrl = SEND_URL_PATTERN.format(domain)
  
  def send(from: String, to: String, subject: String, textMessage: String, htmlMessage: Option[String] = None) = {
    val httpClient = new DefaultHttpClient

    httpClient.getCredentialsProvider().setCredentials(
      AuthScope.ANY,
      new UsernamePasswordCredentials("api", apiKey));

    val execContext = new SyncBasicHttpContext(new BasicHttpContext());
    val basicAuth = new BasicScheme()
    execContext.setAttribute("preemptive-auth", basicAuth)

    httpClient.addRequestInterceptor(new PreemptiveAuth(), 0)

    val post = new HttpPost(sendUrl)
    val multiPartEntity: MultipartEntity = new MultipartEntity(HttpMultipartMode.STRICT)

    multiPartEntity.addPart("from", new StringBody(from))
    multiPartEntity.addPart("subject", new StringBody(subject))
    multiPartEntity.addPart("to", new StringBody(to))
    multiPartEntity.addPart("text", new StringBody(textMessage))
    htmlMessage.map { htmlMessage =>
      multiPartEntity.addPart("html", new StringBody(htmlMessage))
    }

    post.setEntity(multiPartEntity)

    val r: HttpResponse = httpClient.execute(post, execContext)
    val responseBody = IOUtils.toString(r.getEntity().getContent())
    Logger.debug("Got MailGun Response: " + responseBody)
    val jsonResponse = Json.parse(responseBody)

    (jsonResponse \ "id").asOpt[String] match {
      case None => EmailNotifierErrorResponse((jsonResponse \ "message").as[String])
      case Some(id) => EmailNotifierSuccessResponse(id)
    }
  }
  
}

class PreemptiveAuth extends org.apache.http.HttpRequestInterceptor {
  def process(request: org.apache.http.HttpRequest, context: org.apache.http.protocol.HttpContext): Unit = {
    val authState: AuthState = context.getAttribute(
      ClientContext.TARGET_AUTH_STATE).asInstanceOf[AuthState]
    // If no auth scheme avaialble yet, try to initialize it preemptively  
    if (authState.getAuthScheme() == null) {
      val authScheme: AuthScheme = context.getAttribute(
        "preemptive-auth").asInstanceOf[AuthScheme]
      val credsProvider: CredentialsProvider = context.getAttribute(
        ClientContext.CREDS_PROVIDER).asInstanceOf[CredentialsProvider]
      val targetHost: HttpHost = context.getAttribute(
        ExecutionContext.HTTP_TARGET_HOST).asInstanceOf[HttpHost]
      if (authScheme != null) {
        val creds: Credentials = credsProvider.getCredentials(
          new AuthScope(
            targetHost.getHostName(),
            targetHost.getPort())).asInstanceOf[Credentials]
        if (creds == null) {
          throw new HttpException("No credentials for preemptive authentication");
        }
        authState.setAuthScheme(authScheme);
        authState.setCredentials(creds);
      }
    }
  }
}  
