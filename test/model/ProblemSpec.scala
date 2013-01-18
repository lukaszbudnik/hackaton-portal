package model

import org.specs2.mutable.Specification
import org.squeryl.PrimitiveTypeMode.{transaction, from, select, where}
import play.api.test.FakeApplication
import play.api.test.Helpers.{running,inMemoryDatabase}
import java.util.Date
import org.specs2.internal.scalaz.Order

class ProblemSpec extends Specification {

  "Problem model" should {
    "be insertable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val problem: Problem = new Problem("Name", "Description", ProblemStatus.Blocked, 1L, 1L)
          model.Problem.insert(problem)

          problem.isPersisted must beTrue
          problem.id must beGreaterThan(0L)
        }

      }
    }
    
    "be retrievable by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val problem: Problem = new Problem("Name", "Description", ProblemStatus.Blocked, 1L, 1L)
          model.Problem.insert(problem)

          problem.isPersisted must beTrue
                    
          val problemDb: Option[Problem] = model.Problem.lookup(problem.id)
          problemDb.isEmpty must beFalse
        }
      }
    }
    
    "be retrievable in bulk" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          
          model.Problem.insert(new Problem("Name", "Description", ProblemStatus.Blocked, 1L, 1L))
          model.Problem.insert(new Problem("Name", "Description", ProblemStatus.Blocked, 1L, 1L))
          
          val problemList: Iterable[Problem] = model.Problem.all
          problemList must have size(4)
        }
      }
    }
  }

}

