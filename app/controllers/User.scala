package controllers

import play.api.Play.current
import play.api.mvc.Action
import play.api.mvc.Controller
import org.squeryl.PrimitiveTypeMode.transaction
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import model.Page

object User extends Controller with securesocial.core.SecureSocial {
  
  private val pageSize = 20

  def index(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    transaction {
      
      val offset = pageSize * page
      
      val totalUsers = model.User.pagedUsersTotalNumber(filter)
      
      val users = model.User.pagedUsers(orderBy, filter, offset, pageSize)
      
      val currentPage = Page(users, page, offset, totalUsers)
      
      Ok(views.html.users.index(currentPage, None, orderBy, filter))
    }
  }

  def updateIsAdmin(userId: Int, isAdmin: Boolean) = Action { implicit request =>
    transaction {
      
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
    
    def updateIsBlocked(userId: Int, isBlocked: Boolean) = Action { implicit request =>
    transaction {
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

}