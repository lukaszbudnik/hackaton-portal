package controllers

import org.specs2.mutable.Specification
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import play.api.test.FakeRequest$
import org.squeryl.PrimitiveTypeMode.transaction
import helpers.CmsMessages
import org.specs2.matcher.DataTables
import helpers.SecureSocialUtils
import core.SecurityAbuseException

class PrizeSpec extends Specification with DataTables {

  "Prize controller" should {

    "display all prizes when hackathon found on GET /hackathons/:hid/prizes" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val result = routeAndCall(FakeRequest(GET, "/hackathons/1/prizes")).get

          val hackathonDb: Option[model.Hackathon] = model.Hackathon.lookup(1L)
          hackathonDb.isEmpty must beFalse

          status(result) must equalTo(OK)
          for (prize <- hackathonDb.get.prizes) {
            contentAsString(result) must contain(prize.name)
            contentAsString(result) must contain(prize.description)
          }
        }
      }
    }

    "display prize details when hackathon found on GET /hackathons/:hid/prizes/:id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val result = routeAndCall(FakeRequest(GET, "/hackathons/1/prizes/1")).get

          val prizeDb: Option[model.Prize] = model.Prize.lookup(1L)
          prizeDb.isEmpty must beFalse

          status(result) must equalTo(OK)
          contentAsString(result) must contain(prizeDb.get.name)
          contentAsString(result) must contain(prizeDb.get.description)
        }
      }
    }

    "redirect to Login Page if user not logged in" in {
      "" | "httpMethod" | "action" |
        1 ! GET ! "/hackathons/1/prizes/new" |
        1 ! POST ! "/hackathons/1/prizes" |
        1 ! GET ! "/hackathons/1/prizes/1/edit" |
        1 ! POST ! "/hackathons/1/prizes/1" |
        1 ! POST ! "/hackathons/1/prizes/1/delete" |> {
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
        1 ! GET ! "/hackathons/1/prizes/11111" |
        1 ! GET ! "/hackathons/1/prizes/11111/edit" |
        1 ! POST ! "/hackathons/1/prizes/11111" |
        1 ! POST ! "/hackathons/1/prizes/11111/delete" |> {
          (justIgnoreMe, httpMethod, action) =>
            {
              val application = FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))
              running(application) {
                val result = SecureSocialUtils.fakeAuthNormalUser(FakeRequest(httpMethod, action), application)

                status(result) must equalTo(NOT_FOUND)
                contentAsString(result) must contain(helpers.CmsMessages("prizes.notFound"))
              }
            }
        }
    }
    
    "send 404 when hackathon not found in edit, update, delete" in {
      "" | "httpMethod" | "action" |
        1 ! GET ! "/hackathons/11111/prizes/11111" |
        1 ! GET ! "/hackathons/11111/prizes/11111/edit" |
        1 ! POST ! "/hackathons/11111/prizes/11111" |
        1 ! POST ! "/hackathons/11111/prizes/11111/delete" |> {
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
        1 ! GET ! "/hackathons/1/prizes/1/edit" |
        1 ! POST ! "/hackathons/1/prizes/1" |
        1 ! POST ! "/hackathons/1/prizes/1/delete" |> {
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