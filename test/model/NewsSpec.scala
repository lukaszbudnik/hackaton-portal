package model

import org.specs2.mutable.Specification
import org.squeryl.PrimitiveTypeMode.{transaction, inTransaction, from, select, where}
import play.api.test.FakeApplication
import play.api.test.Helpers.{running,inMemoryDatabase}
import java.util.Date
import org.specs2.specification.BeforeExample
import org.specs2.specification.AfterExample
import org.specs2.specification.BeforeContextExample
import java.text.SimpleDateFormat

class NewsSpec extends Specification {

  "News model" should {
    "be insertable" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val news: News = new News("title", "text", "", 1L, new Date(), None)
          News.insert(news)
          
          news.isPersisted must beTrue
          news.id must beGreaterThan(0L)
        }
      }
    }
    "be insertable with labels" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val news: News = new News("title", "text", "", 1L, new Date(), None)
          News.insert(news)
          
          news.isPersisted must beTrue
          news.id must beGreaterThan(0L)
          
          // retrieve test labels
          val label1 = Label.lookupByValue("test_label_1")
          val label2 = Label.lookupByValue("test_label_2")
          
          label1.isEmpty must beFalse
          label2.isEmpty must beFalse

          val labels = Seq(label1.get, label2.get)
          
	      labels.foreach {l =>
	          news.addLabel(l)
          }
          
          val newsDb = News.lookup(news.id).get
          
          newsDb.labels.size must equalTo(labels.size)
          
        }
      }
    }
    "be retrivable by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val news: News = new News("title", "text", "", 1L, new Date(), None)
          News.insert(news)
          
          news.isPersisted must beTrue

          val newsDb: Option[News] = News.lookup(news.id)

          newsDb.isEmpty must beFalse
          newsDb.get.id must equalTo(news.id)
        }
      }
    }
    "be retrivable with author relationship" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val news: News = new News("title", "text", "", 1L, new Date(), None)
          News.insert(news)
          
          news.isPersisted must beTrue

          val newsDb: Option[News] = News.lookup(news.id)

          newsDb.isEmpty must beFalse
          newsDb.get.author.name must not beNull
        }
      }
    }
    "be retrivable in bulk" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          
          News.insert(new News("title", "text", "", 1L, new Date(), None))
          News.insert(new News("title", "text", "", 1L, new Date(), None))
          
          val newsList: Iterable[News] = News.all
          newsList must have size(6)
        }
      }
    }
    "be sortable by date" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          
          val date1 = new SimpleDateFormat("yyy-MM-dd").parse("2012-01-01")
          News.insert(new News("title", "text", "", 1L, date1, None))
          
          val date2 = new SimpleDateFormat("yyy-MM-dd").parse("2012-02-02")
          News.insert(new News("title", "text", "", 1L, date2, None))
          
          val newsList: List[News] = News.all.toList.sortWith((n1, n2) => n1.publishedDate.after(n2.publishedDate))
          
          val newsList2: List[News] = News.all.toList
          
          newsList must equalTo(newsList2)
        }
      }
    }
  }
}

