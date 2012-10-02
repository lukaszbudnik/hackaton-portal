package helpers

import play.api.mvc.Call
import play.api.Play
import play.api.Application
import play.api.Mode

object URL {

  private val applicationUrl = {
    Play.current.mode match {
      case Mode.Prod =>
        val baseUrl = Play.current.configuration.getString("application.url").get
        if (baseUrl.endsWith("/")) {
          baseUrl.substring(0, baseUrl.length() - 1)
        } else {
          baseUrl
        }
      case _ => "http://localhost:9000"
    }
  }

  def externalUrl(call: Call) = {
    applicationUrl + call.url
  }

}