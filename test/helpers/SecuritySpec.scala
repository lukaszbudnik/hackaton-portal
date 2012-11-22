package helpers

import org.specs2.mutable._
import core.SecurityAbuseException
import securesocial.core._
import play.api.test.Helpers._
import play.api.test.FakeApplication

class SecuritySpec extends Specification {

//  "Security helper" should {
//
//    "should throw SecurityAbuseException when conditions evaluate to false" in {
//      running(FakeApplication()) {
//        implicit val socialUser = SocialUser(UserId("id", "providerId"), "displayName", Some("email"), Some("avatarUrl"), AuthenticationMethod.OpenId)
//        
//        Security.verifyIfAllowed(false) must throwA[SecurityAbuseException]
//      }
//    }
//
//    "should throw SecurityAbuseException when user is not an admin" in {
//      running(FakeApplication()) {
//        implicit val socialUser = SocialUser(UserId("id", "providerId"), "displayName", Some("email"), Some("avatarUrl"), AuthenticationMethod.OpenId)
//
//        Security.verifyIfAllowed() must throwA[SecurityAbuseException]
//      }
//    }
//
//  }

}