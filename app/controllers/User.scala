package controllers

import play.api.Play.current
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import org.squeryl.PrimitiveTypeMode.transaction
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import model.Page
import helpers.Security
import core.LangAwareController

object User extends LangAwareController with securesocial.core.SecureSocial {
  
  val userForm = Form(
    mapping(
      "name" -> text,
      "email" -> text
    )
    ((name, email) => model.User(name, email, "", "", "", "", false, false))
    ((user: model.User) => Some(user.name, user.email))
  )

  private val PAGE_SIZE = 20

  def index(page: Int, orderBy: Int, filter: String) = SecuredAction() { implicit request =>
    transaction {
      
      implicit val socialUser = request.user
      Security.verifyIfAllowed(socialUser)
      
      val offset = PAGE_SIZE * page
      
      val totalUsers = model.User.pagedUsersTotalNumber(filter)
      
      val users = model.User.pagedUsers(orderBy, filter, offset, PAGE_SIZE)
      
      val currentPage = Page(users, page, offset, totalUsers)
      
      Ok(views.html.users.index(currentPage, socialUser, orderBy, filter))
    }
  }

  def updateIsAdmin(userId: Int, isAdmin: Boolean) = SecuredAction() { implicit request =>
    transaction {
      
      implicit val socialUser = request.user
      Security.verifyIfAllowed(socialUser)
      
      val user = model.User.lookup(userId)
      
      user.map { u =>
        val userToBeUpdated = u.copy(isAdmin = isAdmin)
        model.User.update(userId, userToBeUpdated)
        Ok(JsArray(Seq(JsObject(List(
        	"status" -> JsString("ok"))))))
      }.getOrElse {
        NotFound(JsArray(Seq(JsObject(List(
        	"status" -> JsString("error"))))))
      }
    }
  }
    
  def updateIsBlocked(userId: Int, isBlocked: Boolean) = SecuredAction() { implicit request =>
    transaction {
      
      implicit val socialUser = request.user
      Security.verifyIfAllowed(socialUser)
      
      val user = model.User.lookup(userId)
      
      user.map { u =>
        val userToBeUpdated = u.copy(isBlocked = isBlocked)
        model.User.update(userId, userToBeUpdated)
        Ok(JsArray(Seq(JsObject(List(
        	"status" -> JsString("ok"))))))
      }.getOrElse {
        NotFound(JsArray(Seq(JsObject(List(
        	"status" -> JsString("error"))))))
      }
    }
  }

  def profile = SecuredAction() { 
    implicit request => transaction {
      val id = request.user.hackathonUserId;
      val user = model.User.lookup(id)
      user.map { u =>
        Ok(views.html.users.profile(userForm.fill(u), request.user))
      }.getOrElse {
        Redirect(securesocial.controllers.routes.LoginPage.login).flashing()
      }
    }
  }
}