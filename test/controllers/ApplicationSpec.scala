package controllers

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages

class ApplicationSpec extends Specification {

  "Application" should {

    "show index page" in {
      running(FakeApplication()) {
        val result = controllers.Application.index(FakeRequest())

        status(result) must equalTo(OK)
        contentAsString(result) must contain(Messages("home.welcome"))
      }
    }

    "show about page" in {
      running(FakeApplication()) {
        val result = controllers.Application.about(FakeRequest())

        status(result) must equalTo(OK)
        contentAsString(result) must contain(Messages("about.title"))
      }
    }

    "show contact page" in {
      running(FakeApplication()) {
        val result = controllers.Application.contact(FakeRequest())

        status(result) must equalTo(OK)
        contentAsString(result) must contain(Messages("contact.title"))
      }
    }

  }

}