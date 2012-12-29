package security

import securesocial.core.SocialUser

case class SecurityAbuseException(socialUser: SocialUser) extends Exception {
  override def toString: String = {
    this.getClass().getSimpleName() + " " + socialUser
  }
}
