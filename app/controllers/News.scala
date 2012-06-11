package controllers

import java.util.Date
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import play.api.mvc._
import play.api._
import model.Hackathon

object News extends Controller {

  def index = Action {
    /** @TODO is there a better place for this call? Is there any application lifecycle listener in Play? AOP, etc? */
    Hackathon.startDatabaseSession()
    
    var news:model.News = null
    
    transaction {
      news = new model.News("title", "text", "author", new Date())
      Hackathon.news.insert(news)
      
      news.title = "title 2"
      Hackathon.news.update(news)
    }
    
    Ok(views.html.news(news))
  }
  
}