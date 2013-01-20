package cache

import play.api.Play.current

object Cache {

  // in seconds
  val defaultTTL = 1000
  val minTTL = 1

  def cached[A](key: String, ttl: Int = defaultTTL)(a: A)(implicit m: ClassManifest[A]): A = {

    play.api.cache.Cache.get(key) match {
      case Some(a) if m.erasure.isAssignableFrom(a.getClass) => a.asInstanceOf[A]
      case _ => {
        play.api.cache.Cache.set(key, a, ttl)
        a
      }
    }

  }

  def updateCache(key: String, entry: Any, ttl: Int = defaultTTL) = {
    play.api.cache.Cache.set(key, entry, ttl)
  }

  /**
   * There is no invalidation in Play
   * invalidate by setting the value to None
   * cached function takes that into account (if a != None)
   */
  def invalidateCache(key: String) = {
    play.api.cache.Cache.set(key, None, minTTL)
  }

}
