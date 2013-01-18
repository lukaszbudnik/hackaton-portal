package helpers

import org.squeryl.PrimitiveTypeMode.transaction

import play.api.Application
import play.api.mvc.AnyContent
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.routeAndCall
import securesocial.core.AuthenticationMethod
import securesocial.core.SecureSocial
import securesocial.core.SocialUser
import securesocial.core.UserId

/**
 * Util class for secure social framework
 */
object SecureSocialUtils {
	
	/**
	 * Makes call on given request, faking authentication first. Method usefull when testing secured content/ functionality
	 * This method will not work when accessing resources restricted for administrator. In such case following user has to be added to db:
	 * mockId - admin
	 */
	 def fakeAuth[T](request: FakeRequest[AnyContent], application: Application ): Result = {
		 // saving user information, this information is then retrieved in SecuredAction when authenticating
		transaction {
			val socialUser = SocialUser(UserId("mockId", "mockProvider"), "mockUser", "mockUser", "mockUser", None, None, AuthenticationMethod.UserPassword)
			securesocial.core.UserService.save(socialUser)
		}
		
		val decoratedRequest = request.withSession(SecureSocial.UserKey -> "mockId", SecureSocial.ProviderKey -> "mockProvider", SecureSocial.lastAccess)
		val result = routeAndCall(decoratedRequest).get
		result
		
	}

	/**
	 * Makes call on given request, faking authentication first. Method usefull when testing secured content/ functionality
	 * This method will not work when accessing resources restricted for administrator. In such case following user has to be added to db:
	 * mockNormalUserId - normaluser
	 */ 
	 def fakeAuthNormalUser[T](request: FakeRequest[AnyContent], application: Application ): Result = {
		 // saving user information, this information is then retrieved in SecuredAction when authenticating
		transaction {
			val socialUser = SocialUser(UserId("mockNormalUserId", "mockProvider"), "mockNormalUser", "mockNormalUser", "mockNormalUser", None, None, AuthenticationMethod.UserPassword)
			securesocial.core.UserService.save(socialUser)
		}
		
		val decoratedRequest = request.withSession(SecureSocial.UserKey -> "mockNormalUserId", SecureSocial.ProviderKey -> "mockProvider", SecureSocial.lastAccess)
		val result = routeAndCall(decoratedRequest).get
		result
		
	}
}