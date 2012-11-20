/**
 *
 */
package controllers

import org.specs2.mutable.Specification
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import play.api.mvc.SimpleResult
import org.squeryl.PrimitiveTypeMode.transaction

import scala.xml._

class FeedSpec extends Specification {

  "Feed controller" should {
    "Generate proper atom news feed" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val result = routeAndCall(FakeRequest(GET, "/atom/news")).get
          val atomXML = XML.loadString(contentAsString(result))
          (atomXML \\ "title").head.text must equalTo("hackaton.pl")
          (atomXML \\ "entry").size must equalTo(4)
          atomXML.namespace must equalTo("http://www.w3.org/2005/Atom")
          (atomXML \\ "entry" \\ "author").head must beEqualToIgnoringSpace(<author><name>Łukasz Budnik</name></author>)
          
          
          status(result) must equalTo(OK)
        }
      }
    }
  
    "Generate proper rss news feed" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val result = routeAndCall(FakeRequest(GET, "/rss/news")).get
          val atomXML = XML.loadString(contentAsString(result))
          (atomXML \\ "title").head.text must equalTo("hackaton.pl")
          (atomXML \\ "item").size must equalTo(4)
          (atomXML \\ "item" \\ "creator").head must beEqualToIgnoringSpace(<dc:creator>Łukasz Budnik</dc:creator>)
          
          status(result) must equalTo(OK)
        }
      }
    }
  
    "Generate proper atom hackathons feed" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val result = routeAndCall(FakeRequest(GET, "/atom/hackathons")).get
          val atomXML = XML.loadString(contentAsString(result))
          (atomXML \\ "title").head.text must equalTo("hackaton.pl")
          (atomXML \\ "entry").size must equalTo(2)
          atomXML.namespace must equalTo("http://www.w3.org/2005/Atom")
          (atomXML \\ "entry" \\ "author").head must beEqualToIgnoringSpace(<author><name>Łukasz Budnik</name></author>)
          
          status(result) must equalTo(OK)
        }
      }
    }
  
    "Generate proper rss hackathons feed" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val result = routeAndCall(FakeRequest(GET, "/rss/hackathons")).get
          val atomXML = XML.loadString(contentAsString(result))
          (atomXML \\ "title").head.text must equalTo("hackaton.pl")
          (atomXML \\ "item").size must equalTo(2)
          (atomXML \\ "item" \\ "creator").head must beEqualToIgnoringSpace(<dc:creator>Łukasz Budnik</dc:creator>)
          
          status(result) must equalTo(OK)
        }
      }
    }
  
    "Generate proper atom hackathons news feed" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val result = routeAndCall(FakeRequest(GET, "/atom/hackathons/1/news")).get
          println(contentAsString(result))
          val atomXML = XML.loadString(contentAsString(result))
          (atomXML \\ "title").head.text must equalTo("hackaton.pl")
          (atomXML \\ "entry").size must equalTo(2)
          (atomXML \\ "entry" \\ "title").head.text must equalTo("Hackathon News 2")
          atomXML.namespace must equalTo("http://www.w3.org/2005/Atom")
          (atomXML \\ "entry" \\ "author").head must beEqualToIgnoringSpace(<author><name>Łukasz Budnik</name></author>)
          
          status(result) must equalTo(OK)
        }
      }
    }
    
    "Generate proper rss hackathons news feed" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val result = routeAndCall(FakeRequest(GET, "/rss/hackathons/1/news")).get
          val atomXML = XML.loadString(contentAsString(result))
          (atomXML \\ "title").head.text must equalTo("hackaton.pl")
          (atomXML \\ "item").size must equalTo(2)
          (atomXML \\ "item" \\ "title").head.text must equalTo("Hackathon News 2")
          (atomXML \\ "item" \\ "creator").head must beEqualToIgnoringSpace(<dc:creator>Łukasz Budnik</dc:creator>)
          
          status(result) must equalTo(OK)
        }
      }
    }
  }
}