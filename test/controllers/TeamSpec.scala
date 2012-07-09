/**
 *
 */
package controllers

import org.specs2.mutable.Specification
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import model.Model
import org.squeryl.PrimitiveTypeMode.transaction

/**
 * @author tomaszj
 *
 */
class TeamSpec extends Specification {

  "Team controller" should {
    "Display all team members on GET /teams/1" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {

          val result = routeAndCall(FakeRequest(GET, "/teams/1")).get
          val members = model.Team.lookup(1L).get.members

          status(result) must equalTo(OK)
          members.forall(m => contentAsString(result).contains(m.name)) must beTrue
        }
      }
    }
  }
}