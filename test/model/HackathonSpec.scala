package model

import org.specs2.mutable.Specification
import org.squeryl.PrimitiveTypeMode.{transaction, inTransaction, from, select, where}
import play.api.test.FakeApplication
import play.api.test.Helpers.{running,inMemoryDatabase}
import java.util.Date
import org.specs2.specification.BeforeExample
import org.specs2.specification.AfterExample
import org.specs2.specification.BeforeContextExample

class HackathonSpec extends Specification {

  "Hackathon model" should {
    "be retrivable with location relationship" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {

          val hackathonDb: Option[Hackathon] = model.Hackathon.lookup(1L)

          hackathonDb.isEmpty must beFalse
          
         // hackathonDb.get.location.fullAddress must not beNull
        }
      }
    }
  }
}

