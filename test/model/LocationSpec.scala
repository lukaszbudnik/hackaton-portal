package model

import java.util.Date
import org.specs2.mutable.Specification
import org.squeryl.PrimitiveTypeMode.transaction
import play.api.test.Helpers.inMemoryDatabase
import play.api.test.Helpers.running
import play.api.test.FakeApplication
import org.specs2.mutable.After
import org.specs2.mutable.Before

class LocationSpec extends Specification {

  
  
  "Location model" should {

     
        
    
    
    "be insertable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val location: Location = new Location(1L, "country", "city", "postal code", "full address", "name", 1, 2, 3)
          Location.insert(location)

          location.isPersisted must beTrue
          location.id must beGreaterThan(0L)
          location.status must beEqualTo(LocationStatus.Unverified)
          location.country must not beNull
        }
      }
    }

    "be retrivable by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val location: Location = new Location(1L, "country", "city", "postal code", "full address", "name", 1, 2, 3)
          Location.insert(location)

          location.isPersisted must beTrue

          val locationDb: Option[Location] = Location.lookup(location.id)

          locationDb.isEmpty must beFalse
          locationDb.get.id must equalTo(location.id)
        }
      }
    }


    "be retrivable in bulk" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          println("asdasd")
          Location.insert(new Location(1L, "country", "city", "postal code", "full address", "name", 1, 2, 3))
          Location.insert(new Location(1L, "country", "city", "postal code", "full address", "name", 1, 2, 3))
          
          val locationList: Iterable[Location] = Location.all
          locationList must have size (4)
        }
      }
    }

    "be updatable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          
          val location: Location = new Location(1L, "country", "city", "postal code", "full address", "name", 1, 2, 3)
          Location.insert(location)

          location.isPersisted must beTrue

          val locationUpd: Location = new Location(1L, "country1", "city1", "postal code1", "full address1", "name", 1, 2, 3)

          val updateResult: Int = Location.update(location.id, locationUpd)
          val locationDb: Option[Location] = Location.lookup(location.id)

          locationDb.isEmpty must beFalse
          (locationDb.get.country == location.country) must beFalse
          (locationDb.get.city == location.city) must beFalse
          (locationDb.get.postalCode == location.postalCode) must beFalse
          (locationDb.get.fullAddress == location.fullAddress) must beFalse
          (locationDb.get.name == location.name) must beTrue
        }
      }
    }

    "be deletable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val location: Location = new Location(1L, "country", "city", "postal code", "full address", "name", 1, 2, 3)
          Location.insert(location)
          location.isPersisted must beTrue

          val deleteResult: Int = Location.delete(location.id)
          val locationDb: Option[Location] = Location.lookup(location.id)

          locationDb.isEmpty must beTrue
        }
      }
    }

  }

}

