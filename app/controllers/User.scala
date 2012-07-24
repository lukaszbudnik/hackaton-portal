package controllers

import play.api.Play.current
import play.api.mvc.Action
import play.api.mvc.Controller
import org.squeryl.PrimitiveTypeMode.transaction
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

object User extends Controller with securesocial.core.SecureSocial {

  def index(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    transaction {
      
      println(page)
      println(orderBy)
      println(filter)
      
      Ok(views.html.users.index(model.User.sortedBy(orderBy, filter), None, orderBy, filter))
    }
  }

  def update(userId: Int, isAdmin: Boolean) = Action { implicit request =>
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

}