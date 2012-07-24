/**
 *
 */
package model

import org.specs2.mutable.Specification
import org.squeryl.PrimitiveTypeMode.transaction
import play.api.test.FakeApplication
import play.api.test.Helpers.{ running, inMemoryDatabase }

class UserTeamSpec extends Specification {

  "UserTeam model" should {
    "allow add and retrive members for a team" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val user1 = User("Łukasz Budnik", "email", "lukasz-budnik", "lukasz-budnik", "avatar", "openIdHere!", false)
          val user2 = User("Łukasz Budnik2", "email", "lukasz-budnik", "lukasz-budnik", "avatar", "openIdHere2!", false)
          val team = Team("test-team", 1L, 1L, Some(1L))

          model.User.insert(user1)
          model.User.insert(user2)
          model.Team.insert(team)

          user1.isPersisted must beTrue
          user2.isPersisted must beTrue
          team.isPersisted must beTrue

          team.addMember(user1)
          team.addMember(user2)

          val teamDb = model.Team.lookup(team.id)

          teamDb.isEmpty must beFalse
          teamDb.get.members.size must equalTo(2)
          teamDb.get.hasMember(user1.id) must beTrue
          teamDb.get.hasMember(user2.id) must beTrue
          teamDb.get.hasMember(-1) must beFalse
          
          val userDb1 = teamDb.get.members.find(u => u.id == user1.id)
          userDb1.isEmpty must beFalse
          userDb1.get.name must not beNull
          
          val userDb2 = teamDb.get.members.find(u => u.id == user2.id)
          userDb2.isEmpty must beFalse
          userDb2.get.name must not beNull
        }
      }
    }
    "allow delete members from a team" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val user1 = User("Łukasz Budnik", "email", "lukasz-budnik", "lukasz-budnik", "avatar", "openIdHere!", false)
          val user2 = User("Łukasz Budnik2", "email", "lukasz-budnik", "lukasz-budnik", "avatar", "openIdHere2!", false)
          val team = Team("test-team", 1L, 1L, Some(1L))

          model.User.insert(user1)
          model.User.insert(user2)
          model.Team.insert(team)

          user1.isPersisted must beTrue
          user2.isPersisted must beTrue
          team.isPersisted must beTrue

          team.addMember(user1)
          team.addMember(user2)

          val teamDb1 = model.Team.lookup(team.id)
          teamDb1.isEmpty must beFalse
          teamDb1.get.members.size must equalTo(2)
          
          team.deleteMember(user1)
          
          val teamDb2 = model.Team.lookup(team.id)
          teamDb2.isEmpty must beFalse
          teamDb2.get.members.size must equalTo(1)
          teamDb2.get.hasMember(user1.id) must beFalse
          teamDb2.get.hasMember(user2.id) must beTrue

          team.deleteMember(user2)
          
          val teamDb3 = model.Team.lookup(team.id)
          teamDb3.isEmpty must beFalse
          teamDb3.get.members.size must equalTo(0)   
          teamDb3.get.hasMember(user1.id) must beFalse
          teamDb3.get.hasMember(user2.id) must beFalse

        }
      }
    }
    "allow add and retrieve teams for a user" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val user1 = User("Łukasz Budnik", "email", "lukasz-budnik", "lukasz-budnik", "avatar", "openIdHere!", false)
          val user2 = User("Łukasz Budnik2", "email", "lukasz-budnik", "lukasz-budnik", "avatar", "openIdHere2!", false)
          val team = Team("test-team", 1L, 1L, Some(1L))
          
          model.User.insert(user1)
          model.User.insert(user2)
          model.Team.insert(team)
          
          user1.isPersisted must beTrue
          user2.isPersisted must beTrue
          team.isPersisted must beTrue

          user1.addTeam(team)
          user2.addTeam(team)
          
          val userDb1 = model.User.lookup(user1.id)
          userDb1.isEmpty must beFalse
          userDb1.get.teams.size must equalTo(1)

          val userDb2 = model.User.lookup(user2.id)
          userDb2.isEmpty must beFalse
          userDb2.get.teams.size must equalTo(1)
        }
      }
    }
  }

}