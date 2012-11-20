package controllers

import org.squeryl.PrimitiveTypeMode._
import play.api.data.Forms._
import play.api.data.Form
import play.api.mvc.Controller
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import core.LangAwareController
import model.ProblemStatus
import helpers.EmailSender
import helpers.URL

object Problem extends LangAwareController with securesocial.core.SecureSocial {

  val problemForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "status" -> ignored(ProblemStatus.Unverified),
      "submitterId" -> longNumber,
      "hackathonId" -> longNumber)(model.Problem.apply)(model.Problem.unapply))

  def index(hid: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.problems.index(model.Hackathon.lookup(hid), userFromRequest))
    }
  }

  def view(hid: Long, id: Long) = UserAwareAction { implicit request =>
    transaction {
      val problem = model.Problem.lookup(id)
      val hackathon = problem.map { p => Some(p.hackathon) }.getOrElse { model.Hackathon.lookup(hid) }
      Ok(views.html.problems.view(hackathon, problem, userFromRequest))
    }
  }

  def create(hid: Long) = SecuredAction() { implicit request =>
    transaction {
      val hackathon = model.Hackathon.lookup(hid)
      val user = userFromRequest(request)
      val problem = new model.Problem(user.id, hid)
      Ok(views.html.problems.create(hackathon, problemForm.fill(problem), user))
    }
  }

  def save(hid: Long) = SecuredAction() { implicit request =>
    val user = userFromRequest(request)
    problemForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.problems.create(model.Hackathon.lookup(hid), errors, user))
      },
      problem => transaction {
        val dbProblem = model.Problem.insert(problem.copy(submitterId = user.id))

        val url = URL.externalUrl(routes.Problem.view(hid, dbProblem.id))
        val params = Seq(dbProblem.name, url)

        EmailSender.sendEmailToHackathonOrganiser(dbProblem.hackathon, "notifications.email.problem.added.subject", "notifications.email.problem.added.body", params)

        Redirect(routes.Problem.index(hid)).flashing("status" -> "added", "title" -> problem.name)
      })
  }

  def edit(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Problem.lookup(id).map { problem =>
        helpers.Security.verifyIfAllowed(hid == problem.hackathonId)(request.user)
        helpers.Security.verifyIfAllowed(problem.submitterId, problem.hackathon.organiserId)(request.user)
        val user = userFromRequest(request)
        Ok(views.html.problems.edit(Some(problem.hackathon), id, problemForm.fill(problem), user))
      }.getOrElse {
        Redirect(routes.Problem.view(hid, id)).flashing()
      }
    }
  }

  def update(hid: Long, id: Long) = SecuredAction() { implicit request =>
    problemForm.bindFromRequest.fold(
      errors => transaction {
        val user = userFromRequest(request)
        BadRequest(views.html.problems.edit(model.Hackathon.lookup(hid), id, errors, user))
      },
      problem => transaction {
        val dbProblem = model.Problem.lookup(id)

        dbProblem.map { problem =>
          helpers.Security.verifyIfAllowed(hid == problem.hackathonId)(request.user)
          helpers.Security.verifyIfAllowed(problem.submitterId, problem.hackathon.organiserId)(request.user)
        }

        model.Problem.update(id, problem.copy(status = dbProblem.get.status))
        Redirect(routes.Problem.index(hid)).flashing("status" -> "updated", "title" -> problem.name)
      })

  }

  def verify(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Problem.lookup(id).map { problem =>

        implicit val socialUser = request.user
        val user = userFromRequest(request)
        helpers.Security.verifyIfAllowed(hid == problem.hackathonId)
        helpers.Security.verifyIfAllowed(user.isAdmin || problem.hackathon.organiserId == user.id)
        model.Problem.update(id, problem.copy(status = ProblemStatus.Approved))

        val url = URL.externalUrl(routes.Problem.view(hid, problem.id))
        val params = Seq(problem.name, url)

        EmailSender.sendEmailToProblemSubmitter(problem, "notifications.email.problem.verified.subject", "notifications.email.problem.verified.body", params)

        Ok(JsArray(Seq(JsObject(List(
          "status" -> JsString("ok"))))))
      }.getOrElse {
        NotFound(JsArray(Seq(JsObject(List(
          "status" -> JsString("error"))))))
      }
    }
  }

  def approve(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Problem.lookup(id).map { problem =>

        implicit val socialUser = request.user
        val user = userFromRequest(request)

        helpers.Security.verifyIfAllowed(hid == problem.hackathonId)
        helpers.Security.verifyIfAllowed(user.isAdmin || problem.hackathon.organiserId == user.id || problem.submitterId == user.id)
        model.Problem.update(id, problem.copy(status = ProblemStatus.Approved))

        if (user.id != problem.submitterId) {
          val url = URL.externalUrl(routes.Problem.view(hid, problem.id))
          val params = Seq(problem.name, url)

          EmailSender.sendEmailToProblemSubmitter(problem, "notifications.email.problem.approved.subject", "notifications.email.problem.approved.body", params)
        }

        Ok(JsArray(Seq(JsObject(List(
          "status" -> JsString("ok"))))))

      }.getOrElse {
        NotFound(JsArray(Seq(JsObject(List(
          "status" -> JsString("error"))))))
      }
    }
  }

  def suspend(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Problem.lookup(id).map { problem =>

        implicit val socialUser = request.user
        val user = userFromRequest(request)

        helpers.Security.verifyIfAllowed(hid == problem.hackathonId)
        helpers.Security.verifyIfAllowed(user.isAdmin || problem.submitterId == user.id || problem.hackathon.organiserId == user.id)
        model.Problem.update(id, problem.copy(status = ProblemStatus.Suspended))

        if (user.id != problem.submitterId) {
          val url = URL.externalUrl(routes.Problem.view(hid, problem.id))
          val params = Seq(problem.name, url)

          EmailSender.sendEmailToProblemSubmitter(problem, "notifications.email.problem.suspended.subject", "notifications.email.problem.suspended.body", params)
        }

        Ok(JsArray(Seq(JsObject(List(
          "status" -> JsString("ok"))))))

      }.getOrElse {
        NotFound(JsArray(Seq(JsObject(List(
          "status" -> JsString("error"))))))
      }
    }
  }

  def block(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Problem.lookup(id).map { problem =>

        implicit val socialUser = request.user
        val user = userFromRequest(request)
        helpers.Security.verifyIfAllowed(hid == problem.hackathonId)
        helpers.Security.verifyIfAllowed(user.isAdmin || problem.hackathon.organiserId == user.id)
        model.Problem.update(id, problem.copy(status = ProblemStatus.Blocked))

        val url = URL.externalUrl(routes.Problem.view(hid, problem.id))
        val params = Seq(problem.name, url)

        EmailSender.sendEmailToProblemSubmitter(problem, "notifications.email.problem.blocked.subject", "notifications.email.problem.blocked.body", params)

        Ok(JsArray(Seq(JsObject(List(
          "status" -> JsString("ok"))))))

      }.getOrElse {
        NotFound(JsArray(Seq(JsObject(List(
          "status" -> JsString("error"))))))
      }
    }
  }

  def delete(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Problem.lookup(id).map { problem =>

        implicit val socialUser = request.user
        val user = userFromRequest(request)
        helpers.Security.verifyIfAllowed(hid == problem.hackathonId)
        helpers.Security.verifyIfAllowed(problem.submitterId, problem.hackathon.organiserId)

        if (user.id != problem.submitterId) {
          val url = URL.externalUrl(routes.Hackathon.view(hid))
          val params = Seq(problem.name, url)

          EmailSender.sendEmailToProblemSubmitter(problem, "notifications.email.problem.deleted.subject", "notifications.email.problem.deleted.body", params)
        }

        model.Problem.delete(id)
      }

      Redirect(routes.Problem.index(hid)).flashing("status" -> "deleted")
    }
  }
}
