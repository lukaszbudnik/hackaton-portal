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
import service.UserService
import play.api.Application

/**
 * Util class for secure social framework
 */
object SecureSocialUtils {

  
  /**
   * Makes call on given request, faking authentication first. Method usefull when testing secured content/ functionality
   * This method will not work when accessing resources restricted for administrator. In such case following user has to be added to db:
   * 
   *  insert into users (name, email, github_username, open_id, avatar_url, is_admin) values ('Mock mockowski','d.bykowski@kainos.com', 'bykes', 'mockIdmockProvider', 'https://lh4.googleusercontent.com/-_qzaBoetBfc/AAAAAAAAAAI/AAAAAAAAENc/q3rBQpasMfI/photo.jpg', true);
   *  
   */
   def fakeAuth[T](request: FakeRequest[AnyContent], application: Application ): Result = {
     // saving user information, this information is then retrieved in SecuredAction when authenticating
    transaction {
      val socialUser = new SocialUser(new UserId("mockId", "mockProvider"), "mockUser", None, None, AuthenticationMethod.UserPassword, true, None, None, None, 0, true)
      val service = new UserService(application)
      securesocial.core.UserService.save(socialUser)
    }
    
    val decoratedRequest = request.withSession(SecureSocial.UserKey -> "mockId", SecureSocial.ProviderKey -> "mockProvider")
    val result = routeAndCall(decoratedRequest).get
    result
    
  }
}