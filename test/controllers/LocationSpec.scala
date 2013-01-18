package controllers

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables;
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import play.api.test.FakeRequest$



class LocationSpec extends Specification with DataTables {
	"Location controller" should {
 
    "redirect to Login Page if user not logged in" in {
		""|"httpMethod" | "action"                    |
		1 ! GET         ! "/locations/find?term=dummy"|
		1 ! GET         ! "/locations/new"            |
		1 ! GET         ! "/locations/newA"           |
		1 ! POST        ! "/locations"                |
	    1 ! POST        ! "/locations/newA"           |
	    1 ! GET         ! "/locations"                |
	    1 ! GET         ! "/locations/1"              |
	    1 ! GET         ! "/locations/1/edit"         |
	    1 ! GET         ! "/locations/1/editA"        |
	    1 ! POST        ! "/locations/1/update"       |
	    1 ! POST        ! "/locations/1/updateA"      |
	    1 ! POST        ! "/locations/1/delete"       |> {
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