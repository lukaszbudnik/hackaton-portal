package model

import org.specs2.mutable.Specification
import org.squeryl.PrimitiveTypeMode.{transaction, from, select, where}
import play.api.test.FakeApplication
import play.api.test.Helpers.{running,inMemoryDatabase}
import java.util.Date

class NewsSpec extends Specification {

  "News model" should {
    "be insertable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val news: News = new News("title", "text", 1L, new Date())
          Model.news.insert(news)
          news.id must equalTo(1)
        }

      }
    }
    "be retrivable by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          var news: News = new News("title", "text", 1L, new Date())
          Model.news.insert(news)
          
          news = Model.lookupNews(news.id)
          news must not beNull
        }
      }
    }
  }

}

