/**
 *
 */
package model

import org.specs2.mutable.Specification
import org.squeryl.PrimitiveTypeMode.transaction
import play.api.test.FakeApplication
import play.api.test.Helpers.{running, inMemoryDatabase}

/**
 * @author tomaszj
 *
 */
class UserTeamSpec extends Specification {
	
  "UserTeam model" should {
    "allow associations and retrieve all users for a team" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val user1 = User("Łukasz Budnik", "email", "lukasz-budnik", "lukasz-budnik", "avatar", "openIdHere!")
          Model.users.insert(user1)
          val user2 = User("Łukasz Budnik2", "email", "lukasz-budnik", "lukasz-budnik", "avatar", "openIdHere2!")
          Model.users.insert(user2)

          val team = Team("test-team", 1L, 1L, Some(1L))
          Model.teams.insert(team)
          
          user1.teams.associate(team)
          user2.teams.associate(team)
          
          val results = Model.allUsersForTeam(team.id).toIterable
          results.size must equalTo(2)
        }
      }
    }
  }
  
}