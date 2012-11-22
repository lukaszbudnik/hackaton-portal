package controllers

import org.squeryl.PrimitiveTypeMode._
import play.api.data.Forms._
import play.api.data.Form
import play.api.mvc.Controller
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
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
      model.Hackathon.lookup(hid).map { hackathon =>
        val user = userFromRequest(request)
        val problem = new model.Problem(user.id, hid)
        Ok(views.html.problems.create(Some(hackathon), problemForm.fill(problem), user))
      }.getOrElse(Redirect(routes.Hackathon.view(hid)).flashing())
    }
  }

  def save(hid: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Hackathon.lookup(hid).map { hackathon =>
        val user = userFromRequest(request)
        problemForm.bindFromRequest.fold(
          errors => BadRequest(views.html.problems.create(Some(hackathon), errors, user)),
          problem => {
            val dbProblem = model.Problem.insert(problem.copy(submitterId = user.id))

            val url = URL.externalUrl(routes.Problem.view(hid, dbProblem.id))
            val params = Seq(dbProblem.name, url)

            EmailSender.sendEmailToHackathonOrganiser(dbProblem.hackathon, "notifications.email.problem.added.subject", "notifications.email.problem.added.body", params)

            Redirect(routes.Problem.index(hid)).flashing("status" -> "added", "title" -> problem.name)
          })

      }.getOrElse(Redirect(routes.Hackathon.view(hid)))
    }
  }

  def edit(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Hackathon.lookup(hid).map { hackathon =>
        model.Problem.lookup(id).map { problem =>
          ensureHackathonOrganiserOrProblemSubmitterOrAdmin(hackathon, problem, problem.hackathonId == hid) {
            val user = userFromRequest(request)
            Ok(views.html.problems.edit(Some(problem.hackathon), id, problemForm.fill(problem), user))
          }
        }.getOrElse(Redirect(routes.Problem.view(hid, id)))
      }.getOrElse(Redirect(routes.Hackathon.view(hid)))
    }
  }

  def update(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Hackathon.lookup(hid).map { hackathon =>
        model.Problem.lookup(id).map { dbProblem =>

          problemForm.bindFromRequest.fold(
            errors => {
              val user = userFromRequest(request)
              BadRequest(views.html.problems.edit(model.Hackathon.lookup(hid), id, errors, user))
            },
            problem => {
              ensureHackathonOrganiserOrProblemSubmitterOrAdmin(hackathon, dbProblem, hid == problem.hackathonId) {
                model.Problem.update(id, problem.copy(status = dbProblem.status))
                Redirect(routes.Problem.index(hid)).flashing("status" -> "updated", "title" -> problem.name)
              }
            })
        }.getOrElse(Redirect(routes.Problem.view(hid, id)))
      }.getOrElse(Redirect(routes.Hackathon.view(hid)))
    }
  }

  def verify(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Problem.lookup(id).map { problem =>

        ensureHackathonOrganiserOrAdmin(problem.hackathon, hid == problem.hackathonId) {
          model.Problem.update(id, problem.copy(status = ProblemStatus.Approved))

          val url = URL.externalUrl(routes.Problem.view(hid, problem.id))
          val params = Seq(problem.name, url)

          EmailSender.sendEmailToProblemSubmitter(problem, "notifications.email.problem.verified.subject", "notifications.email.problem.verified.body", params)

          Ok(JsArray(Seq(JsObject(List(
            "status" -> JsString("ok"))))))
        }
      }.getOrElse {
        NotFound(JsArray(Seq(JsObject(List(
          "status" -> JsString("error"))))))
      }
    }
  }

  def approve(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Problem.lookup(id).map { problem =>

        ensureHackathonOrganiserOrProblemSubmitterOrAdmin(problem.hackathon, problem, hid == problem.hackathonId) {
          model.Problem.update(id, problem.copy(status = ProblemStatus.Approved))

          val user = userFromRequest(request)

          if (user.id != problem.submitterId) {
            val url = URL.externalUrl(routes.Problem.view(hid, problem.id))
            val params = Seq(problem.name, url)

            EmailSender.sendEmailToProblemSubmitter(problem, "notifications.email.problem.approved.subject", "notifications.email.problem.approved.body", params)
          }

          Ok(JsArray(Seq(JsObject(List(
            "status" -> JsString("ok"))))))
        }

      }.getOrElse {
        NotFound(JsArray(Seq(JsObject(List(
          "status" -> JsString("error"))))))
      }
    }
  }

  def suspend(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Problem.lookup(id).map { problem =>

        ensureHackathonOrganiserOrProblemSubmitterOrAdmin(problem.hackathon, problem, hid == problem.hackathonId) {

          val user = userFromRequest(request)

          model.Problem.update(id, problem.copy(status = ProblemStatus.Suspended))

          if (user.id != problem.submitterId) {
            val url = URL.externalUrl(routes.Problem.view(hid, problem.id))
            val params = Seq(problem.name, url)

            EmailSender.sendEmailToProblemSubmitter(problem, "notifications.email.problem.suspended.subject", "notifications.email.problem.suspended.body", params)
          }

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
    transaction {
      model.Problem.lookup(id).map { problem =>

        ensureHackathonOrganiserOrAdmin(problem.hackathon, hid == problem.hackathonId) {

          model.Problem.update(id, problem.copy(status = ProblemStatus.Blocked))

          val url = URL.externalUrl(routes.Problem.view(hid, problem.id))
          val params = Seq(problem.name, url)

          EmailSender.sendEmailToProblemSubmitter(problem, "notifications.email.problem.blocked.subject", "notifications.email.problem.blocked.body", params)

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
    transaction {
      model.Hackathon.lookup(hid).map { hackathon =>
        model.Problem.lookup(id).map { problem =>

          ensureHackathonOrganiserOrAdmin(problem.hackathon, hid == problem.hackathonId) {
            val user = userFromRequest(request)

            if (user.id != problem.submitterId) {
              val url = URL.externalUrl(routes.Hackathon.view(hid))
              val params = Seq(problem.name, url)

              EmailSender.sendEmailToProblemSubmitter(problem, "notifications.email.problem.deleted.subject", "notifications.email.problem.deleted.body", params)
            }

            model.Problem.delete(id)
            Redirect(routes.Problem.index(hid)).flashing("status" -> "deleted")
          }
        }.getOrElse(Redirect(routes.Problem.view(hid, id)))
        
      }.getOrElse(Redirect(routes.Hackathon.view(hid)))

    }
  }
}
