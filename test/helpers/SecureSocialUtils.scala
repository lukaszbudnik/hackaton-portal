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
import security.UserService
import play.api.Application

/**
 * Util class for secure social framework
 */
object SecureSocialUtils {
  
  /**
   * Makes call on given request, faking authentication first. Method usefull when testing secured content/ functionality
   * This method will not work when accessing resources restricted for administrator. In such case following user has to be added to db:
   *  
   */
   def fakeAuth[T](request: FakeRequest[AnyContent], application: Application ): Result = {
     // saving user information, this information is then retrieved in SecuredAction when authenticating
    transaction {
      val socialUser = SocialUser(UserId("mockId", "mockProvider"), "mockUser", "mockUser", "mockUser", None, None, AuthenticationMethod.UserPassword)
      val service = new UserService(application)
      securesocial.core.UserService.save(socialUser)
    }
    
    val decoratedRequest = request.withSession(SecureSocial.UserKey -> "mockId", SecureSocial.ProviderKey -> "mockProvider")
    val result = routeAndCall(decoratedRequest).get
    result
    
  }
   
   def fakeAuthNormalUser[T](request: FakeRequest[AnyContent], application: Application ): Result = {
     // saving user information, this information is then retrieved in SecuredAction when authenticating
    transaction {
      val socialUser = SocialUser(UserId("mockNormalUserId", "mockProvider"), "mockNormalUser", "mockNormalUser", "mockNormalUser", None, None, AuthenticationMethod.UserPassword)
      val service = new UserService(application)
      securesocial.core.UserService.save(socialUser)
    }
    
    val decoratedRequest = request.withSession(SecureSocial.UserKey -> "mockNormalUserId", SecureSocial.ProviderKey -> "mockProvider")
    val result = routeAndCall(decoratedRequest).get
    result
    
  }
}