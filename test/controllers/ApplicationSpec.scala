package controllers

package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages

class ApplicationSpec extends Specification {

  "Application" should {

    "show index page" in {
      val result = controllers.Application.index(FakeRequest())

      status(result) must equalTo(OK)
      contentAsString(result) must contain(Messages("home.welcome"))
    }

    "show about page" in {
      val result = controllers.Application.about(FakeRequest())

      status(result) must equalTo(OK)
      contentAsString(result) must contain(Messages("about.title"))
    }

    "show contact page" in {
      val result = controllers.Application.contact(FakeRequest())

      status(result) must equalTo(OK)
      contentAsString(result) must contain(Messages("contact.title"))
    }

    "JS messages, browser first request, miss" in {
      running(FakeApplication()) {
        val req = FakeRequest()
        val result = controllers.Application.jsMessages(req)
        status(result) must equalTo(OK)
        headers(result).contains(ETAG) must beTrue
      }
    }

    "JS messages browser, subsequent request - hit, not modified" in {

      running(FakeApplication()) {
        val req = FakeRequest()
        val result = controllers.Application.jsMessages(req)
        headers(result).get(ETAG).map { etag =>
          val req2 = FakeRequest().withHeaders(IF_NONE_MATCH -> etag)
          val res2 = controllers.Application.jsMessages(req2)
          status(res2) must equalTo(NOT_MODIFIED)
        }.get
      }
    }
  }
  

}