package model

import org.specs2.mutable.Specification
import org.squeryl.PrimitiveTypeMode.{transaction, from, select, where}
import play.api.test.FakeApplication
import play.api.test.Helpers.{running,inMemoryDatabase}
import java.util.Date
import org.specs2.internal.scalaz.Order

class NewsSpec extends Specification {

  "News model" should {
    "be insertable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val news: News = new News(0L, "title", "text", "label1, label2", 1L, new Date())
          Model.news.insert(news)
          news.id must equalTo(1)
        }

      }
    }
    "be retrivable by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val news: News = new News(0L, "title", "text", "label1, label2", 1L, new Date())
          Model.news.insert(news)
          
          val newsDb: Option[News] = Model.lookupNews(news.id)
          news.isPersisted must beTrue
          news must not beNull
        }
      }
    }
    "be retrivable in bulk" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          Model.news.insert(new News(0L, "title", "text", "label1, label2", 1L, new Date()))
          Model.news.insert(new News(0L, "title", "text", "label1, label2", 1L, new Date()))
          
          val newsList: Iterable[News] = Model.allNews
          newsList must not beNull
        }
      }
    }
  }

}

