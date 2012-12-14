package controllers

import org.squeryl.PrimitiveTypeMode.inTransaction
import model.TeamStatus
import play.api.data.Forms._
import play.api.data.Form
import play.api.mvc.Controller
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import securesocial.core.SocialUser
import securesocial.core.UserId
import securesocial.core.AuthenticationMethod
import helpers.URL
import helpers.EmailSender
import play.api.mvc.AnyContent
import helpers.Conditions

object Team extends LangAwareController {

  val teamForm = Form(
    mapping(
      "name" -> text.verifying("teams.name.error", !_.isEmpty()),
      "status" -> ignored(TeamStatus.Blocked),
      "creatorId" -> longNumber,
      "hackathonId" -> longNumber,
      "problemId" -> optional(longNumber))(model.Team.apply)(model.Team.unapply))

  def index(hid: Long) = UserAwareAction { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      model.Hackathon.lookup(hid).map { hackathon =>
        val teams = hackathon.teams.filter(t => Conditions.Team.canRender(hackathon, t, user))
        val canAdd = Conditions.Team.canAdd(hackathon, user)
        Ok(views.html.teams.index(Some(hackathon), teams, canAdd, user))
      }.getOrElse(NotFound(views.html.hackathons.view(None, user)))

    }
  }

  def view(hid: Long, id: Long) = UserAwareAction { implicit request =>
    inTransaction {

      val user = userFromRequest(request)

      model.Hackathon.lookup(hid).map { hackathon =>

        model.Team.lookup(id).filter(_.hackathonId == hid).map { team =>
          Ok(views.html.teams.view(Some(hackathon), Some(team), userFromRequest))
        }.getOrElse {
          NotFound(views.html.teams.view(Some(hackathon), None, userFromRequest))
        }

      }.getOrElse {
        NotFound(views.html.hackathons.view(None, user))
      }

    }
  }

  def create(hid: Long) = SecuredAction() { implicit request =>
    inTransaction {
      val user = userFromRequest(request)

      model.Team.lookupByHackathonIdAndCreatorId(hid, user.id).map { team =>

        Redirect(routes.Team.view(hid, team.id)).flashing("status" -> "cannotCreateBecauseTeamAlreadyCreatedByUser")

      }.orElse {

        model.Team.lookupByHackathonIdAndMemberId(hid, user.id).map { team =>
          Redirect(routes.Team.view(hid, team.id)).flashing("status" -> "cannotCreateBecauseAlreadyMemberOfAnotherTeam")
        }

      }.getOrElse {

        model.Hackathon.lookup(hid).map { hackathon =>
          val team = new model.Team(user.id, hid)
          Ok(views.html.teams.create(Some(hackathon), teamForm.fill(team), user))
        }.getOrElse {
          NotFound(views.html.hackathons.view(None, Some(user)))
        }

      }

    }
  }

  def save(hid: Long) = SecuredAction() { implicit request =>
    inTransaction {
      val user = userFromRequest(request)

      model.Team.lookupByHackathonIdAndCreatorId(hid, user.id).map { team =>

        Redirect(routes.Team.view(hid, team.id)).flashing("status" -> "teamAlreadyCreatedByUser")

      }.orElse {

        model.Team.lookupByHackathonIdAndMemberId(hid, user.id).map { team =>
          Redirect(routes.Team.view(hid, team.id)).flashing("status" -> "alreadyMemberOfAnotherTeam")
        }

      }.getOrElse {

        model.Hackathon.lookup(hid).map { hackathon =>

          teamForm.bindFromRequest.fold(
            errors => BadRequest(views.html.teams.create(Some(hackathon), errors, user)),
            team => {

              // insert team and add creator as a member
              val dbTeam = model.Team.insert(team.copy(creatorId = user.id, status = model.TeamStatus.Blocked))
              dbTeam.addMember(team.creator)

              if (!hackathon.hasMember(user.id)) {
                hackathon.addMember(user)
              }

              val url = URL.externalUrl(routes.Team.view(hid, team.id))
              val params = Seq(team.name, url)

              EmailSender.sendEmailToHackathonOrganiser(dbTeam.hackathon, "notifications.email.team.created.subject", "notifications.email.team.created.body", params)

              Redirect(routes.Team.index(hid)).flashing("status" -> "added", "title" -> team.name)
            })

        }.getOrElse {
          NotFound(views.html.hackathons.view(None, Some(user)))
        }
      }
    }
  }

  def edit(hid: Long, id: Long) = SecuredAction() { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      model.Hackathon.lookup(hid).map { hackathon =>

        model.Team.lookup(id).filter(_.hackathonId == hid).map { team =>
          ensureHackathonOrganiserOrTeamLeaderOrAdmin(team.hackathon, team) {
            Ok(views.html.teams.edit(Some(team.hackathon), id, teamForm.fill(team), user))
          }
        }.getOrElse {
          NotFound(views.html.teams.view(Some(hackathon), None, Some(user)))
        }

      }.getOrElse {
        NotFound(views.html.hackathons.view(None, Some(user)))
      }

    }
  }

  def update(hid: Long, id: Long) = SecuredAction() { implicit request =>
    inTransaction {

      val user = userFromRequest(request)

      model.Hackathon.lookup(hid).map { hackathon =>

        model.Team.lookup(id).filter(_.hackathonId == hid).map { dbTeam =>

          ensureHackathonOrganiserOrTeamLeaderOrAdmin(dbTeam.hackathon, dbTeam) {
            teamForm.bindFromRequest.fold(
              errors => BadRequest(views.html.teams.edit(Some(hackathon), id, errors, user)),
              team => {
                model.Team.update(id, team.copy(status = dbTeam.status, creatorId = dbTeam.creatorId))
                Redirect(routes.Team.index(hid)).flashing("status" -> "updated", "title" -> team.name)
              })
          }

        }.getOrElse {
          NotFound(views.html.teams.view(Some(hackathon), None, Some(user)))
        }

      }.getOrElse {
        NotFound(views.html.hackathons.view(None, Some(user)))
      }

    }
  }

  def approve(hid: Long, id: Long) = SecuredAction() { implicit request =>
    inTransaction {

      model.Team.lookup(id).filter(_.hackathonId == hid).map { team =>
        ensureHackathonOrganiserOrAdmin(team.hackathon) {
          model.Team.update(id, team.copy(status = TeamStatus.Approved))
          val url = URL.externalUrl(routes.Team.view(hid, team.id))
          val params = Seq(team.name, url)

          EmailSender.sendEmailToWholeTeam(team, "notifications.email.team.verified.subject", "notifications.email.team.verified.body", params)

          Ok(JsArray(Seq(JsObject(List(
            "status" -> JsString("ok"))))))
        }
      }.getOrElse {
        NotFound(JsArray(Seq(JsObject(List(
          "status" -> JsString("error"))))))
      }
    }
  }

  def block(hid: Long, id: Long) = SecuredAction() { implicit request =>
    inTransaction {
      model.Team.lookup(id).filter(_.hackathonId == hid).map { team =>
        ensureHackathonOrganiserOrAdmin(team.hackathon) {
          model.Team.update(id, team.copy(status = TeamStatus.Blocked))

          val url = URL.externalUrl(routes.Team.view(hid, team.id))
          val params = Seq(team.name, url)

          EmailSender.sendEmailToWholeTeam(team, "notifications.email.team.blocked.subject", "notifications.email.team.blocked.body", params)

          Ok(JsArray(Seq(JsObject(List(
            "status" -> JsString("ok"))))))
        }
      }.getOrElse {
        NotFound(JsArray(Seq(JsObject(List(
          "status" -> JsString("error"))))))
      }
    }
  }

  def delete(hid: Long, id: Long) = SecuredAction() { implicit request =>
    inTransaction {

      val user = userFromRequest(request)

      model.Hackathon.lookup(hid).map { hackathon =>

        model.Team.lookup(id).filter(_.hackathonId == hid).map { team =>
          ensureHackathonOrganiserOrTeamLeaderOrAdmin(team.hackathon, team) {

            model.Team.delete(id)
            val url = URL.externalUrl(routes.Hackathon.view(hid))
            val params = Seq(team.name, url)

            EmailSender.sendEmailToWholeTeam(team, "notifications.email.team.deleted.subject", "notifications.email.team.deleted.body", params)

            Redirect(routes.Team.index(hid)).flashing("status" -> "deleted")
          }
        }.getOrElse {
          NotFound(views.html.teams.view(Some(hackathon), None, Some(user)))
        }

      }.getOrElse {
        NotFound(views.html.hackathons.view(None, Some(user)))
      }
    }
  }

  def join(hid: Long, id: Long) = SecuredAction() { implicit request =>
    inTransaction {
      val user = userFromRequest(request)

      model.Team.lookupByHackathonIdAndCreatorId(hid, user.id).map { team =>

        Redirect(routes.Team.view(hid, team.id)).flashing("status" -> "cannotJoinBecauseTeamAlreadyCreatedByUser")

      }.orElse {

        model.Team.lookupByHackathonIdAndMemberId(hid, user.id).map { team =>
          Redirect(routes.Team.view(hid, team.id)).flashing("status" -> "cannotJoinBecauseAlreadyMemberOfAnotherTeam")
        }

      }.getOrElse {

        model.Hackathon.lookup(hid).map { hackathon =>

          model.Team.lookup(id).map { team =>

            team.addMember(user)
            Redirect(routes.Team.view(hid, id)).flashing("status" -> "joined")

          }.getOrElse(NotFound(views.html.teams.view(Some(hackathon), None, Some(user))))
        }.getOrElse(NotFound(views.html.hackathons.view(None, Some(user))))
      }
    }
  }

  def disconnect(hid: Long, id: Long) = SecuredAction() { implicit request =>
    inTransaction {
      model.Team.lookup(id).map { team =>
        val user = userFromRequest(request)
        if (!team.hasMember(user.id)) {
          team.deleteMember(user)
        }
        Redirect(routes.Team.view(hid, id)).flashing("status" -> "disconnected")
      }.getOrElse(Redirect(routes.Team.view(hid, id)).flashing("status" -> "error"))
    }
  }

  def disconnectUser(hid: Long, id: Long, userId: Long) = SecuredAction() { implicit request =>
    inTransaction {
      (for (
        team <- model.Team.lookup(id) if (hid == team.hackathonId);
        user <- model.User.lookup(userId) if (team.hasMember(user.id))
      ) yield {
        ensureHackathonOrganiserOrTeamLeaderOrAdmin(team.hackathon, team) {
          team.deleteMember(user)

          Redirect(routes.Team.view(hid, id)).flashing("status" -> "disconnectedUser")
        }
      }).getOrElse(Redirect(routes.Team.view(hid, id)).flashing("status" -> "error"))
    }
  }
}
