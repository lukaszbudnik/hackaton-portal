package model

import org.specs2.mutable.Specification
import org.squeryl.PrimitiveTypeMode.{transaction, inTransaction, from, select, where}
import play.api.test.FakeApplication
import play.api.test.Helpers.{running,inMemoryDatabase}
import org.specs2.specification.BeforeExample
import org.specs2.specification.AfterExample
import org.specs2.specification.BeforeContextExample

class SponsorSpec extends Specification {

  "Sponsor model" should {
    "be insertable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          
          val sponsor: Sponsor = new Sponsor("name", "title", "description", "website", 1, None, None)
          Sponsor.insert(sponsor)
          
          sponsor.isPersisted must beTrue
          sponsor.id must beGreaterThan(0L)
        }
      }
    }
    "be retrivable by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          
          val sponsor: Sponsor = new Sponsor("name", "title", "description", "website", 1, None, None)
          Sponsor.insert(sponsor)
          
          sponsor.isPersisted must beTrue

          val sponsorDb: Option[Sponsor] = Sponsor.lookup(sponsor.id)

          sponsorDb.isEmpty must beFalse
          sponsorDb.get.id must equalTo(sponsor.id)
        }
      }
    }
    "be retrivable by id with hackaton relationship" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          
          val sponsor: Sponsor = new Sponsor("name", "title", "description", "website", 1, Some(1L), None)
          Sponsor.insert(sponsor)
          
          sponsor.isPersisted must beTrue

          val sponsorDb: Option[Sponsor] = Sponsor.lookup(sponsor.id)

          sponsorDb.isEmpty must beFalse
          sponsorDb.get.hackathon.get.organiser must not beNull
        }
      }
    }
    "be retrivable in bulk" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          
          val originalSize = Sponsor.all.size
          
          Sponsor.insert(new Sponsor("name", "title", "description", "website", 1, None, None))
          Sponsor.insert(new Sponsor("name", "title", "description", "website", 1, None, None))
          
          val sponsorList: Iterable[Sponsor] = Sponsor.all
          sponsorList must have size(originalSize + 2)
        }
      }
    }
  }
}

