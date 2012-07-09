package controllers

import org.squeryl.PrimitiveTypeMode._
import model.Model
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import java.util.Date

object News extends Controller with securesocial.core.SecureSocial {

  val newsForm = Form(
    mapping(
      "title" -> nonEmptyText,
      "text" -> nonEmptyText,
      "labelsAsString" -> nonEmptyText,
      "authorId" -> longNumber,
      "published" -> date("dd/MM/yyyy"),
      "hackathonId"  -> optional(longNumber))(model.News.apply)(model.News.unapply))

  def index = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.news.index(model.News.allNewsSortedByDateDesc.toList, request.user))
    }
  }

  def view(id: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.news.view(model.News.lookup(id), request.user))
    }
  }

  def create = SecuredAction() { implicit request =>
    val news = model.News("", "", "", request.user.hackathonUserId, new Date(), None)
    transaction {
      Ok(views.html.news.create(newsForm.fill(news), Model.users.toList, request.user))
    }
  }
  
  def createHackathonNews(hackathonId: Long) = SecuredAction() { implicit request =>
    val hackathonNews = model.News("", "", "", request.user.hackathonUserId, new Date(), Some(hackathonId))
    transaction {
      Ok(views.html.news.create(newsForm.fill(hackathonNews), Model.users.toList, request.user))
    }
  }

  def save = SecuredAction() { implicit request =>
    newsForm.bindFromRequest.fold(
      errors => transaction {
        println("errors!" + errors.errors) 
        BadRequest(views.html.news.create(errors, Model.users.toList, request.user))
      },
      news => transaction {
        model.News.news.insert(news)

        news.labelsAsString.split(",").map(_.trim().toLowerCase()).distinct.map { label =>
          val dbLabel = model.Label.findByValue(label).getOrElse(model.Label.labels.insert(model.Label(label)))
          news.labels.associate(dbLabel)
        }

        Redirect(routes.News.index).flashing("status" -> "added", "title" -> news.title)
      })
  }

  def edit(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.News.news.lookup(id).map { news =>
        Ok(views.html.news.edit(id, newsForm.fill(news), Model.users.toList, request.user))
      }.get
    }
  }

  def update(id: Long) = SecuredAction() { implicit request =>
    newsForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.news.edit(id, errors, Model.users.toList, request.user))
      },
      news => transaction {
        
        // split labels
        // uniq
        // diff with existing
        // fetch from db
        // associate
        // remove old ones
        
        model.News.news.update(n =>
          where(n.id === id)
            set (
              n.title := news.title,
              n.text := news.text,
              n.authorId := news.authorId,
              n.published := news.published))
        Redirect(routes.News.index).flashing("status" -> "updated", "title" -> news.title)
      })
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.News.news.deleteWhere(n => n.id === id)
    }
    Redirect(routes.News.index).flashing("status" -> "deleted")
  }

}