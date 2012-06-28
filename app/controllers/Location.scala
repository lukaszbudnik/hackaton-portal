package controllers

import org.squeryl.PrimitiveTypeMode._

import model.Model
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

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
        Ok(views.html.locations.index(Model.locations.toList, request.user))
      }
  }

  def view(id: Long) = UserAwareAction {
    implicit request =>
      transaction {
        Ok(views.html.locations.view(Model.locations.lookup(id), request.user))
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
          Model.locations.insert(location)
          Redirect(routes.Location.index).flashing("status" -> "added", "title" -> location.name)
        }
      )
  }

  def edit(id: Long) = SecuredAction() {
    implicit request =>
      transaction {
        Model.locations.lookup(id).map {
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
          Model.locations.update(l =>
            where(l.id === id)
              set(
              l.name := location.name,
              l.country := location.country,
              l.city := location.city,
              l.postalCode := location.postalCode,
              l.fullAddress := location.fullAddress,
              l.latitude := location.latitude,
              l.longitude := location.longitude))
          Redirect(routes.Location.index).flashing("status" -> "updated", "title" -> location.name)
        })
  }

  def delete(id: Long) = SecuredAction() {
    implicit request =>
      transaction {
        Model.locations.deleteWhere(l => l.id === id)
      }
      Redirect(routes.Location.index).flashing("status" -> "deleted")
  }

}