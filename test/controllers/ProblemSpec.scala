package controllers

import org.specs2.mutable.Specification
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import play.api.test.FakeRequest$



class ProblemSpec extends Specification {
	"Problem controller" should {

    "Display all problems on GET /problems" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val result = controllers.Problem.index(FakeRequest())

        status(result) must equalTo(OK)
        contentAsString(result) must contain(Messages("problems.title"))
      }
    }
    
    "Display a problem on GET /problems/1" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val result = routeAndCall(FakeRequest(GET, "/problems/1")).get

        status(result) must equalTo(OK)
        contentAsString(result) must contain(Messages("problems.title"))
      }
    }
    
    "redirect to Login Page on GET /problems/new (controllers.Problem.create)" in {
      // application.secret is required for redirections!
      running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
        val result = routeAndCall(FakeRequest(GET, "/problems/new")).get

        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/login")
      }
    }
    
    "redirect to Login Page on POST /problems (controllers.Problem.save)" in {
      // application.secret is required for redirections!
      running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
        val result = routeAndCall(FakeRequest(POST, "/problems")).get

        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/login")
      }
    }

    "redirect to Login Page on GET /problems/1/edit (controllers.Problem.edit)" in {
      // application.secret is required for redirections!
      running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
        val result = routeAndCall(FakeRequest(GET, "/problems/12/edit")).get

        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/login")
      }
    }

    "redirect to Login Page on POST /problems/1 (controllers.Problem.update)" in {
      // application.secret is required for redirections!
      running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
        val result = routeAndCall(FakeRequest(POST, "/problems/12/update")).get

        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/login")
      }
    }

    "redirect to Login Page on POST /problems/1/delete (controllers.Problem.delete)" in {
      // application.secret is required for redirections!
      running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
        val result = routeAndCall(FakeRequest(POST, "/problems/12/delete")).get

        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/login")
      }
    }

  }
}