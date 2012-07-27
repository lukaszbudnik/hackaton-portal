package controllers

import org.squeryl.PrimitiveTypeMode.transaction
import play.api.data.Forms._
import play.api.data.Form
import play.api.mvc.Controller
import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import play.api.libs.json.Format
import play.api.libs.json.JsNumber
import play.api.libs.json.JsString
import play.api.libs.json.Json._
import play.api.libs.json.Json._
import play.api.mvc.Action
import play.api.Logger
import core.LangAwareController

object Location extends LangAwareController with securesocial.core.SecureSocial {

  val locationForm = Form(
    mapping(
      "country" -> nonEmptyText,
      "city" -> nonEmptyText,
      "postalCode" -> nonEmptyText,
      "fullAddress" -> nonEmptyText,
      "name" -> nonEmptyText,
      "latitude" -> helpers.Forms.real,
      "longitude" -> helpers.Forms.real)(model.Location.apply)(model.Location.unapply))

  def index = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.locations.index(model.Location.all.toList, request.user))
    }
  }

  def view(id: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.locations.view(model.Location.lookup(id), request.user))
    }
  }

  def create = SecuredAction() { implicit request =>
    transaction {
      Ok(views.html.locations.locationForm(routes.Location.save, locationForm))
    }
  }

  def createInWindow = SecuredAction() { implicit request =>
    transaction {
      Ok(views.html.locations.locationForm(routes.Location.save, locationForm))
    }
  }

  def save = SecuredAction() { implicit request =>
    locationForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.locations.locationForm(routes.Location.save, errors))
      },
      location => transaction {
        model.Location.insert(location)
        Ok("")
      })
  }

  def edit(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Location.lookup(id).map { location =>
        Ok(views.html.locations.edit(id, locationForm.fill(location), request.user))
      }.getOrElse {
        // no location found
        Redirect(routes.Location.view(id)).flashing()
      }
    }
  }

  def update(id: Long) = SecuredAction() { implicit request =>
    locationForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.locations.edit(id, errors, request.user))
      },
      location => transaction {
        model.Location.update(id, location)
        Redirect(routes.Location.index).flashing("status" -> "updated", "title" -> location.name)
      })
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Location.delete(id)
    }
    Redirect(routes.Location.index).flashing("status" -> "deleted")
  }

  def findByPattern(term: String) = Action { implicit request =>
    transaction {
      implicit object LocationFormat extends Format[model.Location] {
        def reads(json: JsValue): model.Location = new model.Location()

        def writes(l: model.Location): JsValue = JsObject(List(
          "id" -> JsNumber(l.id),
          "value" -> JsString(l.name),
          "fullAddress" -> JsString(l.fullAddress),
          "country" -> JsString(l.country),
          "city" -> JsString(l.city)))
      }

      val locations: List[model.Location] = model.Location.findByPattern("%" + term + "%").toList
      Ok(toJson(locations))
    }
  }
}