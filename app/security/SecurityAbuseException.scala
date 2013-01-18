package security

import securesocial.core.Identity

case class SecurityAbuseException(socialUser: Identity) extends Exception {
  override def toString: String = {
    this.getClass().getSimpleName() + " " + socialUser
  }
}
