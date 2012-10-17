package controllers

import org.specs2.mutable.Specification
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import play.api.test.FakeRequest$



class ProblemSpec extends Specification {
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
    
    "redirect to Login Page on GET /hackathons/1/problems/new (controllers.Problem.create)" in {
      // application.secret is required for redirections!
      running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
        val result = routeAndCall(FakeRequest(GET, "/hackathons/1/problems/new")).get

        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/login")
      }
    }
    
    "redirect to Login Page on POST /hackathons/1/problems (controllers.Problem.save)" in {
      // application.secret is required for redirections!
      running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
        val result = routeAndCall(FakeRequest(POST, "/hackathons/1/problems")).get

        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/login")
      }
    }

    "redirect to Login Page on GET /hackathons/1/problems/1/edit (controllers.Problem.edit)" in {
      // application.secret is required for redirections!
      running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
        val result = routeAndCall(FakeRequest(GET, "/hackathons/1/problems/12/edit")).get

        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/login")
      }
    }

    "redirect to Login Page on POST /hackathons/1/problems/1 (controllers.Problem.update)" in {
      // application.secret is required for redirections!
      running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
        val result = routeAndCall(FakeRequest(POST, "/hackathons/1/problems/12/update")).get

        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/login")
      }
    }

    "redirect to Login Page on POST /hackathons/1/problems/1/delete (controllers.Problem.delete)" in {
      // application.secret is required for redirections!
      running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
        val result = routeAndCall(FakeRequest(POST, "/hackathons/1/problems/12/delete")).get

        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/login")
      }
    }

  }
}