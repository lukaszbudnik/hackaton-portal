package security

import securesocial.core.SocialUser

case class SecurityAbuseException(socialUser: SocialUser) extends Exception
