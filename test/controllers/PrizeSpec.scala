package controllers

import org.specs2.mutable.Specification
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import play.api.test.FakeRequest$
import org.squeryl.PrimitiveTypeMode.transaction
import helpers.CmsMessages
import org.specs2.matcher.DataTables;

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

    "display prize not found when hackathon found on GET /hackathons/:hid/prizes/:id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val result = routeAndCall(FakeRequest(GET, "/hackathons/1/prizes/10")).get

          val prizeDb: Option[model.Prize] = model.Prize.lookup(1L)
          prizeDb.isEmpty must beFalse

          status(result) must equalTo(OK)
          contentAsString(result) must contain(helpers.CmsMessages("prizes.notFound"))
        }
      }
    }

    "redirect to Login Page if user not logged in" in {
      "" | "httpMethod" | "action" |
        1 ! GET ! "/hackathons/1/prizes/new" |
        1 ! POST ! "/hackathons/1/prizes" |
        1 ! GET ! "/hackathons/1/prizes/1/edit" |
        1 ! POST ! "/hackathons/1/prizes/1/update" |
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
  }
}