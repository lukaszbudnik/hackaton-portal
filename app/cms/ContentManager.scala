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

  private val parsingRegex = """mongodb://(.+)[:](.+)[@](.+)[:](\d+)[/](.+)""".r

  private lazy val entries = {
    val mongoUri = current.configuration.getString("mongodb.uri").getOrElse {
      throw new RuntimeException("mongodb.uri could not be resolved")
    }
    mongoUri match {
      case parsingRegex(username, password, server, port, database) => {
        //        MongoConnection(MongoURI(mongoUri))(database)("entries")
        MongoConnection()("test")("entries")
      }
      case _ => throw new RuntimeException("Not able to parse MONGOHQ_URL")
    }
  }

  private def addToCache(key: String, c: Any) = {
    Cache.set("entries_find_" + key, c, 1000)
  }

  def remove(entry: Entry) = {
    entries.remove(grater[Entry].asDBObject(entry))
  }

  def create(entry: Entry) = {
    entries += grater[Entry].asDBObject(entry)
  }

  def update(entry: Entry) = {
    val cacheKey = "entry_" + entry.key

    val q = MongoDBObject("key" -> entry.key)
    val oldEntry = entries.findOne(q).get
    entries.update(oldEntry, grater[Entry].asDBObject(entry))

    addToCache(cacheKey, entry)
  }

  def find(key: String) = {
    val cacheKey = "entry_" + key

    Cache.getAs[Entry](cacheKey) match {
      case c: Entry => Option(c)
      case None => {
        val q = MongoDBObject("key" -> key)
        val opC = entries.findOne(q).map(grater[Entry].asObject(_))
        opC.map {
          c => addToCache(cacheKey, c)
        }
        opC
      }
    }

  }

  def all = {
    Cache.getAs[Seq[Entry]]("entries_all").getOrElse {
      val c = entries.map(grater[Entry].asObject(_)).toSeq
      if (!c.isEmpty) {
        addToCache("entries_all", c)
      }
      c
    }
  }

  def filtered(entryType: EntryType.Value) = {
    Cache.getAs[Seq[Entry]]("entries_filtered_" + entryType).getOrElse {
      val q = MongoDBObject("entryType" -> entryType.toString())
      val c = entries.find(q).map(grater[Entry].asObject(_)).toSeq
      if (!c.isEmpty) {
        addToCache("entries_filtered_" + entryType, c)
      }
      c
    }
  }

}