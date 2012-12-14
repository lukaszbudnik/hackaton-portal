package controllers

import org.squeryl.PrimitiveTypeMode.inTransaction
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
import helpers.Forms.enum
import model.LocationStatus
import org.squeryl.PrimitiveTypeMode._
import play.api.libs.iteratee.Error

object Location extends LangAwareController {

  val locationForm = Form(
    mapping(
      "id" -> ignored(0L),
      "country" -> nonEmptyText,
      "city" -> nonEmptyText,
      "postalCode" -> nonEmptyText,
      "fullAddress" -> nonEmptyText,
      "name" -> nonEmptyText,
      "latitude" -> helpers.Forms.real,
      "longitude" -> helpers.Forms.real,
      "submitterId" -> ignored(0L),
      "status" -> enum(model.LocationStatus))(model.Location.apply)(model.Location.unapply))

  def index = SecuredAction() { implicit request =>
    inTransaction {
      ensureAdmin {
        val user = userFromRequest(request)
        Ok(views.html.locations.index(model.Location.all, user))
      }
    }

  }

  def view(id: Long) = SecuredAction() { implicit request =>
    inTransaction {
      ensureAdmin {
        val user = userFromRequest(request)
        Ok(views.html.locations.view(model.Location.lookup(id), user))
      }
    }
  }

  def create = SecuredAction() { implicit request =>
    inTransaction {
      Ok(views.html.locations.locationForm(routes.Location.save, locationForm, false))
    }
  }

  def createA = SecuredAction() { implicit request =>
    inTransaction {
      ensureAdmin {
        val user = userFromRequest(request)
        Ok(views.html.locations.create(routes.Location.saveA, locationForm, user))
      }
    }
  }

  def save = SecuredAction() { implicit request =>
    locationForm.bindFromRequest.fold(
      errors => BadRequest(views.html.locations.locationForm(routes.Location.save, errors, false)),
      location => inTransaction {
        val user = userFromRequest(request)
        model.Location.insert(location.copy(submitterId = user.id, status = LocationStatus.Unverified))

        Ok("")
      })
  }

  def saveA = SecuredAction() { implicit request =>
    inTransaction {
      ensureAdmin {
        val user = userFromRequest(request)
        locationForm.bindFromRequest.fold(
          errors => BadRequest(views.html.locations.create(routes.Location.saveA, errors, user)),
          location => {
            model.Location.insert(location.copy(submitterId = user.id))
            Redirect(routes.Location.index).flashing("status" -> "added", "title" -> location.name)
          })
      }
    }
  }

  def edit(id: Long) = SecuredAction() { implicit request =>

    inTransaction {
      model.Location.lookup(id).map { location =>

        ensureAdmin {
          Ok(views.html.locations.locationForm(routes.Location.edit(id), locationForm.fill(location), false))
        }

      }.getOrElse {
        Ok(views.html.locations.locationForm(routes.Location.edit(id), locationForm, false))
      }
    }
  }

  def editA(id: Long) = SecuredAction() { implicit request =>
    inTransaction {
      ensureAdmin {
        val user = userFromRequest(request)
        model.Location.lookup(id).map { location =>
          Ok(views.html.locations.edit(id, routes.Location.updateA(id), locationForm.fill(location), user))
        }.getOrElse {
          Redirect(routes.Location.view(id)).flashing()
        }
      }
    }
  }

  def update(id: Long) = SecuredAction() { implicit request =>
    locationForm.bindFromRequest.fold(
      errors => BadRequest(views.html.locations.locationForm(routes.Location.edit(id), errors, false)),
      location => inTransaction {
        model.Location.lookup(id).map { dbLocation =>
          ensureLocationSubmitterOrAdmin(dbLocation) {
            model.Location.update(id, location)
            Ok("")
          }
        }.getOrElse(Ok(""))
      })
  }

  def updateA(id: Long) = SecuredAction() { implicit request =>
    ensureAdmin {
      val user = userFromRequest(request)
      locationForm.bindFromRequest.fold(
        errors => BadRequest(views.html.locations.edit(id, routes.Location.editA(id), errors, user)),
        location => inTransaction {
          model.Location.update(id, location.copy(submitterId = user.id))
          Redirect(routes.Location.index).flashing("status" -> "updated", "title" -> location.name)
        })
    }
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    inTransaction {
      ensureAdmin {
        model.Location.delete(id)
        Redirect(routes.Location.index).flashing("status" -> "deleted")
      }
    }
  }

  def findByPattern(term: String) = SecuredAction() { implicit request =>
    inTransaction {
      implicit object LocationFormat extends Format[model.Location] {
        def reads(json: JsValue): model.Location = new model.Location()

        def writes(l: model.Location): JsValue = JsObject(List(
          "id" -> JsNumber(l.id),
          "value" -> JsString(l.name),
          "fullAddress" -> JsString(l.fullAddress),
          "country" -> JsString(l.country),
          "submitterId" -> JsNumber(l.submitterId),
          "city" -> JsString(l.city)))
      }

      val locations: List[model.Location] =
        model.User.lookupByOpenId(request.user.id.id + request.user.id.providerId).map { user =>
          model.Location.findByPattern("%" + term + "%",
            (l) => l.status === LocationStatus.Approved or user.id === l.submitterId
              or user.isAdmin === true).toList
        }.getOrElse(Nil)
      Ok(toJson(locations))
    }
  }
}
