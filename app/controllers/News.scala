package controllers

import java.util.Date
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import play.api.mvc._
import play.api._
import model.Hackathon

object News extends Controller {

  def index = Action {

    val news = model.News("title", "text", "author", new Date())

    transaction {
      Hackathon.news.insert(news)

      Hackathon.news.update(n =>
        where(n.id === news.id)
        set(n.title := "my new title")
      )

      Ok(views.html.news(Hackathon.news.get(news.id)))
    }
  }
}