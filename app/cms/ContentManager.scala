package cms

import play.api.Application
import play.api.Play.current
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoURI
import com.novus.salat._
import com.novus.salat.global._
import cms.dto.Entry
import cache.Cache._
import cms.dto.EntryType

object ContentManager {

  val defaultLanguage = "en"

  import play.api.Play.current

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

  def remove(entry: Entry) = {
    entries.remove(grater[Entry].asDBObject(entry))
    invalidateCache("entries_" + entry.key)
    invalidateCache("entries_all")
    invalidateCache("entries_filtered_" + entry.entryType)
  }

  def create(entry: Entry) = {
    entries += grater[Entry].asDBObject(entry)
    invalidateCache("entries_all")
    invalidateCache("entries_filtered_" + entry.entryType)
  }

  def update(entry: Entry) = {
    val q = MongoDBObject("key" -> entry.key)
    val oldEntry = entries.findOne(q).get
    entries.update(oldEntry, grater[Entry].asDBObject(entry))
    // update cache with newly updated entry
    val fullKey = "entries_" + entry.key
    updateCache(fullKey, Some(entry))
  }

  def find(key: String) = {
    cached(key) {
      val q = MongoDBObject("key" -> key)
      entries.findOne(q).map(grater[Entry].asObject(_))
    }
  }

  def all = {
    cached("all") {
      entries.map(grater[Entry].asObject(_)).toList
    }
  }

  def filtered(entryType: EntryType.Value) = {
    cached("filtered_" + entryType) {
      val q = MongoDBObject("entryType" -> entryType.toString())
      entries.find(q).map(grater[Entry].asObject(_)).toList
    }
  }

}
