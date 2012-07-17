package controllers

import org.squeryl.PrimitiveTypeMode.transaction

import play.api.data.Forms._
import play.api.data.Form
import play.api.mvc.Controller

object Team extends Controller with securesocial.core.SecureSocial {

  val teamForm = Form(
    mapping(
      "name" -> text.verifying("teams.name.error", !_.isEmpty()),
      "creatorId" -> longNumber,
      "hackathonId" -> longNumber,
      "problemId" -> optional(longNumber))(model.Team.apply)(model.Team.unapply))

  def index(hid: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.teams.index(model.Hackathon.lookup(hid), request.user))
    }
  }

  def view(hid: Long, id: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.teams.view(model.Hackathon.lookup(hid), model.Team.lookup(id), request.user))
    }
  }

  def create(hid: Long) = SecuredAction() { implicit request =>
    transaction {
      val hackathon = model.Hackathon.lookup(hid)
      val team = new model.Team(request.user.hackathonUserId, hid)
      Ok(views.html.teams.create(hackathon, teamForm.fill(team), request.user))
    }
  }

  def save(hid: Long) = SecuredAction() { implicit request =>
    teamForm.bindFromRequest.fold(
      errors => transaction {
        val hackathon = model.Hackathon.lookup(hid)
        BadRequest(views.html.teams.create(hackathon, errors, request.user))
      },
      team => transaction {
        // insert team and add creator as a member
        model.Team.insert(team).addMember(team.creator)
        Redirect(routes.Team.index(hid)).flashing("status" -> "added", "title" -> team.name)
      })
  }

  def edit(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Team.lookup(id).map { team =>
        helpers.Security.verifyIfAllowed(team.creatorId, "admin")(request.user)
        Ok(views.html.teams.edit(Some(team.hackathon), id, teamForm.fill(team), request.user))
      }.getOrElse {
        // no team found
        Redirect(routes.Team.view(hid, id)).flashing()
      }
    }
  }

  def update(hid: Long, id: Long) = SecuredAction() { implicit request =>
    teamForm.bindFromRequest.fold(
      errors => transaction {
        val hackathon = model.Hackathon.lookup(hid)
        BadRequest(views.html.teams.edit(hackathon, id, errors, request.user))
      },
      team => transaction {
        helpers.Security.verifyIfAllowed(team.creatorId, "admin")(request.user)
        model.Team.update(id, team)
        Redirect(routes.Team.index(hid)).flashing("status" -> "updated", "title" -> team.name)
      })
  }

  def delete(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Team.lookup(id).map { team =>
        helpers.Security.verifyIfAllowed(team.creatorId, "admin")(request.user)
        model.Team.delete(id)
      }
      Redirect(routes.Team.index(hid)).flashing("status" -> "deleted")
    }
  }

  def join(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      var status = "error"
      model.User.lookup(request.user.hackathonUserId).map { user =>
        model.Team.lookup(id).map { team =>
          if (!team.hasMember(user.id)) {
            team.addMember(user)
            status = "joined"
          }
        }
      }
      Redirect(routes.Team.view(hid, id)).flashing("status" -> status)
    }
  }

  def disconnect(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      var status = "error"
      model.User.lookup(request.user.hackathonUserId).map { user =>
        model.Team.lookup(id).map { team =>
          team.deleteMember(user)
          status = "disconnected"
        }
      }
      Redirect(routes.Team.view(hid, id)).flashing("status" -> status)
    }
  }

  def disconnectUser(hid: Long, id: Long, userId: Long) = SecuredAction() { implicit request =>
    transaction {
      var status = "error"
      model.User.lookup(userId).map { user =>
        model.Team.lookup(id).map { team =>
          helpers.Security.verifyIfAllowed(team.creatorId, "admin")(request.user)
          team.deleteMember(user)
          status = "disconnectedUser"
        }
      }
      Redirect(routes.Team.view(hid, id)).flashing("status" -> status)
    }
  }
}