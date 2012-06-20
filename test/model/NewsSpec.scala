package model

import org.specs2.mutable.Specification
import org.squeryl.PrimitiveTypeMode.{transaction, inTransaction, from, select, where}
import play.api.test.FakeApplication
import play.api.test.Helpers.{running,inMemoryDatabase}
import java.util.Date
import org.specs2.specification.BeforeExample
import org.specs2.specification.AfterExample
import org.specs2.specification.BeforeContextExample

class NewsSpec extends Specification {

  "News model" should {
    "be insertable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        inTransaction {
          val news: News = new News("title", "text", "label1, label2", 1L, new Date())
          Model.news.insert(news)
          
          news.isPersisted must beTrue
          news.id must beGreaterThan(0L)
        }
      }
    }
    "be retrivable by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val news: News = new News("title", "text", "label1, label2", 1L, new Date())
          Model.news.insert(news)
          
          news.isPersisted must beTrue

          val newsDb: Option[News] = Model.lookupNews(news.id)

          newsDb.isEmpty must beFalse
          newsDb.get.id must equalTo(news.id)
        }
      }
    }
    "be retrivable in bulk" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          Model.deleteAllNews()
          
          Model.news.insert(new News("title", "text", "label1, label2", 1L, new Date()))
          Model.news.insert(new News("title", "text", "label1, label2", 1L, new Date()))
          
          val newsList: Iterable[News] = Model.allNews
          newsList must have size(2)
        }
      }
    }
  }
}

