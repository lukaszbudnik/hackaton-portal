package controllers

import org.squeryl.PrimitiveTypeMode.transaction
import core.LangAwareController
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

object Team extends LangAwareController with securesocial.core.SecureSocial {

  val teamForm = Form(
    mapping(
      "name" -> text.verifying("teams.name.error", !_.isEmpty()),
      "status" -> ignored(TeamStatus.Unverified),
      "creatorId" -> longNumber,
      "hackathonId" -> longNumber,
      "problemId" -> optional(longNumber))(model.Team.apply)(model.Team.unapply))

  def index(hid: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.teams.index(model.Hackathon.lookup(hid), userFromRequest))
    }
  }

  def view(hid: Long, id: Long) = UserAwareAction { implicit request =>
    transaction {
      val team = model.Team.lookup(id)
      val hackathon = team.map { t => Some(t.hackathon) }.getOrElse { model.Hackathon.lookup(hid) }
      Ok(views.html.teams.view(hackathon, team, userFromRequest))
    }
  }

  def create(hid: Long) = SecuredAction() { implicit request =>
    transaction {
      val user = userFromRequest(request)
      val team = new model.Team(user.id, hid)
      val hackathon = model.Hackathon.lookup(hid)
      Ok(views.html.teams.create(hackathon, teamForm.fill(team), user))
    }
  }

  def save(hid: Long) = SecuredAction() { implicit request =>
    teamForm.bindFromRequest.fold(
      errors => BadRequest(views.html.teams.create(model.Hackathon.lookup(hid), errors, userFromRequest(request))),
      team => transaction {
        // insert team and add creator as a member
        val dbTeam = model.Team.insert(team)
        dbTeam.addMember(team.creator)

        val url = URL.externalUrl(routes.Team.view(hid, team.id))
        val params = Seq(team.name, url)

        EmailSender.sendEmailToHackathonOrganiser(dbTeam.hackathon, "notifications.email.team.created.subject", "notifications.email.team.created.body", params)

        Redirect(routes.Team.index(hid)).flashing("status" -> "added", "title" -> team.name)
      })
  }

  def edit(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Team.lookup(id).map { team =>
        helpers.Security.verifyIfAllowed(hid == team.hackathonId)(request.user)
        helpers.Security.verifyIfAllowed(team.creatorId, team.hackathon.organiserId)(request.user)
        Ok(views.html.teams.edit(Some(team.hackathon), id, teamForm.fill(team), userFromRequest(request)))
      }.getOrElse {
        // no team found
        Redirect(routes.Team.view(hid, id)).flashing()
      }
    }
  }

  def update(hid: Long, id: Long) = SecuredAction() { implicit request =>
    teamForm.bindFromRequest.fold(
      errors => BadRequest(views.html.teams.edit(model.Hackathon.lookup(hid), id, errors, userFromRequest(request))),
      team => transaction {
        val dbTeam = model.Team.lookup(id)

        dbTeam.map { team =>
          helpers.Security.verifyIfAllowed(hid == team.hackathonId)(request.user)
          helpers.Security.verifyIfAllowed(team.creatorId, team.hackathon.organiserId)(request.user)
        }

        model.Team.update(id, team.copy(status = dbTeam.get.status))
        Redirect(routes.Team.index(hid)).flashing("status" -> "updated", "title" -> team.name)
      })
  }

  def verify(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {

      val result = for (
        team <- model.Team.lookup(id);
        user <- model.User.lookupByOpenId(request.user.id.id + request.user.id.providerId)
      ) yield {
        implicit val socialUser = request.user
        helpers.Security.verifyIfAllowed(hid == team.hackathonId)
        helpers.Security.verifyIfAllowed(user.isAdmin || team.hackathon.organiserId == user.id)
        model.Team.update(id, team.copy(status = TeamStatus.Approved))

        val url = URL.externalUrl(routes.Team.view(hid, team.id))
        val params = Seq(team.name, url)

        EmailSender.sendEmailToWholeTeam(team, "notifications.email.team.verified.subject", "notifications.email.team.verified.body", params)

        Ok(JsArray(Seq(JsObject(List(
          "status" -> JsString("ok"))))))
      }

      result.getOrElse {
        NotFound(JsArray(Seq(JsObject(List(
          "status" -> JsString("error"))))))
      }
    }
  }

  def approve(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      val result = for (
        team <- model.Team.lookup(id);
        user <- model.User.lookupByOpenId(request.user.id.id + request.user.id.providerId)
      ) yield {
        implicit val socialUser = request.user
        helpers.Security.verifyIfAllowed(hid == team.hackathonId)
        helpers.Security.verifyIfAllowed(user.isAdmin || team.hackathon.organiserId == user.id || team.creatorId == user.id)
        model.Team.update(id, team.copy(status = TeamStatus.Approved))

        val url = URL.externalUrl(routes.Team.view(hid, team.id))
        val params = Seq(team.name, url)

        EmailSender.sendEmailToWholeTeam(team, "notifications.email.team.approved.subject", "notifications.email.team.approved.body", params)

        Ok(JsArray(Seq(JsObject(List(
          "status" -> JsString("ok"))))))
      }

      result.getOrElse {
        NotFound(JsArray(Seq(JsObject(List(
          "status" -> JsString("error"))))))
      }
    }
  }

  def suspend(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      val result = for (
        team <- model.Team.lookup(id);
        user <- model.User.lookupByOpenId(request.user.id.id + request.user.id.providerId)
      ) yield {
        implicit val socialUser = request.user

        helpers.Security.verifyIfAllowed(hid == team.hackathonId)
        helpers.Security.verifyIfAllowed(user.isAdmin || team.creatorId == user.id || team.hackathon.organiserId == user.id)
        model.Team.update(id, team.copy(status = TeamStatus.Suspended))

        val url = URL.externalUrl(routes.Team.view(hid, team.id))
        val params = Seq(team.name, url)

        EmailSender.sendEmailToWholeTeam(team, "notifications.email.team.suspended.subject", "notifications.email.team.suspended.body", params)

        Ok(JsArray(Seq(JsObject(List(
          "status" -> JsString("ok"))))))

      }

      result.getOrElse {
        NotFound(JsArray(Seq(JsObject(List(
          "status" -> JsString("error"))))))
      }
    }
  }

  def block(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      val result = for (
        team <- model.Team.lookup(id);
        user <- model.User.lookupByOpenId(request.user.id.id + request.user.id.providerId)
      ) yield {
        implicit val socialUser = request.user

        helpers.Security.verifyIfAllowed(hid == team.hackathonId)
        helpers.Security.verifyIfAllowed(user.isAdmin || team.hackathon.organiserId == user.id)
        model.Team.update(id, team.copy(status = TeamStatus.Blocked))

        val url = URL.externalUrl(routes.Team.view(hid, team.id))
        val params = Seq(team.name, url)

        EmailSender.sendEmailToWholeTeam(team, "notifications.email.team.blocked.subject", "notifications.email.team.blocked.body", params)

        Ok(JsArray(Seq(JsObject(List(
          "status" -> JsString("ok"))))))
      }

      result.getOrElse {
        NotFound(JsArray(Seq(JsObject(List(
          "status" -> JsString("error"))))))
      }
    }
  }

  def delete(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      val result = for (
        team <- model.Team.lookup(id)
      ) yield {
        implicit val socialUser = request.user

        helpers.Security.verifyIfAllowed(hid == team.hackathonId)
        helpers.Security.verifyIfAllowed(team.creatorId, team.hackathon.organiserId)

        val url = URL.externalUrl(routes.Hackathon.view(hid))
        val params = Seq(team.name, url)

        EmailSender.sendEmailToWholeTeam(team, "notifications.email.team.deleted.subject", "notifications.email.team.deleted.body", params)

        model.Team.delete(id)

        Redirect(routes.Team.index(hid)).flashing("status" -> "deleted")
      }

      result.getOrElse(Redirect(routes.Team.index(hid)).flashing("status" -> "error"))
    }
  }

  def join(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      val result = for (
        team <- model.Team.lookup(id);
        user <- model.User.lookupByOpenId(request.user.id.id + request.user.id.providerId)
      ) yield {
        if (!team.hasMember(user.id)) {
          team.addMember(user)
        }
        Redirect(routes.Team.view(hid, id)).flashing("status" -> "joined")
      }

      result.getOrElse(Redirect(routes.Team.view(hid, id)).flashing("status" -> "error"))
    }
  }

  def disconnect(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      val result = for (
        team <- model.Team.lookup(id);
        user <- model.User.lookupByOpenId(request.user.id.id + request.user.id.providerId)
      ) yield {
        if (!team.hasMember(user.id)) {
          team.deleteMember(user)
        }
        Redirect(routes.Team.view(hid, id)).flashing("status" -> "disconnected")
      }

      result.getOrElse(Redirect(routes.Team.view(hid, id)).flashing("status" -> "error"))
    }
  }

  def disconnectUser(hid: Long, id: Long, userId: Long) = SecuredAction() { implicit request =>
    transaction {
      val result = for (
        team <- model.Team.lookup(id);
        user <- model.User.lookupByOpenId(request.user.id.id + request.user.id.providerId)
      ) yield {
        helpers.Security.verifyIfAllowed(team.creatorId, team.hackathon.organiserId)(request.user)
        team.deleteMember(user)

        Redirect(routes.Team.view(hid, id)).flashing("status" -> "disconnectedUser")
      }

      result.getOrElse(Redirect(routes.Team.view(hid, id)).flashing("status" -> "error"))
    }
  }
}