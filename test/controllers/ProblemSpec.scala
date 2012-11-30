package controllers

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import play.api.test.FakeRequest$
import helpers.SecureSocialUtils
import security.SecurityAbuseException

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
      "" | "httpMethod" | "action" |
        1 ! GET ! "/hackathons/1/problems/new" |
        1 ! POST ! "/hackathons/1/problems" |
        1 ! GET ! "/hackathons/1/problems/12/edit" |
        1 ! POST ! "/hackathons/1/problems/12" |
        1 ! POST ! "/hackathons/1/problems/12/delete" |> {
          (justIgnoreMe, httpMethod, action) =>
            {
              running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
                val result = routeAndCall(FakeRequest(httpMethod, action)).get

                status(result) must equalTo(SEE_OTHER)
                redirectLocation(result) must beSome.which(_ == "/login")
              }
            }
        }
    }

    "send 404 when prize not found in edit, update, delete" in {
      "" | "httpMethod" | "action" |
        1 ! GET ! "/hackathons/1/problems/11111" |
        1 ! GET ! "/hackathons/1/problems/11111/edit" |
        1 ! POST ! "/hackathons/1/problems/11111" |
        1 ! POST ! "/hackathons/1/problems/11111/delete" |> {
          (justIgnoreMe, httpMethod, action) =>
            {
              val application = FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))
              running(application) {
                val result = SecureSocialUtils.fakeAuthNormalUser(FakeRequest(httpMethod, action), application)

                status(result) must equalTo(NOT_FOUND)
                contentAsString(result) must contain(helpers.CmsMessages("problems.notFound"))
              }
            }
        }
    }

    "send 404 when hackathon not found in create, save, edit, update, delete" in {
      "" | "httpMethod" | "action" |
        1 ! GET ! "/hackathons/11111/problems/new" |
        1 ! POST ! "/hackathons/11111/problems" |
        1 ! GET ! "/hackathons/11111/problems/11111" |
        1 ! GET ! "/hackathons/11111/problems/11111/edit" |
        1 ! POST ! "/hackathons/11111/problems/11111" |
        1 ! POST ! "/hackathons/11111/problems/11111/delete" |> {
          (justIgnoreMe, httpMethod, action) =>
            {
              val application = FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))
              running(application) {
                val result = SecureSocialUtils.fakeAuthNormalUser(FakeRequest(httpMethod, action), application)

                status(result) must equalTo(NOT_FOUND)
                contentAsString(result) must contain(helpers.CmsMessages("hackathons.notFound"))
              }
            }
        }
    }

    "throw SecurityAbuseException when tampering with edit, update, and delete" in {

      "" | "httpMethod" | "action" |
        1 ! GET ! "/hackathons/1/problems/1/edit" |
        1 ! POST ! "/hackathons/1/problems/1" |
        1 ! POST ! "/hackathons/1/problems/1/delete" |> {
          (justIgnoreMe, httpMethod, action) =>
            {
              val application = FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))
              running(application) {
                {
                  SecureSocialUtils.fakeAuthNormalUser(FakeRequest(httpMethod, action), application)
                } must throwA[SecurityAbuseException]
              }
            }
        }
    }

  }
}