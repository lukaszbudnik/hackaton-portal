package controllers

import org.squeryl.PrimitiveTypeMode.transaction

import helpers.Forms.enum
import play.api.data.Forms._
import play.api.data.Form
import play.api.mvc.Action
import play.api.mvc.Controller

object Hackathon extends Controller with securesocial.core.SecureSocial {

  def hackathonsJson = Action {
    transaction {
      val hackathons = model.Hackathon.all.toList
      Ok(com.codahale.jerkson.Json.generate(hackathons)).as(JSON)
    }
  }

  val hackathonForm = Form(
    mapping(
      "subject" -> nonEmptyText,
      "status" -> enum(model.HackathonStatus),
      "submitterId" -> longNumber,
      "locationId" -> longNumber)(model.Hackathon.apply)(model.Hackathon.unapply))

  def index = UserAwareAction {
    implicit request =>
      transaction {
        Ok(views.html.hackathons.index(model.Hackathon.all.toList, request.user))
      }
  }

  def view(id: Long) = UserAwareAction {
    implicit request =>
      transaction {
        Ok(views.html.hackathons.view(model.Hackathon.lookup(id), model.News.all(id), request.user))
      }
  }

  def create = SecuredAction() {
    implicit request =>
      transaction {
        Ok(views.html.hackathons.create(hackathonForm, model.User.all.toList, model.Location.all.toList, request.user))
      }
  }

  def save = SecuredAction() {
    implicit request =>
      hackathonForm.bindFromRequest.fold(
        errors => transaction {
          BadRequest(views.html.hackathons.create(errors, model.User.all.toList, model.Location.all.toList, request.user))
        },
        hackathon => transaction {
          model.Hackathon.insert(hackathon)
          Redirect(routes.Hackathon.index).flashing("status" -> "added", "title" -> hackathon.subject)
        }
      )
  }

  def edit(id: Long) = SecuredAction() {
    implicit request =>
      transaction {
        model.Hackathon.lookup(id).map {
          hackathon =>
            Ok(views.html.hackathons.edit(id, hackathonForm.fill(hackathon), model.User.all.toList, model.Location.all.toList, request.user))
        }.get
      }
  }

  def update(id: Long) = SecuredAction() {
    implicit request =>
      hackathonForm.bindFromRequest.fold(
        errors => transaction {
          BadRequest(views.html.hackathons.edit(id, errors, model.User.all.toList, model.Location.all.toList, request.user))
        },
        hackathon => transaction {
          model.Hackathon.update(id, hackathon)
          Redirect(routes.Hackathon.index).flashing("status" -> "updated", "title" -> hackathon.subject)
        })
  }

  def delete(id: Long) = SecuredAction() {
    implicit request =>
      transaction {
        model.Hackathon.delete(id)
      }
      Redirect(routes.Hackathon.index).flashing("status" -> "deleted")
  }
}