package helpers

import core.SecurityAbuseException
import play.api.mvc._

object Security {

  def verifyIfAllowed(condition: Boolean)(implicit socialUser: securesocial.core.SocialUser) = {
    if (!condition) {
      throw new SecurityAbuseException(socialUser)
    }
  }
  
  def verifyIfAllowed(condition: () => Boolean)(implicit socialUser: securesocial.core.SocialUser) = {
    if (!condition()) {
      throw new SecurityAbuseException(socialUser)
    }
  }
  
  def verifyIfAllowed(roles: String*)(implicit socialUser: securesocial.core.SocialUser) = {
    for (role <- roles) {
      if (!socialUser.roles.exists(_ == role)) {
        throw new SecurityAbuseException(socialUser)
      }
    }
  }
  
  def verifyIfAllowed(authorizedUserId: Long, authorizedRoles: String*)(implicit socialUser: securesocial.core.SocialUser) = {
    if (authorizedUserId != socialUser.hackathonUserId){
      authorizedRoles.map {
        role => if (!socialUser.roles.exists(_ == role)) {
    				throw new SecurityAbuseException(socialUser)
        		}
      }
    }  
  }
}