package helpers
import play.api.mvc.Result
import play.api.test.FakeRequest
import securesocial.core.SecureSocial
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import org.squeryl.PrimitiveTypeMode.transaction
import securesocial.core.SocialUser
import securesocial.core.UserId
import securesocial.core.AuthenticationMethod
import securesocial.core.UserService

/**
 * Util class for secure social framework
 */
object SecureSocialUtils {

  
  /**
   * Makes call on given request, faking authentication first. Method usefull when testing secured content/ functionality
   * 
   */
   def fakeAuth[T](request: FakeRequest[AnyContent]): Result = {
     // saving user information, this information is then retrieved in SecuredAction when authenticating
    transaction {
      val socialUser = new SocialUser(new UserId("mockId", "mockProvider"), "mockUser", None, None, AuthenticationMethod.UserPassword)
      UserService.save(socialUser)
    }
    
    val decoratedRequest = request.withSession(SecureSocial.UserKey -> "mockId", SecureSocial.ProviderKey -> "mockProvider")
    val result = routeAndCall(decoratedRequest).get
    result
    
  }
}