package controllers

import java.util.Date
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import play.api.mvc._
import play.api._
import model.Model
import play.api.data._
import play.api.data.Forms._
import scala.collection.mutable.ListBuffer

object News extends Controller {

  val newsForm = Form(
    mapping(
      "title" -> nonEmptyText,
      "text" -> nonEmptyText,
      "authorId" -> play.api.data.Forms.longNumber,
      "published" -> date("dd/MM/yyyy"))(model.News.apply)(model.News.unapply))

  def index = Action { implicit request =>
    var newsList: List[model.News] = null

    transaction {
      newsList = Model.news.seq.toList
    }

    Ok(views.html.news.index(newsList))
  }
  
  def view(id: Long) = TODO

  def newNews = Action { implicit request =>
    var users: List[model.User] = null

    transaction {
      users = Model.users.seq.toList
    }

    Ok(views.html.news.newNews(newsForm, users))
  }

  def create = Action {
    implicit request =>
      newsForm.bindFromRequest.fold(
        errors => {
          var users: List[model.User] = null

          transaction {
            users = Model.users.seq.toList
          }
          
          BadRequest(views.html.news.newNews(errors, users))
        },
        news => {
          transaction {
            Model.news.insert(news)
          }
          Redirect(routes.News.index).flashing("status" -> "News added")
        })
  }
  
  def edit(id: Long) = TODO
  
  def update(id: Long) = TODO

  def delete(id: Long) = Action {

    transaction {
      Model.news.deleteWhere(n => n.id === id)
    }

    Redirect(routes.News.index).flashing("status" -> "News deleted")
  }

}