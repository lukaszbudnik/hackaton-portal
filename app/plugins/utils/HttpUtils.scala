package plugins.utils

import org.apache.http.auth.AuthState
import org.apache.http.client.protocol.ClientContext
import org.apache.http.client.CredentialsProvider
import org.apache.http.HttpException
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScheme
import org.apache.http.protocol.ExecutionContext
import org.apache.http.auth.Credentials
import org.apache.http.auth.AuthScope
import org.apache.http.protocol.SyncBasicHttpContext
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.auth.UsernamePasswordCredentials

object HttpUtils {

  def createPreemptiveAuthHttpContext(httpClient: DefaultHttpClient, usernamePasswordCredentials: UsernamePasswordCredentials) = {
    httpClient.getCredentialsProvider().setCredentials(
      AuthScope.ANY,
      usernamePasswordCredentials);

    val execContext = new SyncBasicHttpContext(new BasicHttpContext());
    val basicAuth = new BasicScheme()
    execContext.setAttribute("preemptive-auth", basicAuth)

    httpClient.addRequestInterceptor(new PreemptiveAuth(), 0)
    
    execContext
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