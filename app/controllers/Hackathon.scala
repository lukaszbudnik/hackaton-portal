package controllers

import org.squeryl.PrimitiveTypeMode.transaction
import helpers.Forms.enum
import play.api.data.Forms.date
import play.api.data.Forms.longNumber
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.data.Form
import play.api.libs.json.Json.toJson
import play.api.mvc.Action
import play.api.mvc.Controller
import core.LangAwareController
import play.api.i18n.Messages

object Hackathon extends LangAwareController with securesocial.core.SecureSocial {

  def hackathonsJson = Action {
    transaction {
      val hackathons = model.Hackathon.all.toList
      Ok(toJson(hackathons))
    }
  }
  
  def hackathonJson(id: Long) = Action {
    transaction {
      val hackathon = model.Hackathon.lookup(id)
      Ok(toJson(hackathon.get))
    }
  }

  val hackathonForm = Form(
    mapping(
      "subject" -> nonEmptyText,
      "status" -> enum(model.HackathonStatus),
      "date" -> date(Messages("global.dateformat")),
      "description" -> nonEmptyText,
      "organizerId" -> longNumber,
      "locationId" -> longNumber,
      "locationName" -> nonEmptyText)(model.Hackathon.apply)(model.Hackathon.unapply))

  def index = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.hackathons.index(model.Hackathon.all, request.user))
    }
  }

  def view(id: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.hackathons.view(model.Hackathon.lookup(id), request.user))
    }
  }
  
  def chat(id: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.hackathons.chat(model.Hackathon.lookup(id), request.user))
    }
  }

  def create = SecuredAction() { implicit request =>
    transaction {
      val hackathon = new model.Hackathon(request.user.hackathonUserId)
      Ok(views.html.hackathons.create(hackathonForm.fill(hackathon), None, request.user))
    }
  }

  def save = SecuredAction() { implicit request =>
    hackathonForm.bindFromRequest.fold(
      errors => transaction {
        val location: Option[model.Location] = hackathonForm.data.get("locationId")
          .map(l => model.Location.lookup(l.toLong).get)
        BadRequest(views.html.hackathons.create(errors, location, request.user))
      },
      hackathon => transaction {
        model.Hackathon.insert(hackathon)
        Redirect(routes.Hackathon.index).flashing("status" -> "added", "title" -> hackathon.subject)
      })
  }

  def edit(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Hackathon.lookup(id).map { hackathon =>
        helpers.Security.verifyIfAllowed(hackathon.organiserId)(request.user)
     	hackathon.locationName = hackathon.location.name
        Ok(views.html.hackathons.edit(id, hackathonForm.fill(hackathon), model.Location.lookup(hackathon.locationId), request.user))
      }.getOrElse {
        // no hackathon found
        Redirect(routes.Hackathon.view(id)).flashing()
      }
    }
  }

  def update(id: Long) = SecuredAction() { implicit request =>
    hackathonForm.bindFromRequest.fold(
      errors => transaction {
        val location: Option[model.Location] = hackathonForm.data.get("locationId")
          .map(l => model.Location.lookup(l.toLong).get)
        BadRequest(views.html.hackathons.edit(id, errors, location, request.user))
      },
      hackathon => transaction {
        model.Hackathon.lookup(id).map { hackathon =>
          helpers.Security.verifyIfAllowed(hackathon.organiserId)(request.user)
        }
        model.Hackathon.update(id, hackathon)
        Redirect(routes.Hackathon.index).flashing("status" -> "updated", "title" -> hackathon.subject)
      })
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Hackathon.lookup(id).map { hackathon =>
        helpers.Security.verifyIfAllowed(hackathon.organiserId)(request.user)
      }
      model.Hackathon.delete(id)
      Redirect(routes.Hackathon.index).flashing("status" -> "deleted")
    }
  }
  
  def join(id: Long) = SecuredAction() { implicit request =>
    transaction {
      var status = "error"
      model.User.lookup(request.user.hackathonUserId).map { user =>
        model.Hackathon.lookup(id).map { hackathon =>
          if (!hackathon.hasMember(user.id)) {
            hackathon.addMember(user)
            status = "joined"
          }
        }
      }
      Redirect(routes.Hackathon.view(id)).flashing("status" -> status)
    }
  }

  def disconnect(id: Long) = SecuredAction() { implicit request =>
    transaction {
      var status = "error"
      model.User.lookup(request.user.hackathonUserId).map { user =>
        model.Hackathon.lookup(id).map { hackathon =>
          hackathon.deleteMember(user)
          status = "disconnected"
        }
      }
      Redirect(routes.Hackathon.view(id)).flashing("status" -> status)
    }
  }

  def disconnectUser(id: Long, userId: Long) = SecuredAction() { implicit request =>
    transaction {
      var status = "error"
      model.User.lookup(userId).map { user =>
        model.Hackathon.lookup(id).map { hackathon =>
          helpers.Security.verifyIfAllowed(hackathon.organiserId)(request.user)
          hackathon.deleteMember(user)
          status = "disconnectedUser"
        }
      }
      Redirect(routes.Hackathon.view(id)).flashing("status" -> status)
    }
  }
}