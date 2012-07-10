package controllers

import org.squeryl.PrimitiveTypeMode._
import model.{Model, Label}
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
      Ok(views.html.news.index(model.News.all, request.user))
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
        BadRequest(views.html.news.create(errors, Model.users.toList, request.user))
      },
      news => transaction {
        model.News.insert(news)

        news.labelsAsString.split(",").map(_.trim().toLowerCase()).distinct.map { label =>
          val dbLabel = Label.findByValue(label).getOrElse(Label.insert(Label(label)))
          news.addLabel(dbLabel)
        }

        Redirect(routes.News.index).flashing("status" -> "added", "title" -> news.title)
      })
  }

  def edit(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.News.lookup(id).map { news =>
        Ok(views.html.news.edit(id, newsForm.fill(news.copy(labelsAsString = news.labels.map(_.value).mkString(","))), Model.users.toList, request.user))
      }.get
    }
  }

  def update(id: Long) = SecuredAction() { implicit request =>
    newsForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.news.edit(id, errors, Model.users.toList, request.user))
      },
      news => transaction {
        
        val dbNews = model.News.lookup(id).get
        
        val existingLabels = dbNews.labels.map(_.value).toSeq
        
        val newLabels = news.labelsAsString.split(",").map(_.trim().toLowerCase()).distinct.toSeq
        
        // collection of model.Label
        val labelsToBeRemoved = existingLabels.diff(newLabels).map(v => dbNews.labels.find(l => l.value == v).get)
        // remove old labels
        labelsToBeRemoved.map { label =>
          dbNews.removeLabel(label)
        }
        
        // collection of strings
        val labelsToBeAdded = newLabels.diff(existingLabels)
        // add new labels
        labelsToBeAdded.map { label =>
          dbNews.addLabel(Label.insert(Label(label)))
        }

        // update the model
        model.News.update(id, news)

        Redirect(routes.News.index).flashing("status" -> "updated", "title" -> news.title)
      })
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.News.delete(id)
    }
    Redirect(routes.News.index).flashing("status" -> "deleted")
  }

}