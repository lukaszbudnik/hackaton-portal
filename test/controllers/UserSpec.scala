package controllers

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables;
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import play.api.test.FakeRequest$



class UserSpec extends Specification with DataTables {
	"User controller" should {
 
    "redirect to Login Page if user not logged in" in {
		""|"httpMethod" | "action"               |
		1 ! GET         ! "/users"               |
		1 ! POST        ! "/users/1/isAdmin/1"   |
	    1 ! POST        ! "/users/1/isBlocked/1" |> {
	    	(justIgnoreMe, httpMethod, action) =>  {
	    		running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
			    val result = routeAndCall(FakeRequest(httpMethod, action)).get
			
			    status(result) must equalTo(SEE_OTHER)
			    redirectLocation(result) must beSome.which(_ == "/login")
	    	  }
	    	}
      }
    }
    
  }
}