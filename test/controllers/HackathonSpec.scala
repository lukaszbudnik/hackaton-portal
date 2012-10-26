package controllers

import org.specs2.mutable.Specification
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import play.api.test.FakeRequest$
import org.squeryl.PrimitiveTypeMode.transaction
import helpers.CmsMessages
import org.specs2.matcher.DataTables;

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

    "display message about no hackathon found on GET /hackathons/id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val result = routeAndCall(FakeRequest(GET, "/hackathons/10")).get

        status(result) must equalTo(OK)
        contentAsString(result) must contain(helpers.CmsMessages("hackathons.notFound"))
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

    "redirect to Login Page if user not logged in" in {
      "" | "httpMethod" | "action" |
        1 ! GET ! "/hackathons/new" |
        1 ! POST ! "/hackathons" |
        1 ! GET ! "/hackathons/1/edit" |
        1 ! POST ! "/hackathons/1/update" |
        1 ! POST ! "/hackathons/1/delete" |
        1 ! GET ! "/hackathons/1/join" |
        1 ! GET ! "/hackathons/1/disconnect" |
        1 ! GET ! "/hackathons/1/1/disconnect" |> {
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
