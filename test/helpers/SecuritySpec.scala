package helpers

import org.specs2.mutable._
import core.SecurityAbuseException
import securesocial.core._

class SecuritySpec extends Specification {

  "Security helper" should {
    
    "should throw SecurityAbuseException when conditions evaluate to false" in {
      Security.verifyIfAllowed(() => { false })(null) must throwA[SecurityAbuseException]
      
      Security.verifyIfAllowed(false)(null) must throwA[SecurityAbuseException]
    }
    
    "should throw SecurityAbuseException when user is not an admin" in {
      implicit val socialUser = SocialUser(UserId("id", "providerId"), "displayName", Some("email"), Some("avatarUrl"), AuthenticationMethod.OpenId, None, None, 123, Seq("viewer"))
      
      Security.verifyIfAllowed() must throwA[SecurityAbuseException]
    }

  }
  
}