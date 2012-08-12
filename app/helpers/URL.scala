package helpers
import play.api.mvc.Call
import play.api.Play

object URL {
  
  private val applicationUrl = Play.current.configuration.getString("application.url").get
  
  def externalUrl(call: Call) = {
    applicationUrl + call.url
  }

}