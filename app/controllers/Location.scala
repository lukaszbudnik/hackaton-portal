package controllers

import org.squeryl.PrimitiveTypeMode.transaction

import play.api.data.Forms._
import play.api.data.Form
import play.api.mvc.Controller

object Location extends Controller with securesocial.core.SecureSocial {

  val locationForm = Form(
    mapping(
      "country" -> nonEmptyText,
      "city" -> nonEmptyText,
      "postalCode" -> nonEmptyText,
      "fullAddress" -> nonEmptyText,
      "name" -> nonEmptyText,
      "latitude" -> helpers.Forms.real,
      "longitude" -> helpers.Forms.real)(model.Location.apply)(model.Location.unapply))

  def index = UserAwareAction {
    implicit request =>
      transaction {
        Ok(views.html.locations.index(model.Location.all.toList, request.user))
      }
  }

  def view(id: Long) = UserAwareAction {
    implicit request =>
      transaction {
        Ok(views.html.locations.view(model.Location.lookup(id), request.user))
      }
  }

  def create = SecuredAction() {
    implicit request =>
      transaction {
        Ok(views.html.locations.create(locationForm, request.user))
      }
  }

  def save = SecuredAction() {
    implicit request =>
      locationForm.bindFromRequest.fold(
        errors => transaction {
          BadRequest(views.html.locations.create(errors, request.user))
        },
        location => transaction {
          model.Location.insert(location)
          Redirect(routes.Location.index).flashing("status" -> "added", "title" -> location.name)
        })
  }

  def edit(id: Long) = SecuredAction() {
    implicit request =>
      transaction {
        model.Location.lookup(id).map {
          location =>
            Ok(views.html.locations.edit(id, locationForm.fill(location), request.user))
        }.get
      }
  }

  def update(id: Long) = SecuredAction() {
    implicit request =>
      locationForm.bindFromRequest.fold(
        errors => transaction {
          BadRequest(views.html.locations.edit(id, errors, request.user))
        },
        location => transaction {
          model.Location.update(id, location)
          Redirect(routes.Location.index).flashing("status" -> "updated", "title" -> location.name)
        })
  }

  def delete(id: Long) = SecuredAction() {
    implicit request =>
      transaction {
        model.Location.delete(id)
      }
      Redirect(routes.Location.index).flashing("status" -> "deleted")
  }

}