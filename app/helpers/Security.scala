package helpers

import core.SecurityAbuseException
import play.api.mvc._

object Security {

  def verifyIfAllowed(implicit socialUser: securesocial.core.SocialUser) = {
    if (!socialUser.isAdmin) {
      throw new SecurityAbuseException(socialUser)
    }
  }

  def secured(f: => Result)(implicit socialUser: securesocial.core.SocialUser) : Result = {
    if (!socialUser.isAdmin) {
      throw new SecurityAbuseException(socialUser)
    }
    else {
      f
    }
  }
  
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

  def verifyIfAllowed(authorizedUsersId: Long*)(implicit socialUser: securesocial.core.SocialUser) = {
    if (!socialUser.isAdmin) {
      if (!authorizedUsersId.contains(socialUser.hackathonUserId)) {
        throw new SecurityAbuseException(socialUser)
      }
    }
  }
}