package helpers

import core.SecurityAbuseException
import play.api.mvc._
import org.squeryl.PrimitiveTypeMode.inTransaction

object Security {

  def secured(f: => Result)(implicit socialUser: securesocial.core.SocialUser): Result = {
    inTransaction {
      val user = model.User.lookupByOpenId(socialUser.id.id + socialUser.id.providerId)

      user match {
        case Some(u: model.User) if u.isAdmin => f
        case _ => throw new SecurityAbuseException(socialUser)
      }
    }
  }

  def verifyIfAllowed(implicit socialUser: securesocial.core.SocialUser) = {
    inTransaction {
      val user = model.User.lookupByOpenId(socialUser.id.id + socialUser.id.providerId)

      user match {
        case Some(u: model.User) if u.isAdmin => true
        case _ => throw new SecurityAbuseException(socialUser)
      }
    }
  }

  def verifyIfAllowed(condition: Boolean)(implicit socialUser: securesocial.core.SocialUser): Boolean = {
    inTransaction {
      val user = model.User.lookupByOpenId(socialUser.id.id + socialUser.id.providerId)

      user match {
        case Some(u: model.User) if condition => true
        case _ => throw new SecurityAbuseException(socialUser)
      }
    }
  }

  def verifyIfAllowed(authorizedUsersId: Long*)(implicit socialUser: securesocial.core.SocialUser): Boolean = {
    inTransaction {
      val user = model.User.lookupByOpenId(socialUser.id.id + socialUser.id.providerId)

      user match {
        case Some(u: model.User) if u.isAdmin || authorizedUsersId.contains(u.id) => true
        case _ => throw new SecurityAbuseException(socialUser)
      }
    }
  }
}