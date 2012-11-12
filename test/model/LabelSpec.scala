package model

import java.util.Date
import org.specs2.mutable.Specification
import org.squeryl.PrimitiveTypeMode.transaction
import play.api.test.Helpers.inMemoryDatabase
import play.api.test.Helpers.running
import play.api.test.FakeApplication
import org.specs2.mutable.After
import org.specs2.mutable.Before

class LabelSpec extends Specification {

  "Location model" should {

    "be insertable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val label: Label = new Label("label")
          Label.insert(label)

          label.isPersisted must beTrue
          label.id must beGreaterThan(0L)

          label.value must not beNull
        }
      }
    }

    "be retrivable by value" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val label: Label = new Label("label")
          Label.insert(label)

          label.isPersisted must beTrue

          val labelDb: Option[Label] = Label.lookupByValue(label.value)

          labelDb.isEmpty must beFalse
          labelDb.get.id must equalTo(label.id)
        }
      }
    }

  }

}

