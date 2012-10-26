package controllers

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables;
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import play.api.test.FakeRequest$



class ContentSpec extends Specification with DataTables {
	"Content controller" should {
 
    "redirect to Login Page if user not logged in" in {
		""|"httpMethod" | "action"                    |
		1 ! GET         ! "/contents"                 |
		1 ! GET         ! "/contents/new"             |
		1 ! POST        ! "/contents"                 |
	    1 ! POST        ! "/contents/dumymKey/update" |
	    1 ! GET         ! "/contents/dummyKey/edit"   |
	    1 ! GET         ! "/contents/dummyKey/delete" |> {
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