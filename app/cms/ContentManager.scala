package cms

import play.api.Application
import play.api.Play.current
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.global._
import cms.dto.Entry
import play.api.cache.Cached
import play.api.cache.Cache
import cms.dto.EntryType

object ContentManager {

  val defaultLanguage = "en"
  // in seconds
  val defaultTTL = 1000

  import play.api.Play.current
  import com.mongodb.casbah.Imports._
  import com.mongodb.casbah.MongoURI

  private val fullParsingRegex = """mongodb://(.+)[:](.+)[@](.+)[:](\d+)[/](.+)""".r
  private val simpleParsingRegex = """mongodb://(.+)[/](.+)""".r

  lazy val mongoDB = {
    val mongoUri = current.configuration.getString("mongodb.uri").getOrElse {
      throw new RuntimeException("mongodb.uri could not be resolved")
    }
    mongoUri match {
      case fullParsingRegex(username, password, server, port, database) => {
        val db = MongoConnection(MongoURI(mongoUri))(database)
        db.authenticate(username, password)
        db
      }
      case simpleParsingRegex(server, database) => MongoConnection(MongoURI(mongoUri))(database)
      case _ => throw new RuntimeException("Not able to parse mongodb.uri")
    }
  }

  private lazy val entries = mongoDB("entries")

  def cache[A](key: String)(f: => A): A = {
    val fullKey = "entries_" + key

    Cache.get(fullKey) match {
      case Some(e: A) if e != None => {
        e
      }
      case _ => {
        val e = f
        Cache.set(fullKey, e, defaultTTL)
        e
      }
    }
  }

  def remove(entry: Entry) = {
    entries.remove(grater[Entry].asDBObject(entry))
    // invalidate cache
    println("invalidate cache")
    Cache.set("entries_" + entry.key, None, defaultTTL)
    Cache.set("entries_all", None, defaultTTL)
    Cache.set("entries_filtered_" + entry.entryType, None, defaultTTL)
  }

  def create(entry: Entry) = {
    entries += grater[Entry].asDBObject(entry)
    Cache.set("entries_all", None, defaultTTL)
    Cache.set("entries_filtered_" + entry.entryType, None, defaultTTL)
  }

  def update(entry: Entry) = {
    val q = MongoDBObject("key" -> entry.key)
    val oldEntry = entries.findOne(q).get
    entries.update(oldEntry, grater[Entry].asDBObject(entry))
    // update cache with newly updated entry
    val fullKey = "entries_" + entry.key
    Cache.set(fullKey, Some(entry), defaultTTL)
  }

  def find(key: String) = {
    cache(key) {
      val q = MongoDBObject("key" -> key)
      entries.findOne(q).map(grater[Entry].asObject(_))
    }
  }

  def all = {
    cache("all") {
      entries.map(grater[Entry].asObject(_)).toList
    }
  }

  def filtered(entryType: EntryType.Value) = {
    cache("filtered_" + entryType) {
      val q = MongoDBObject("entryType" -> entryType.toString())
      entries.find(q).map(grater[Entry].asObject(_)).toList
    }
  }

}