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
import helpers.Conditions

object Problem extends LangAwareController with securesocial.core.SecureSocial {

  val problemForm = Form(
    mapping(
      "name" -> helpers.Forms.nonEmptyTextNonHtml,
      "description" -> helpers.Forms.nonEmptyTextSimpleHtmlOnly,
      "status" -> ignored(ProblemStatus.Blocked),
      "submitterId" -> longNumber,
      "hackathonId" -> longNumber)(model.Problem.apply)(model.Problem.unapply))

  def index(hid: Long) = UserAwareAction { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      model.Hackathon.lookup(hid).map { hackathon =>
        val problems = hackathon.problems.filter(p => Conditions.Problem.canRender(hackathon, p, user))
        val canAdd = helpers.Conditions.Problem.canAdd(hackathon, user)
        Ok(views.html.problems.index(Some(hackathon), problems, canAdd, user))
      }.getOrElse(NotFound(views.html.hackathons.view(None, user)))

    }
  }

  def view(hid: Long, id: Long) = UserAwareAction { implicit request =>
    inTransaction {

      val user = userFromRequest(request)

      model.Hackathon.lookup(hid).map { hackathon =>

        model.Problem.lookup(id).filter(_.hackathonId == hid).map { problem =>

          Ok(views.html.problems.view(Some(hackathon), Some(problem), user))

        }.getOrElse {
          NotFound(views.html.problems.view(Some(hackathon), None, user))
        }

      }.getOrElse {
        NotFound(views.html.hackathons.view(None, user))
      }

    }
  }

  def create(hid: Long) = SecuredAction() { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      model.Hackathon.lookup(hid).map { hackathon =>
        val problem = new model.Problem(user.id, hid)
        Ok(views.html.problems.create(Some(hackathon), problemForm.fill(problem), user))
      }.getOrElse {
        NotFound(views.html.hackathons.view(None, Some(user)))
      }
    }
  }

  def save(hid: Long) = SecuredAction() { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      model.Hackathon.lookup(hid).map { hackathon =>
        problemForm.bindFromRequest.fold(
          errors => BadRequest(views.html.problems.create(Some(hackathon), errors, user)),
          problem => {
            val dbProblem = model.Problem.insert(problem.copy(submitterId = user.id, status = model.ProblemStatus.Blocked))

            if (!hackathon.hasMember(user.id)) {
              hackathon.addMember(user)
            }

            val url = URL.externalUrl(routes.Problem.view(hid, dbProblem.id))
            val params = Seq(dbProblem.name, url)

            EmailSender.sendEmailToHackathonOrganiser(dbProblem.hackathon, "notifications.email.problem.added.subject", "notifications.email.problem.added.body", params)

            Redirect(routes.Problem.index(hid)).flashing("status" -> "added", "title" -> problem.name)
          })

      }.getOrElse {
        NotFound(views.html.hackathons.view(None, Some(user)))
      }
    }
  }

  def edit(hid: Long, id: Long) = SecuredAction() { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      model.Hackathon.lookup(hid).map { hackathon =>
        model.Problem.lookup(id).filter(_.hackathonId == hid).map { problem =>
          ensureHackathonOrganiserOrProblemSubmitterOrAdmin(hackathon, problem) {
            Ok(views.html.problems.edit(Some(problem.hackathon), id, problemForm.fill(problem), user))
          }
        }.getOrElse {
          NotFound(views.html.problems.view(Some(hackathon), None, Some(user)))
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
        model.Problem.lookup(id).filter(_.hackathonId == hid).map { dbProblem =>

          ensureHackathonOrganiserOrProblemSubmitterOrAdmin(hackathon, dbProblem) {

            problemForm.bindFromRequest.fold(
              errors => BadRequest(views.html.problems.edit(Some(hackathon), id, errors, user)),
              problem => {
                model.Problem.update(id, problem.copy(submitterId = dbProblem.submitterId, status = dbProblem.status))
                Redirect(routes.Problem.index(hid)).flashing("status" -> "updated", "title" -> problem.name)
              })

          }

        }.getOrElse {
          NotFound(views.html.problems.view(Some(hackathon), None, Some(user)))
        }

      }.getOrElse {
        NotFound(views.html.hackathons.view(None, Some(user)))
      }
    }
  }

  def approve(hid: Long, id: Long) = SecuredAction() { implicit request =>
    inTransaction {
      model.Problem.lookup(id).filter(_.hackathonId == hid).map { problem =>

        ensureHackathonOrganiserOrAdmin(problem.hackathon) {
          model.Problem.update(id, problem.copy(status = ProblemStatus.Approved))

          val user = userFromRequest(request)

          val url = URL.externalUrl(routes.Problem.view(hid, problem.id))
          val params = Seq(problem.name, url)

          EmailSender.sendEmailToProblemSubmitter(problem, "notifications.email.problem.approved.subject", "notifications.email.problem.approved.body", params)

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
      model.Problem.lookup(id).filter(_.hackathonId == hid).map { problem =>

        ensureHackathonOrganiserOrAdmin(problem.hackathon) {

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
    inTransaction {
      val user = userFromRequest(request)
      model.Hackathon.lookup(hid).map { hackathon =>
        model.Problem.lookup(id).filter(_.hackathonId == hid).map { problem =>

          ensureHackathonOrganiserOrAdmin(problem.hackathon) {

            if (user.id != problem.submitterId) {
              val url = URL.externalUrl(routes.Hackathon.view(hid))
              val params = Seq(problem.name, url)

              EmailSender.sendEmailToProblemSubmitter(problem, "notifications.email.problem.deleted.subject", "notifications.email.problem.deleted.body", params)
            }

            model.Problem.delete(id)
            Redirect(routes.Problem.index(hid)).flashing("status" -> "deleted")
          }
        }.getOrElse {
          NotFound(views.html.problems.view(Some(hackathon), None, Some(user)))
        }

      }.getOrElse {
        NotFound(views.html.hackathons.view(None, Some(user)))
      }

    }
  }
}
