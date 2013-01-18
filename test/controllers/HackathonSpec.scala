package controllers

import org.specs2.mutable.Specification
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import play.api.test.FakeRequest$
import org.squeryl.PrimitiveTypeMode.transaction
import helpers.CmsMessages
import org.specs2.matcher.DataTables
import securesocial.core.SecureSocial
import securesocial.core.UserService
import securesocial.core.SocialUser
import securesocial.core.UserId
import securesocial.core.AuthenticationMethod
import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.api.mvc.Result
import helpers.SecureSocialUtils
import security.SecurityAbuseException

class HackathonSpec extends Specification with DataTables {

  "Hackathon controller" should {

    "display all on GET /hackathons" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val result = controllers.Hackathon.index(FakeRequest())

        status(result) must equalTo(OK)
        contentAsString(result) must contain(helpers.CmsMessages("hackathons.title"))
      }
    }

    "display hackathon details on GET /hackathons/id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val result = routeAndCall(FakeRequest(GET, "/hackathons/1")).get

          val hackathonDb: Option[model.Hackathon] = model.Hackathon.lookup(1L)
          hackathonDb.isEmpty must beFalse

          status(result) must equalTo(OK)
          contentAsString(result) must contain(helpers.CmsMessages("hackathons.organiser.label"))
        }
      }
    }

    "display the json message containing all hackathons info on GET /hackathons.json" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val result = routeAndCall(FakeRequest(GET, "/hackathons.json")).get

          val hackathonsDb: Iterable[model.Hackathon] = model.Hackathon.all
          hackathonsDb.isEmpty must beFalse

          status(result) must equalTo(OK)
          for (hackathon <- hackathonsDb) {
            contentAsString(result) must contain(hackathon.subject)
          }
        }
      }
    }

    "display the json message containing hackathon info on GET /hackathon.json/:id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val result = routeAndCall(FakeRequest(GET, "/hackathon.json/1")).get

          val hackathonDb: Option[model.Hackathon] = model.Hackathon.lookup(1L)
          hackathonDb.isEmpty must beFalse

          status(result) must equalTo(OK)
          contentAsString(result) must contain(hackathonDb.get.subject)
        }
      }
    }

    "display the empty json message when no hackathon found on GET /hackathon.json/:id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val result = routeAndCall(FakeRequest(GET, "/hackathon.json/1")).get

          val hackathonDb: Option[model.Hackathon] = model.Hackathon.lookup(1L)
          hackathonDb.isEmpty must beFalse

          status(result) must equalTo(OK)
          contentAsString(result) must contain("")
        }
      }
    }

    "display add hackathon form when logged in" in {

      val fakeapp = FakeApplication(additionalConfiguration = inMemoryDatabase())
      running(fakeapp) {

        val action = "/hackathons/new"
        // adding userId 
        val result = SecureSocialUtils.fakeAuth(FakeRequest(GET, action), fakeapp)

        status(result) must equalTo(OK)
        contentAsString(result) must contain(helpers.CmsMessages("hackathons.create.title"))
      }
    }

    "redirect to Login Page if user not logged in" in {

      "" | "httpMethod" | "action" |
        1 ! GET ! "/hackathons/new" |
        1 ! POST ! "/hackathons" |
        1 ! GET ! "/hackathons/1/edit" |
        1 ! POST ! "/hackathons/1" |
        1 ! POST ! "/hackathons/1/delete" |
        1 ! GET ! "/hackathons/1/join" |
        1 ! GET ! "/hackathons/1/disconnect" |
        1 ! GET ! "/hackathons/1/disconnect/1" |> {
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

    "send 404 and display not found on view, chat, edit, update, delete when news does not exists" in {

      "" | "httpMethod" | "action" |
        1 ! GET ! "/hackathons/11111" |
        1 ! GET ! "/hackathons/11111/chat" |
        1 ! GET ! "/hackathons/11111/edit" |
        1 ! POST ! "/hackathons/11111" |
        1 ! POST ! "/hackathons/11111/delete" |
        1 ! GET ! "/hackathons/11111/join" |
        1 ! GET ! "/hackathons/11111/disconnect" |
        1 ! GET ! "/hackathons/11111/disconnect/1" |> {
          (justIgnoreMe, httpMethod, action) =>
            {
              val application = FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))
              running(application) {
                val result = SecureSocialUtils.fakeAuth(FakeRequest(httpMethod, action), application)

                status(result) must equalTo(NOT_FOUND)
                contentAsString(result) must contain(helpers.CmsMessages("hackathons.notFound"))
              }
            }
        }
    }

    "throw SecurityAbuseException when tampering with edit, update, and delete" in {

      "" | "httpMethod" | "action" |
        1 ! GET ! "/hackathons/1/edit" |
        1 ! POST ! "/hackathons/1" |
        1 ! POST ! "/hackathons/1/delete" |
        1 ! GET ! "/hackathons/1/disconnect/1" |> {
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
