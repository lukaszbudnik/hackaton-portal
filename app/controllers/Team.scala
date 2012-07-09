package controllers

import org.squeryl.PrimitiveTypeMode._

import model.Model
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.data._
import play.api.mvc._

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
      request.user.roles.exists(r => r == "admin")
      val team = model.Team("", request.user.hackathonUserId, 0)
      Ok(views.html.teams.create(teamForm.fill(team), Model.users.toList, Model.hackathons.toList, Model.problems.toList, request.user))
    }
  }

  def save = SecuredAction() { implicit request =>
    teamForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.teams.create(errors, Model.users.toList, Model.hackathons.toList, Model.problems.toList, request.user))
      },
      team => transaction {
        // insert team
        model.Team.teams.insert(team)
        // add creator as a member
        team.members.associate(team.creator.head)
        Redirect(routes.Team.index).flashing("status" -> "added", "title" -> team.name)
      })
  }

  def edit(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Team.lookup(id).map {
        team =>
          helpers.Security.verifyIfAllowed(team.creatorId)(request.user)
          Ok(views.html.teams.edit(id, teamForm.fill(team), Model.users.toList, Model.hackathons.toList, Model.problems.toList, request.user))
      }.getOrElse {
        // no team found
        Redirect(routes.Team.view(id)).flashing()
      }
    }
  }

  def update(id: Long) = SecuredAction() { implicit request =>
    teamForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.teams.edit(id, errors, Model.users.toList, Model.hackathons.toList, Model.problems.toList, request.user))
      },
      team => transaction {
        helpers.Security.verifyIfAllowed(team.creatorId)(request.user)
        model.Team.teams.update(
          t =>
            where(t.id === id)
              set (
                t.name := team.name,
                t.creatorId := team.creatorId,
                t.hackathonId := team.hackathonId,
                t.problemId := team.problemId))
        Redirect(routes.Team.index).flashing("status" -> "updated", "title" -> team.name)
      })
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Team.teams.deleteWhere(team => team.id === id)
    }
    Redirect(routes.Team.index).flashing("status" -> "deleted")
  }

  def join(id: Long) = SecuredAction() { implicit request =>
    transaction {
      var status = "error"
      Model.users.lookup(request.user.hackathonUserId).map {
        user =>
          model.Team.lookup(id).map {
            team =>
              if (!team.hasMember(user.id)) {
                team.members.associate(user)
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
      Model.users.lookup(request.user.hackathonUserId).map {
        user =>
          model.Team.lookup(id).map {
            team =>
              team.members.dissociate(user)
              status = "disconnected"
          }
      }
      Redirect(routes.Team.view(id)).flashing("status" -> status)
    }
  }
}