package cache

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import cache.Cache._
import play.api.Play.current

class CacheSpec extends Specification {

  val key = "key"
  val value = "this is a text"
  // in seconds
  val expiration = 100

  "Cache" should {

    "contain newly added values" in {
      running(FakeApplication()) {

        cached(key) {
          value
        }

        val cachedValue = play.api.cache.Cache.getAs[String](key)

        cachedValue.isDefined must beTrue

        cachedValue must beSome.which { _ == value }
      }
    }

    "return none when item expired" in {
      running(FakeApplication()) {

        cached(key, 1) {
          value
        }

        // expiration set to 1 second
        // wait 2 to be sure it's expired
        Thread.sleep(2000)

        val valueOption = play.api.cache.Cache.get(key)

        valueOption.isEmpty must beTrue
      }
    }
  }
}