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

class PrizeSpec extends Specification {

  "Prize model" should {
    "be insertable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val prize: Prize = new Prize(1,1)
          model.Prize.insert(prize)
          
          prize.isPersisted must beTrue
          prize.id must beGreaterThan(0L)
        }
      }
    }    
    "be retrivable by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val prize: Prize = new Prize(1,1)
          model.Prize.insert(prize)
          
          prize.isPersisted must beTrue

          val prizeDb: Option[Prize] = model.Prize.lookup(prize.id)

          prizeDb.isEmpty must beFalse
          prizeDb.get.id must equalTo(prize.id)
        }
      }
    }
    "be retrivable with hackathon relationship" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val prize: Prize = new Prize(1,1)
          model.Prize.insert(prize)
          
          prize.isPersisted must beTrue

          val prizeDb: Option[Prize] = model.Prize.lookup(prize.id)

          prizeDb.isEmpty must beFalse
          prizeDb.get.hackathon.subject must not beNull
        }
      }
    }
    "be retrivable in bulk" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val size = model.Prize.all.size
          model.Prize.insert(new Prize(1,1))
          model.Prize.insert(new Prize(2,2))
          
          Prize.all must have size(size + 2)
        }
      }
    }
    "be updatable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val prize1: Prize = new Prize(1, 1)
          model.Prize.insert(prize1)
          
          prize1.isPersisted must beTrue

          val prizeDb1: Option[Prize] = model.Prize.lookup(prize1.id)
          val prize2: Prize = new Prize(2,2)
          model.Prize.update(prize1.id, prize2)

          val prizeDb2: Option[Prize] = model.Prize.lookup(prize1.id)
                    
          prizeDb1.isEmpty must beFalse
          prizeDb2.isEmpty must beFalse
          

          prizeDb1.get.hackathon.id must beEqualTo(1)
          prizeDb2.get.hackathon.id must beEqualTo(2)


          prizeDb1.get.order must beEqualTo(1)
          prizeDb2.get.order must beEqualTo(2)
        }
      }
    }
    "be deletable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val size = model.Prize.all.size
          val prize = new Prize(1,1)
          
          model.Prize.insert(prize)
          model.Prize.all must have size(size + 1)
          
          model.Prize.delete(prize.id)
          model.Prize.all must have size(size)
        }
      }
    }
  }
}

