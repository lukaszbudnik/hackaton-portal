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
          val problem: Problem = new Problem("Name", "Description", 1L, 1L)
          Model.problems.insert(problem)

          problem.isPersisted must beTrue
          problem.id must beGreaterThan(0L)
        }

      }
    }
    
    "be retrievable by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val problem: Problem = new Problem("Name", "Description", 1L, 1L)
          Model.problems.insert(problem)
          
          val problemDb: Option[Problem] = Model.lookupProblem(problem.id)
          problem.isPersisted must beTrue
          problem must not beNull
        }
      }
    }
    
    "be retrievable in bulk" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          Model.problems.insert(new Problem("Name", "Description", 1L, 1L))
          Model.problems.insert(new Problem("Name", "Description", 1L, 1L))
          
          val problemList: Iterable[Problem] = Model.allProblems
          problemList must not beNull
        }
      }
    }
  }

}

