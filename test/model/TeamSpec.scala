package model

import java.util.Date
import java.text.SimpleDateFormat
import play.api.test.FakeApplication
import play.api.test.Helpers.{running,inMemoryDatabase}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeExample
import org.specs2.specification.AfterExample
import org.specs2.specification.BeforeContextExample
import org.squeryl.PrimitiveTypeMode.{transaction, inTransaction, from, select, where}

class TeamSpec extends Specification {

  "Team model" should {
    "be insertable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val team: Team = Team("name", TeamStatus.Unverified, 1, 1)
          model.Team.insert(team)
          
          team.isPersisted must beTrue
          team.id must beGreaterThan(0L)
        }
      }
    }    
    "be retrivable by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val team: Team = Team("name", TeamStatus.Unverified, 1, 1)
          model.Team.insert(team)
          
          team.isPersisted must beTrue

          val teamDb: Option[Team] = model.Team.lookup(team.id)

          teamDb.isEmpty must beFalse
          teamDb.get.id must equalTo(team.id)
        }
      }
    }
    "be retrivable with creator relationship" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val team: Team = Team("name", TeamStatus.Unverified, 1, 1)
          model.Team.insert(team)
          
          team.isPersisted must beTrue

          val teamDb: Option[Team] = model.Team.lookup(team.id)

          teamDb.isEmpty must beFalse
          teamDb.get.creator.name must not beNull
        }
      }
    }
    "be retrivable with hackathon relationship" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val team: Team = Team("name", TeamStatus.Unverified, 1, 1)
          model.Team.insert(team)
          
          team.isPersisted must beTrue

          val teamDb: Option[Team] = model.Team.lookup(team.id)

          teamDb.isEmpty must beFalse
          teamDb.get.hackathon.subject must not beNull
        }
      }
    }
    "be retrivable with optional problem relationship" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val team1: Team = Team("name", TeamStatus.Unverified, 1, 1)
          val team2: Team = Team("name", TeamStatus.Unverified, 1, 1, Some(1))
          model.Team.insert(team1)
          model.Team.insert(team2)
          
          team1.isPersisted must beTrue
          team2.isPersisted must beTrue

          val teamDb1: Option[Team] = model.Team.lookup(team1.id)
          val teamDb2: Option[Team] = model.Team.lookup(team2.id)

          teamDb1.isEmpty must beFalse
          teamDb1.get.problem.isEmpty must beTrue
          
          teamDb2.isEmpty must beFalse
          teamDb2.get.problem.isEmpty must beFalse
          teamDb2.get.problem.get.name must not beNull
        }
      }
    }
    "be retrivable in bulk" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val size = model.Team.all.size
          model.Team.insert(Team("name", TeamStatus.Unverified, 1, 1))
          model.Team.insert(Team("name", TeamStatus.Unverified, 1, 1))
          
          Team.all must have size(size + 2)
        }
      }
    }
    "be updatable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val team1: Team = Team("name", TeamStatus.Unverified, 1, 1)
          model.Team.insert(team1)
          
          team1.isPersisted must beTrue

          val teamDb1: Option[Team] = model.Team.lookup(team1.id)
          
          val team2: Team = Team("name2", TeamStatus.Unverified, 2, 2, Some(2))
          model.Team.update(team1.id, team2)

          val teamDb2: Option[Team] = model.Team.lookup(team1.id)
                    
          teamDb1.isEmpty must beFalse
          teamDb2.isEmpty must beFalse
          
          teamDb1.get.name must beEqualTo("name")
          teamDb2.get.name must beEqualTo("name2")

          teamDb1.get.hackathon.id must beEqualTo(1)
          teamDb2.get.hackathon.id must beEqualTo(2)

          teamDb1.get.creator.id must beEqualTo(1)
          teamDb2.get.creator.id must beEqualTo(2)

          teamDb1.get.problem must beNone         
          teamDb2.get.problem.isEmpty must beFalse
          teamDb2.get.problem.get.id must beEqualTo(2)
        }
      }
    }
    "be deletable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val size = model.Team.all.size
          val team = Team("name", TeamStatus.Unverified, 1, 1)
          
          model.Team.insert(team)
          model.Team.all must have size(size + 1)
          
          model.Team.delete(team.id)
          model.Team.all must have size(size)
        }
      }
    }
  }
}

