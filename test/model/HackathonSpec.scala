package model

import java.util.Date
import org.specs2.mutable.Specification
import org.squeryl.PrimitiveTypeMode.transaction
import play.api.test.Helpers.inMemoryDatabase
import play.api.test.Helpers.running
import play.api.test.FakeApplication

class HackathonSpec extends Specification {

  "Hackathon model" should {

    "be insertable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val hackathon: Hackathon = new Hackathon("Hackathon Subject", HackathonStatus.Planning, new Date(), "Hackathon Desc", 1L, false, false)
          Hackathon.insert(hackathon)

          hackathon.isPersisted must beTrue
          hackathon.id must beGreaterThan(0L)
          hackathon.status must beEqualTo(HackathonStatus.Planning)
          hackathon.date must not beNull
        }
      }
    }

    "be retrivable by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val hackathon: Hackathon = new Hackathon("Hackathon Subject", HackathonStatus.Planning, new Date(), "Hackathon Desc", 1L, false, false)
          Hackathon.insert(hackathon)

          hackathon.isPersisted must beTrue

          val hackathonDb: Option[Hackathon] = Hackathon.lookup(hackathon.id)

          hackathonDb.isEmpty must beFalse
          hackathonDb.get.id must equalTo(hackathon.id)
        }
      }
    }

    "be retrivable with organiser relationship" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val hackathon: Hackathon = new Hackathon("Hackathon Subject", HackathonStatus.Planning, new Date(), "Hackathon Desc", 1L, false, false)
          Hackathon.insert(hackathon)

          hackathon.isPersisted must beTrue

          val hackathonDb: Option[Hackathon] = Hackathon.lookup(hackathon.id)

          hackathonDb.isEmpty must beFalse
          hackathonDb.get.organiser.name must not beNull
        }
      }
    }

    "be retrivable with problem relationship" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val hackathonDb: Option[Hackathon] = Hackathon.lookup(1L)

          hackathonDb.isEmpty must beFalse
          hackathonDb.get.problems must not beEmpty
        }
      }
    }

    "be retrivable with member relationship" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val hackathon: Hackathon = new Hackathon("Hackathon Subject", HackathonStatus.Planning, new Date(), "Hackathon Desc", 1L, false, false)
          Hackathon.insert(hackathon)
          hackathon.isPersisted must beTrue

          val user1: Option[User] = User.lookup(1L)
          hackathon.addMember(user1.get)

          val hackathonDb: Option[Hackathon] = Hackathon.lookup(hackathon.id)
          hackathonDb.isEmpty must beFalse
          hackathonDb.get.members must not beEmpty

          hackathon.deleteMember(user1.get)

          val hackathonDbNoUsers: Option[Hackathon] = Hackathon.lookup(hackathon.id)
          hackathonDbNoUsers.isEmpty must beFalse
          hackathonDbNoUsers.get.members must beEmpty
        }
      }
    }

    "be retrivable with member-team relationship" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val hackathon: Hackathon = new Hackathon("Hackathon Subject", HackathonStatus.Planning, new Date(), "Hackathon Desc", 1L, false, false)
          Hackathon.insert(hackathon)
          hackathon.isPersisted must beTrue

          val user1: Option[User] = User.lookup(1L)
          hackathon.addMember(user1.get, 1L)

          val hackathonDb: Option[Hackathon] = Hackathon.lookup(hackathon.id)
          hackathonDb.isEmpty must beFalse
          hackathonDb.get.members must not beEmpty
        }
      }
    }

    "be retrievable with team relationship" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val hackathon: Hackathon = new Hackathon("Hackathon Subject", HackathonStatus.Planning, new Date(), "Hackathon Desc", 1L, false, false)
          Hackathon.insert(hackathon)
          hackathon.isPersisted must beTrue

          val team: Team = new Team("New team", TeamStatus.Unverified, 1L, hackathon.id, None)
          Team.insert(team)
          team.isPersisted must beTrue

          val hackathonDb: Option[Hackathon] = Hackathon.lookup(hackathon.id)
          hackathonDb.isEmpty must beFalse
          hackathonDb.get.teams must not beEmpty
        }
      }
    }

    "be retrievable with problem relationship" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val hackathon: Hackathon = new Hackathon("Hackathon Subject", HackathonStatus.Planning, new Date(), "Hackathon Desc", 1L, false, false)
          Hackathon.insert(hackathon)
          hackathon.isPersisted must beTrue

          val problem: Problem = new Problem("New problem", "New problem description", ProblemStatus.Blocked, 1L, hackathon.id)
          Problem.insert(problem)
          problem.isPersisted must beTrue

          val hackathonDb: Option[Hackathon] = Hackathon.lookup(hackathon.id)
          hackathonDb.isEmpty must beFalse
          hackathonDb.get.problems must not beEmpty
        }
      }
    }

    "be retrievable with prize relationship" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val hackathon: Hackathon = new Hackathon("Hackathon Subject", HackathonStatus.Planning, new Date(), "Hackathon Desc", 1L, false, false)
          Hackathon.insert(hackathon)
          hackathon.isPersisted must beTrue

          val prize: Prize = new Prize("Prize name", "Prize description", 1, Some("Founder name"), Some("www.founder.com"), hackathon.id)
          Prize.insert(prize)
          prize.isPersisted must beTrue

          val hackathonDb: Option[Hackathon] = Hackathon.lookup(hackathon.id)
          hackathonDb.isEmpty must beFalse
          hackathonDb.get.prizes must not beEmpty
        }
      }
    }

    "be retrievable with news relationship" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val hackathon: Hackathon = new Hackathon("Hackathon Subject", HackathonStatus.Planning, new Date(), "Hackathon Desc", 1L, false, false)
          Hackathon.insert(hackathon)
          hackathon.isPersisted must beTrue

          val news: News = new News("title", "text", "", 1L, new Date(), Some(hackathon.id))
          News.insert(news)
          news.isPersisted must beTrue

          val hackathonDb: Option[Hackathon] = Hackathon.lookup(hackathon.id)
          hackathonDb.isEmpty must beFalse
          hackathonDb.get.news must not beEmpty
        }
      }
    }

    "be retrievable with sponsor relationship" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val hackathon: Hackathon = new Hackathon("Hackathon Subject", HackathonStatus.Planning, new Date(), "Hackathon Desc", 1L, false, false)
          Hackathon.insert(hackathon)
          hackathon.isPersisted must beTrue

          val sponsor: Sponsor = new Sponsor("Sponsor name", "Sponsor title", "Sponsor description", "Sponsor website", 1, Some(hackathon.id), None)
          Sponsor.insert(sponsor)
          sponsor.isPersisted must beTrue

          val hackathonDb: Option[Hackathon] = Hackathon.lookup(hackathon.id)
          hackathonDb.isEmpty must beFalse
          hackathonDb.get.sponsors must not beEmpty
        }
      }
    }

    "be retrievable with location relationship" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val hackathon: Hackathon = new Hackathon("Hackathon Subject", HackathonStatus.Planning, new Date(), "Hackathon Desc", 1L, false, false)
          Hackathon.insert(hackathon)
          hackathon.isPersisted must beTrue

          val locationDb: Option[Location] = Location.lookup(1L)
          locationDb.isEmpty must beFalse

          hackathon.addLocation(locationDb.get)

          val hackathonDb: Option[Hackathon] = Hackathon.lookup(hackathon.id)
          hackathonDb.isEmpty must beFalse
          hackathonDb.get.locations.isEmpty must beFalse
          hackathonDb.get.hasLocation(locationDb.get.id) must beTrue

          val result: Int = hackathon.deleteLocations()

          val hackathonDbNoLocations: Option[Hackathon] = Hackathon.lookup(hackathon.id)
          hackathonDbNoLocations.isEmpty must beFalse
          hackathonDbNoLocations.get.locations.isEmpty must beTrue
          hackathonDbNoLocations.get.hasLocation(locationDb.get.id) must beFalse
        }
      }
    }

    "be retrivable in bulk" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          Hackathon.insert(new Hackathon("Hackathon Subject", HackathonStatus.Planning, new Date(), "Hackathon Desc", 1L, false, false))
          Hackathon.insert(new Hackathon("Hackathon Subject", HackathonStatus.Planning, new Date(), "Hackathon Desc", 1L, false, false))

          val hackathonsList: Iterable[Hackathon] = Hackathon.all
          hackathonsList must have size (4)
        }
      }
    }

    "be updatable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val hackathon: Hackathon = new Hackathon("Hackathon Subject", HackathonStatus.Planning, new Date(), "Hackathon Desc", 1L, false, false)
          Hackathon.insert(hackathon)
          hackathon.isPersisted must beTrue

          val hackathonUpd: Hackathon = new Hackathon("New Hackathon Subject", HackathonStatus.InProgress, new Date(), "New Hackathon Desc", 1L, true, true)

          val updateResult: Int = Hackathon.update(hackathon.id, hackathonUpd)
          val hackathonDb: Option[Hackathon] = Hackathon.lookup(hackathon.id)

          hackathonDb.isEmpty must beFalse
          (hackathonDb.get.subject == hackathon.subject) must beFalse
          (hackathonDb.get.status == hackathon.status) must beFalse
          (hackathonDb.get.description == hackathon.description) must beFalse
          (hackathonDb.get.newProblemsDisabled == hackathon.newProblemsDisabled) must beFalse
          (hackathonDb.get.newTeamsDisabled == hackathon.newTeamsDisabled) must beFalse
        }
      }
    }

    "be deletable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val hackathon: Hackathon = new Hackathon("Hackathon Subject", HackathonStatus.Planning, new Date(), "Hackathon Desc", 1L, false, false)
          Hackathon.insert(hackathon)
          hackathon.isPersisted must beTrue

          val deleteResult: Int = Hackathon.delete(hackathon.id)
          val hackathonDb: Option[Hackathon] = Hackathon.lookup(hackathon.id)

          hackathonDb.isEmpty must beTrue
        }
      }
    }

  }
}

