package controllers

import play.api._
import data.Form
import data.Forms._
import play.api.mvc._
import play.api.i18n._
import model._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column
import helpers.Forms._

object Hackathon extends Controller with securesocial.core.SecureSocial {

  def hackathonsJson = Action {
    transaction {
      val hackathons = Model.hackathons.toList
      Ok(com.codahale.jerkson.Json.generate(hackathons)).as(JSON)
    }
  }

  val hackathonForm = Form(
    mapping(
      "subject" -> nonEmptyText,
      "status" -> enum(HackathonStatus),
      "submitterId" -> longNumber,
      "locationId" -> longNumber)(model.Hackathon.apply)(model.Hackathon.unapply))

  def index = UserAwareAction {
    implicit request =>
      transaction {
        Ok(views.html.hackathons.index(Model.hackathons.toList, request.user))
      }
  }

  def view(id: Long) = UserAwareAction {
    implicit request =>
      transaction {
        val users:Map[Long, String] = Model.users.toList.map({ u => (u.id, u.name) }).toMap
        val locations:Map[Long, String] = Model.locations.toList.map({ l => (l.id, l.name) }).toMap
        Ok(views.html.hackathons.view(Model.hackathons.lookup(id), users, locations, request.user))
      }
  }

  def create = SecuredAction() {
    implicit request =>
      transaction {
        Ok(views.html.hackathons.create(hackathonForm, Model.users.toList, Model.locations.toList, request.user))
      }
  }

  def save = SecuredAction() {
    implicit request =>
      hackathonForm.bindFromRequest.fold(
        errors => transaction {
          BadRequest(views.html.hackathons.create(errors, Model.users.toList, Model.locations.toList, request.user))
        },
        hackathon => transaction {
          Model.hackathons.insert(hackathon)
          Redirect(routes.Hackathon.index).flashing("status" -> "added", "title" -> hackathon.subject)
        }
      )
  }

  def edit(id: Long) = SecuredAction() {
    implicit request =>
      transaction {
        Model.hackathons.lookup(id).map {
          hackathon =>
            Ok(views.html.hackathons.edit(id, hackathonForm.fill(hackathon), Model.users.toList, Model.locations.toList, request.user))
        }.get
      }
  }

  def update(id: Long) = SecuredAction() {
    implicit request =>
      hackathonForm.bindFromRequest.fold(
        errors => transaction {
          BadRequest(views.html.hackathons.edit(id, errors, Model.users.toList, Model.locations.toList, request.user))
        },
        hackathon => transaction {
          Model.hackathons.update(h =>
            where(h.id === id)
              set(
              h.subject := hackathon.subject,
              h.status := hackathon.status,
              h.submitterId := hackathon.submitterId,
              h.locationId := hackathon.locationId))
          Redirect(routes.Hackathon.index).flashing("status" -> "updated", "title" -> hackathon.subject)
        })
  }

  def delete(id: Long) = SecuredAction() {
    implicit request =>
      transaction {
        Model.hackathons.deleteWhere(h => h.id === id)
      }
      Redirect(routes.Hackathon.index).flashing("status" -> "deleted")
  }
}