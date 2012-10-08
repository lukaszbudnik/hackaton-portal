package cache

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import play.api.cache.Cache
import play.api.Play.current

class CacheSpec extends Specification {

  val key = "key"
  val value = "this is a text"
  // in seconds
  val expiration = 100

  "Cache" should {

    "contain newly added values" in {
      running(FakeApplication()) {
        Cache.set(key, value, expiration)

        val valueOption = Cache.getAs[String](key)

        valueOption.isDefined must beTrue

        valueOption must beSome.which { _ == value }
      }
    }

    "return none when item expired" in {
      running(FakeApplication()) {
        Cache.set(key, value, expiration - expiration + 1)

        // expiration set to 1 second
        // wait 2 to be sure it's expired
        Thread.sleep(2000)

        val valueOption = Cache.get(key)

        valueOption.isEmpty must beTrue
      }
    }
  }
}