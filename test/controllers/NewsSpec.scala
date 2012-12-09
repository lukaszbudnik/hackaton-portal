package controllers

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import play.api.test.FakeRequest$
import org.squeryl.PrimitiveTypeMode.transaction

class NewsSpec extends Specification {

  "News controller" should {

    "display all news on GET /news" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val result = controllers.News.index(FakeRequest())

        status(result) must equalTo(OK)
        contentAsString(result) must contain(helpers.CmsMessages("news.title"))
      }
    }

    "display news on GET /news/1" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transaction {
          val dbNews = model.News.lookup(1).get

          val result = routeAndCall(FakeRequest(GET, "/news/1")).get

          status(result) must equalTo(OK)
          contentAsString(result) must contain(dbNews.title)
        }
      }
    }

    "redirect to Login Page on GET /news/new (controllers.News.create)" in {
      // application.secret is required for redirections!
      running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
        val result = routeAndCall(FakeRequest(GET, "/news/new")).get

        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/login")
      }
    }

    "redirect to Login Page on POST /news (controllers.News.save)" in {
      // application.secret is required for redirections!
      running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
        val result = routeAndCall(FakeRequest(POST, "/news")).get

        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/login")
      }
    }

    "redirect to Login Page on GET /news/12/edit (controllers.News.edit)" in {
      // application.secret is required for redirections!
      running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
        val result = routeAndCall(FakeRequest(GET, "/news/12/edit")).get

        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/login")
      }
    }

    "redirect to Login Page on POST /news/12 (controllers.News.update)" in {
      // application.secret is required for redirections!
      running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
        val result = routeAndCall(FakeRequest(POST, "/news/12")).get

        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/login")
      }
    }

    "redirect to Login Page on POST /news/12/delete (controllers.News.delete)" in {
      // application.secret is required for redirections!
      running(FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))) {
        val result = routeAndCall(FakeRequest(POST, "/news/12/delete")).get

        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/login")
      }
    }

  }

}
