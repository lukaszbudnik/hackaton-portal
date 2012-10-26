package controllers

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables;
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import play.api.test.FakeRequest$



class ProblemSpec extends Specification with DataTables {
	"Problem controller" should {

    "Display all problems on GET /hackathons/1/problems" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val result = controllers.Problem.index(1)(FakeRequest())

        status(result) must equalTo(OK)
        contentAsString(result) must contain(helpers.CmsMessages("hackathons.view.title"))
      }
    }
    
    "Display a problem on GET /hackathons/1/problems/1" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val result = routeAndCall(FakeRequest(GET, "/hackathons/1/problems/1")).get

        status(result) must equalTo(OK)
        contentAsString(result) must contain(helpers.CmsMessages("hackathons.view.title"))
      }
    }
    
    "redirect to Login Page if user not logged in" in {
		""|"httpMethod" | "action"                           |
		1 ! GET         ! "/hackathons/1/problems/new"       |
		1 ! POST        ! "/hackathons/1/problems"           |
		1 ! POST        ! "/hackathons/1/problems/12/update" |
	    1 ! POST        ! "/hackathons/1/problems/12/delete" |
	    1 ! GET         ! "/hackathons/1/problems/12/edit"   |
	    1 ! POST        ! "/hackathons/1/problems/12/update" |
	    1 ! POST        ! "/hackathons/1/problems/12/delete" |> {
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