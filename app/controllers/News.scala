package controllers

import org.squeryl.PrimitiveTypeMode._

import model.Model
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

object News extends Controller with securesocial.core.SecureSocial {

  val newsForm = Form(
    mapping(
      "title" -> nonEmptyText,
      "text" -> nonEmptyText,
      "labels" -> nonEmptyText,
      "authorId" -> longNumber,
      "published" -> date("dd/MM/yyyy"))(model.News.apply)(model.News.unapply))

  def index = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.news.index(Model.allNewsSortedByDateDesc.toList, request.user))
    }
  }

  def view(id: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.news.view(Model.news.lookup(id), request.user))
    }
  }

  def create = SecuredAction() { implicit request =>
    transaction {
      Ok(views.html.news.create(newsForm, Model.users.toList, request.user))
    }
  }

  def save = SecuredAction() { implicit request =>
    newsForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.news.create(errors, Model.users.toList, request.user))
      },
      news => transaction {
        Model.news.insert(news)
        Redirect(routes.News.index).flashing("status" -> "added", "title" -> news.title)
      })
  }

  def edit(id: Long) = SecuredAction() { implicit request =>
    transaction {
      Model.news.lookup(id).map { news =>
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
        Model.news.update(n =>
          where(n.id === id)
            set (
              n.title := news.title,
              n.text := news.text,
              n.labels := news.labels,
              n.authorId := news.authorId,
              n.published := news.published))
        Redirect(routes.News.index).flashing("status" -> "updated", "title" -> news.title)
      })
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    transaction {
      Model.news.deleteWhere(n => n.id === id)
    }
    Redirect(routes.News.index).flashing("status" -> "deleted")
  }

}