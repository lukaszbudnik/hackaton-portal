package controllers

import org.squeryl.PrimitiveTypeMode._

import model.Model
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

object Team extends Controller with securesocial.core.SecureSocial {

  val teamForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "creatorId" -> longNumber,
      "hackathonId" -> longNumber,
      "problemId" -> optional(longNumber))(model.Team.apply)(model.Team.unapply))

  def index = UserAwareAction { implicit request =>
    transaction {
      val users: Map[Long, String] = Model.users.toList.map({ u => (u.id, u.name) }).toMap
      Ok(views.html.teams.index(Model.teams.toList, users, request.user))
    }
  }

  def view(id: Long) = UserAwareAction { implicit request =>
    transaction {
      val users: Map[Long, String] = Model.users.toList.map({ u => (u.id, u.name) }).toMap
      Ok(views.html.teams.view(Model.teams.lookup(id), users, request.user))
    }
  }

  def create = SecuredAction() { implicit request =>
    transaction {
      Ok(views.html.teams.create(teamForm, Model.users.toList, Model.hackathons.toList, Model.problems.toList, request.user))
    }
  }

    def save = SecuredAction() { implicit request =>
      teamForm.bindFromRequest.fold(
        errors =>  transaction {
          BadRequest(views.html.teams.create(errors, Model.users.toList, Model.hackathons.toList, Model.problems.toList, request.user))
        },
        team => transaction {
          Model.teams.insert(team)
          Redirect(routes.Team.index).flashing("status" -> "teams.status.added")
        }
      )
    }

  def edit(id: Long) = SecuredAction() { implicit request =>
    transaction {
      Model.teams.lookup(id).map { team =>
        Ok(views.html.teams.edit(id, teamForm.fill(team), Model.users.toList, Model.hackathons.toList, Model.problems.toList, request.user))
      }.get
    }
  }

  def update(id: Long) = SecuredAction() { implicit request =>
    teamForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.teams.edit(id, errors, Model.users.toList, Model.hackathons.toList, Model.problems.toList, request.user))
      },
      team => transaction {
        Model.teams.update(t =>
          where(t.id === id)
            set (
              t.name := team.name,
              t.creatorId := team.creatorId,
              t.hackathonId := team.hackathonId,
              t.problemId := team.problemId))
        Redirect(routes.Team.index).flashing("status" -> "teams.status.updated",
          "title" -> team.name)
      })
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    transaction {
      Model.teams.deleteWhere(t => t.id === id)
    }
    Redirect(routes.Team.index).flashing("status" -> "teams.status.deleted")
  }

}