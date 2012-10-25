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

class ResourceSpec extends Specification {

  "Resource model" should {
    "be insertable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val resource: Resource = Resource("http://fake", "id001")
          model.Resource.insert(resource)
          
          resource.isPersisted must beTrue
          resource.id must beGreaterThan(0L)
        }
      }
    }
    "be retrivable by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val resource: Resource= Resource("http://fake", "id001")
          model.Resource.insert(resource)
          
          resource.isPersisted must beTrue

          val resourceDb: Option[Resource] = model.Resource.lookup(resource.id)

          resourceDb.isEmpty must beFalse
          resourceDb.get.id must equalTo(resource.id)
        }
      }
    }
    "be retrivable in bulk" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val size = model.Resource.all.size
          model.Resource.insert(Resource("http://urla", "id001"))
          model.Resource.insert(Resource("http://urlb", "id002"))
          
          Resource.all must have size(size + 2)
        }
      }
    }
    "be updatable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val resource1: Resource = Resource("http://urla", "id001")
          model.Resource.insert(resource1)
          
          resource1.isPersisted must beTrue

          val resourceDb1: Option[Resource] = model.Resource.lookup(resource1.id)
          
          val resource2: Resource = Resource("http://urlb", "id002")
          model.Resource.update(resource1.id, resource2)

          val resourceDb2: Option[Resource] = model.Resource.lookup(resource1.id)
                    
          resourceDb1.isEmpty must beFalse
          resourceDb2.isEmpty must beFalse
          
          resourceDb1.get.url must beEqualTo("http://urla")
          resourceDb2.get.url must beEqualTo("http://urlb")

          resourceDb1.get.publicId must beEqualTo("id001")
          resourceDb2.get.publicId must beEqualTo("id002")
        }
      }
    }
    "be deletable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val size = model.Resource.all.size
          val resource: Resource = Resource("http://urla", "id001")
          
          model.Resource.insert(resource)
          model.Resource.all must have size(size + 1)
          
          model.Resource.delete(resource.id)
          model.Resource.all must have size(size)
        }
      }
    }
  }
}

