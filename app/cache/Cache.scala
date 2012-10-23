package cache

import play.api.Play.current

object Cache {

  // in seconds
  val defaultTTL = 1000
  val minTTL = 1

  def cached[A](key: String)(f: => A): A = {
    val fullKey = "entries_" + key

    play.api.cache.Cache.get(fullKey) match {
      case Some(e: A) if e != None => e
      case _ => {
        play.api.cache.Cache.set(fullKey, f, defaultTTL)
        f
      }
    }
  }

  def updateCache(key: String, entry: Any) = {
    play.api.cache.Cache.set(key, entry, defaultTTL)
  }

  /**
   * There is no invalidation in Play
   * invalidate by setting the value to None
   * cached function takes that into account
   */
  def invalidateCache(key: String) = {
    play.api.cache.Cache.set(key, None, minTTL)
  }

}