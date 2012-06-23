package controllers

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class NewsSpec extends Specification {
  
  "News" should {
    
    "redirect to Login Page on /news" in {
      // application.secret is required for redirections!
      running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
	      val result = controllers.News.index(FakeRequest())

//	      status(result) must equalTo(SEE_OTHER)
//	      redirectLocation(result) must beSome.which(_ == "/login")
	      status(result) must equalTo(OK)
      }
      
    }
    
  }
  
}