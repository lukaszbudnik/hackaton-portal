package controllers

import com.sun.syndication.feed.synd.SyndFeedImpl
import play.api.Play.current
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.RequestHeader
import org.squeryl.PrimitiveTypeMode.transaction
import java.util.ArrayList
import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.feed.synd.SyndEntryImpl
import com.sun.syndication.feed.synd.SyndContentImpl
import com.sun.syndication.io.SyndFeedOutput
import com.sun.syndication.feed.synd.SyndCategory
import com.sun.syndication.feed.synd.SyndCategoryImpl
import com.sun.syndication.feed.synd.SyndImage
import com.sun.syndication.feed.synd.SyndImageImpl

object Feed extends Controller {

  def generateFeed(feed: String, entries: ArrayList[SyndEntry], categories: ArrayList[SyndCategory], externalUrl: String) = {

    val feedType = getAllowedFeedType(feed)

    val syndFeed = new SyndFeedImpl()
    syndFeed.setFeedType(feedType)
    syndFeed.setTitle("hackaton.pl")
    syndFeed.setLink(externalUrl)
    syndFeed.setDescription("hackaton.pl")
    syndFeed.setCopyright("hackaton.pl")
    val syndImage = new SyndImageImpl()
    syndImage.setTitle("hackaton.pl")
    syndImage.setUrl(helpers.URL.externalUrl(routes.Assets.at("images/logo.png")))
    syndFeed.setImage(syndImage)

    syndFeed.setEntries(entries)
    syndFeed.setCategories(categories)

    val output = new SyndFeedOutput()
    val feedDocument = output.outputString(syndFeed, true)

    Ok(feedDocument).withHeaders(CONTENT_TYPE -> "application/xml; charset=utf-8")

  }

  def news(feed: String) = Action { implicit request =>
    transaction {
      val (entries, allCategories) = generateEntriesAndCategories(model.News.all)
      generateFeed(feed, entries, allCategories, helpers.URL.externalUrl(routes.News.index))
    }
  }

  def newsH(hid: Long, feed: String) = Action { implicit request =>
    transaction {
      val (entries, allCategories) = generateEntriesAndCategories(model.Hackathon.lookup(hid).get.news, true)
      generateFeed(feed, entries, allCategories, helpers.URL.externalUrl(routes.News.indexH(hid)))
    }
  }

  def hackathons(feed: String) = Action { implicit request =>
    transaction {
      val entries = new ArrayList[SyndEntry]
      val allCategories = new ArrayList[SyndCategory]

      model.Hackathon.all.foreach { h =>

        val entry = new SyndEntryImpl()

        entry.setTitle(h.subject)
        entry.setLink(helpers.URL.externalUrl(routes.Hackathon.view(h.id)))

        entry.setPublishedDate(h.date)
        entry.setAuthor(h.organiser.name)

        val description = new SyndContentImpl()
        description.setType("text/html")
        description.setValue(h.description)
        entry.setDescription(description)

        val categories = new ArrayList[SyndCategory]
        val category = new SyndCategoryImpl()
        category.setName(h.status.toString)
        categories.add(category)

        allCategories.addAll(categories)
        entry.setCategories(categories)

        entries.add(entry)
      }

      generateFeed(feed, entries, allCategories, helpers.URL.externalUrl(routes.Hackathon.index))
    }
  }

  private def getAllowedFeedType(feed: String) = {
    val allowedFeedTypes: Map[String, String] = Map(("atom" -> "atom_1.0"), ("rss" -> "rss_2.0"))
    allowedFeedTypes.getOrElse(feed, "atom_1.0")
  }

  private def generateEntriesAndCategories(news: Iterable[model.News], shortTitle: Boolean = false) = {
    val entries = new ArrayList[SyndEntry]
    val allCategories = new ArrayList[SyndCategory]
    news.foreach { n =>
      val entry = new SyndEntryImpl()

      n.hackathon.map { hackathon =>
        entry.setLink(helpers.URL.externalUrl(routes.News.viewH(hackathon.id, n.id)))

        if (!shortTitle) {
          entry.setTitle(hackathon.subject + ": " + n.title)
        } else {
          entry.setTitle(n.title)
        }

      }.getOrElse {
        entry.setTitle(n.title)
        entry.setLink(helpers.URL.externalUrl(routes.News.view(n.id)))
      }

      entry.setPublishedDate(n.publishedDate)
      entry.setAuthor(n.author.name)

      val description = new SyndContentImpl()
      description.setType("text/html")
      description.setValue(n.text)
      entry.setDescription(description)

      val categories = new ArrayList[SyndCategory]
      n.labels.foreach { l =>
        val category = new SyndCategoryImpl()
        category.setName(l.value)
        categories.add(category)
      }
      allCategories.addAll(categories)
      entry.setCategories(categories)

      entries.add(entry)
    }
    (entries, allCategories)
  }

}