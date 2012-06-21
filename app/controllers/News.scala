package controllers

import org.squeryl.PrimitiveTypeMode._
import play.api.mvc._
import model.Model
import play.api.data._
import play.api.data.Forms._

object News extends Controller with securesocial.core.SecureSocial {

  val newsForm = Form(
    mapping(
      "title"     -> nonEmptyText,
      "text"      -> nonEmptyText,
      "labels"    -> nonEmptyText,
      "authorId"  -> longNumber,
      "published" -> date("dd/MM/yyyy")
    )(model.News.apply)(model.News.unapply)
  )

  def index = SecuredAction() { implicit request =>
    transaction {
      val users:Map[Long, String] = Model.users.toList.map({ u => (u.id, u.name) }).toMap
      Ok(views.html.news.index(Model.news.toList, users))
    }
  }
  
  def view(id: Long) = Action { implicit request =>
    transaction {
      val users:Map[Long, String] = Model.users.toList.map({ u => (u.id, u.name) }).toMap
      Ok(views.html.news.view(Model.news.lookup(id), users))
    }
  }

  def create = Action { implicit request =>
    transaction {
      Ok(views.html.news.create(newsForm, Model.users.toList))
    }
  }

  def save = Action { implicit request =>
    newsForm.bindFromRequest.fold(
      errors =>  transaction {
        BadRequest(views.html.news.create(errors, Model.users.toList))
      },
      news => transaction {
        Model.news.insert(news)
        Redirect(routes.News.index).flashing("status" -> "newsInserted",
        									 "title" -> news.title)
      }
    )
  }
  
  def edit(id: Long) = Action { implicit request =>
    transaction {
      Model.news.lookup(id).map { news =>
        Ok(views.html.news.edit(id, newsForm.fill(news), Model.users.toList))
      }.get
      //.getOrElse(NotFound)
    }
  }
  
  def update(id: Long) = Action { implicit request =>
    newsForm.bindFromRequest.fold(
      errors =>  transaction {
        BadRequest(views.html.news.edit(id, errors, Model.users.toList))
      },
      news => transaction {
        Model.news.update(n =>
          where(n.id === id)
          set(
              n.title := news.title,
              n.text := news.text,
              n.labels := news.labels,
              n.authorId := news.authorId,
              n.published := news.published
          )
		)
        Redirect(routes.News.index).flashing("status" -> "newsUpdated",
        									 "title" -> news.title)
      }
    )
  }

  def delete(id: Long) = Action {
    transaction {
      Model.news.deleteWhere(n => n.id === id)
    }
    Redirect(routes.News.index).flashing("status" -> "newsDeleted")
  }

}