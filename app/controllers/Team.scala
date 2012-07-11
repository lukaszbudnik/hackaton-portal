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

  def index = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.teams.index(model.Team.all, request.user))
    }
  }

  def view(id: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.teams.view(model.Team.lookup(id), request.user))
    }
  }

  def create = SecuredAction() { implicit request =>
    transaction {
      val team = model.Team("", request.user.hackathonUserId, 0)
      Ok(views.html.teams.create(teamForm.fill(team), model.User.all.toList, model.Hackathon.all.toList, model.Problem.all.toList, request.user))
    }
  }

  def save = SecuredAction() { implicit request =>
    teamForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.teams.create(errors, model.User.all.toList, model.Hackathon.all.toList, model.Problem.all.toList, request.user))
      },
      team => transaction {
        // insert team and add creator as a member
        model.Team.insert(team).addMember(team.creator)
        Redirect(routes.Team.index).flashing("status" -> "added", "title" -> team.name)
      })
  }

  def edit(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Team.lookup(id).map { team =>
        helpers.Security.verifyIfAllowed(team.creatorId, "admin")(request.user)
        Ok(views.html.teams.edit(id, teamForm.fill(team), model.User.all.toList, model.Hackathon.all.toList, model.Problem.all.toList, request.user))
      }.getOrElse {
        // no team found
        Redirect(routes.Team.view(id)).flashing()
      }
    }
  }

  def update(id: Long) = SecuredAction() { implicit request =>
    teamForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.teams.edit(id, errors, model.User.all.toList, model.Hackathon.all.toList, model.Problem.all.toList, request.user))
      },
      team => transaction {
        helpers.Security.verifyIfAllowed(team.creatorId, "admin")(request.user)
        model.Team.update(id, team)
        Redirect(routes.Team.index).flashing("status" -> "updated", "title" -> team.name)
      })
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Team.lookup(id).map { team =>
        helpers.Security.verifyIfAllowed(team.creatorId, "admin")(request.user)
        model.Team.delete(id)
      }
      Redirect(routes.Team.index).flashing("status" -> "deleted")
    }
  }

  def join(id: Long) = SecuredAction() { implicit request =>
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
      Redirect(routes.Team.view(id)).flashing("status" -> status)
    }
  }

  def disconnect(id: Long) = SecuredAction() { implicit request =>
    transaction {
      var status = "error"
      model.User.lookup(request.user.hackathonUserId).map { user =>
        model.Team.lookup(id).map { team =>
          team.deleteMember(user)
          status = "disconnected"
        }
      }
      Redirect(routes.Team.view(id)).flashing("status" -> status)
    }
  }

  def disconnectUser(id: Long, userId: Long) = SecuredAction() { implicit request =>
    transaction {
      var status = "error"
      model.User.lookup(userId).map { user =>
        model.Team.lookup(id).map { team =>
          helpers.Security.verifyIfAllowed(team.creatorId, "admin")(request.user)
          team.deleteMember(user)
          status = "disconnectedUser"
        }
      }
      Redirect(routes.Team.view(id)).flashing("status" -> status)
    }
  }
}