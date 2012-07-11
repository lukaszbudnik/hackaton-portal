package model

import org.specs2.mutable.Specification
import org.squeryl.PrimitiveTypeMode.transaction
import play.api.test.FakeApplication
import play.api.test.Helpers.{ running, inMemoryDatabase }

class RoleSpec extends Specification {

  "Role model" should {
    "be retrievable by 'admin' name" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val admin = "admin"
          
          val role = model.Role.lookupByName(admin)

          role.get.name must equalTo(admin)
        }
      }
    }
    
    "be retrievable by 'user' name" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val user = "user"
          
          val role = model.Role.lookupByName(user)

          role.get.name must equalTo(user)
        }
      }
    }
    
  }
}