package model

import java.util.Date

import org.specs2.mutable.Specification
import org.squeryl.PrimitiveTypeMode.transaction

import play.api.test.FakeApplication
import play.api.test.Helpers.inMemoryDatabase
import play.api.test.Helpers.running

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

