package controllers


import org.specs2.mutable._

import play.api.test.Helpers._
import play.api.test._

class JsMessageFeedSpec extends Specification {

  "JsMessageFeed" should {

    "set ETAG header in response and send the messages to the browser" in {
      running(FakeApplication()) {
        val req = FakeRequest()
        val result = controllers.JsMessagesFeed.jsMessages(req)
        status(result) must equalTo(OK)
        headers(result).contains(ETAG) must beTrue
      }
    }

    "verify IF_NONE_MATCH header and send 304 when the response is identical with the previous ones " in {

      running(FakeApplication()) {
        val req = FakeRequest()
        val result = controllers.JsMessagesFeed.jsMessages(req)
        headers(result).get(ETAG).map { etag =>
          val req2 = FakeRequest().withHeaders(IF_NONE_MATCH -> etag)
          val res2 = controllers.JsMessagesFeed.jsMessages(req2)
          status(res2) must equalTo(NOT_MODIFIED)
        }.get
      }
    }
  }
  

}