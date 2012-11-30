package controllers

import org.specs2.mutable.Specification
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import org.squeryl.PrimitiveTypeMode.transaction
import org.specs2.matcher.DataTables
import helpers.SecureSocialUtils
import security.SecurityAbuseException

class TeamSpec extends Specification with DataTables {

  "Team controller" should {
    "Display all team members on GET /hackathons/1/teams/1" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {

          val result = routeAndCall(FakeRequest(GET, "/hackathons/1/teams/1")).get
          val members = model.Team.lookup(1L).get.members

          status(result) must equalTo(OK)
          members.forall(m => contentAsString(result).contains(m.name)) must beTrue
        }
      }
    }

    "redirect to Login Page if user not logged in" in {
      "" | "httpMethod" | "action" |
        1 ! GET ! "/hackathons/1/teams/new" |
        1 ! POST ! "/hackathons/1/teams" |
        1 ! GET ! "/hackathons/1/teams/12/edit" |
        1 ! POST ! "/hackathons/1/teams/12" |
        1 ! POST ! "/hackathons/1/teams/12/delete" |> {
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
        1 ! GET ! "/hackathons/1/teams/11111" |
        1 ! GET ! "/hackathons/1/teams/11111/edit" |
        1 ! POST ! "/hackathons/1/teams/11111" |
        1 ! POST ! "/hackathons/1/teams/11111/delete" |> {
          (justIgnoreMe, httpMethod, action) =>
            {
              val application = FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))
              running(application) {
                val result = SecureSocialUtils.fakeAuthNormalUser(FakeRequest(httpMethod, action), application)

                status(result) must equalTo(NOT_FOUND)
                contentAsString(result) must contain(helpers.CmsMessages("teams.notFound"))
              }
            }
        }
    }

    "send 404 when hackathon not found in create, save, edit, update, delete" in {
      "" | "httpMethod" | "action" |
        1 ! GET ! "/hackathons/11111/teams/new" |
        1 ! POST ! "/hackathons/11111/teams" |
        1 ! GET ! "/hackathons/11111/teams/11111" |
        1 ! GET ! "/hackathons/11111/teams/11111/edit" |
        1 ! POST ! "/hackathons/11111/teams/11111" |
        1 ! POST ! "/hackathons/11111/teams/11111/delete" |> {
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
        1 ! GET ! "/hackathons/1/teams/1/edit" |
        1 ! POST ! "/hackathons/1/teams/1" |
        1 ! POST ! "/hackathons/1/teams/1/delete" |> {
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
