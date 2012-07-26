package controllers

import play.api.Play.current
import play.api.mvc.Action
import play.api.mvc.Controller
import org.squeryl.PrimitiveTypeMode.transaction
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import model.Page
import helpers.Security

object User extends Controller with securesocial.core.SecureSocial {
  
  private val pageSize = 20

  def index(page: Int, orderBy: Int, filter: String) = SecuredAction() { implicit request =>
    transaction {
      
      val socialUser = request.user
      Security.verifyIfAllowed(socialUser.isAdmin)(socialUser)
      
      val offset = pageSize * page
      
      val totalUsers = model.User.pagedUsersTotalNumber(filter)
      
      val users = model.User.pagedUsers(orderBy, filter, offset, pageSize)
      
      val currentPage = Page(users, page, offset, totalUsers)
      
      Ok(views.html.users.index(currentPage, socialUser, orderBy, filter))
    }
  }

  def updateIsAdmin(userId: Int, isAdmin: Boolean) = SecuredAction() { implicit request =>
    transaction {
      
      implicit val socialUser = request.user
      Security.verifyIfAllowed(socialUser.isAdmin)
      
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
      Security.verifyIfAllowed(socialUser.isAdmin)
      
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