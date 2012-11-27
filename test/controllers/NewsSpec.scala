package controllers

import org.specs2.matcher.DataTables
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import play.api.test.FakeRequest$
import helpers.SecureSocialUtils
import core.SecurityAbuseException

class NewsSpec extends Specification with DataTables {

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
        val result = routeAndCall(FakeRequest(GET, "/news/1")).get

        status(result) must equalTo(OK)
        contentAsString(result) must contain(helpers.CmsMessages("news.title"))
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

    "send 404 and display not found on view, edit, update, delete when news does not exists" in {

      "" | "httpMethod" | "action" |
        1 ! GET ! "/news/11111" |
        1 ! GET ! "/news/11111/edit" |
        1 ! POST ! "/news/11111" |
        1 ! POST ! "/news/11111/delete" |
        1 ! GET ! "/hackathons/1/news/11111" |
        1 ! GET ! "/hackathons/1/news/11111/edit" |
        1 ! POST ! "/hackathons/1/news/11111" |
        1 ! POST ! "/hackathons/1/news/11111/delete" |> {
          (justIgnoreMe, httpMethod, action) =>
            {
              val application = FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))
              running(application) {
                val result = SecureSocialUtils.fakeAuth(FakeRequest(httpMethod, action), application)

                status(result) must equalTo(NOT_FOUND)
                contentAsString(result) must contain(helpers.CmsMessages("news.notFound"))
              }
            }
        }
    }
    
    "send 404 and display not found on view, edit, update, delete when hackathon does not exists" in {

      "" | "httpMethod" | "action" |
        1 ! GET ! "/hackathons/11111/news/11111" |
        1 ! GET ! "/hackathons/11111/news/11111/edit" |
        1 ! POST ! "/hackathons/11111/news/11111" |
        1 ! POST ! "/hackathons/11111/news/11111/delete" |> {
          (justIgnoreMe, httpMethod, action) =>
            {
              val application = FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))
              running(application) {
                val result = SecureSocialUtils.fakeAuth(FakeRequest(httpMethod, action), application)

                status(result) must equalTo(NOT_FOUND)
                contentAsString(result) must contain(helpers.CmsMessages("hackathons.notFound"))
              }
            }
        }
    }

    "throw SecurityAbuseException when tampering with edit, update, and delete" in {

      "" | "httpMethod" | "action" |
        1 ! GET ! "/news/1/edit" |
        1 ! POST ! "/news/1" |
        1 ! POST ! "/news/1/delete" |
        1 ! GET ! "/hackathons/1/news/3/edit" |
        1 ! POST ! "/hackathons/1/news/3" |
        1 ! POST ! "/hackathons/1/news/3/delete" |> {
          (justIgnoreMe, httpMethod, action) =>
            {
              val application = FakeApplication(additionalConfiguration = inMemoryDatabase() + (("application.secret", "asasasas")))
              running(application) {
                {
                  SecureSocialUtils.fakeAuthNormalUser(FakeRequest(httpMethod, action), application)
                } must throwA[SecurityAbuseException]
              }
            }
        }
    }

  }

}