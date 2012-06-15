package model

import org.specs2.mutable._

import org.squeryl.PrimitiveTypeMode._

import play.api.test._
import play.api.test.Helpers._

class NewsSpec extends Specification {

  "News model" should {
    "be insertable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val news: model.News = new model.News("title", "text", 1l, new Date)
        transaction {
          Hackathon.news.insert(news)
        }
        news.id must equalTo(1)
      }
    }
    "be retrivable by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
	        val Some(news) = Hackathon.news.find(1l)
	        news must notBeNull
        }
      }
    }
  }

}

