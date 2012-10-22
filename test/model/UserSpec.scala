package model

import org.specs2.mutable.Specification
import org.squeryl.PrimitiveTypeMode.transaction
import play.api.test.FakeApplication
import play.api.test.Helpers.{running, inMemoryDatabase}

class UserSpec extends Specification {

  "User model" should {
    "be insertable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val user = User("Łukasz Budnik", "email", "pl", "lukasz-budnik", "lukasz-budnik", "avatar", "openIdHere!", true)
          model.User.insert(user)

          user.isPersisted must beTrue
          user.id must beGreaterThan(0L)
        }
      }
    }
    "be retrivable by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val user = User("Łukasz Budnik", "email", "pl", "lukasz-budnik", "lukasz-budnik", "avatar", "openIdHere!", true)
          model.User.insert(user)

          user.isPersisted must beTrue

          val userDb: Option[User] = model.User.lookup(user.id)

          userDb.isEmpty must beFalse
          userDb.get.id must equalTo(user.id)
        }
      }
    }
  }
}

