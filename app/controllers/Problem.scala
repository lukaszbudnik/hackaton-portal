package controllers

import org.squeryl.PrimitiveTypeMode._
import play.api.data.Forms._
import play.api.data.Form
import play.api.mvc.Controller

object Problem extends Controller with securesocial.core.SecureSocial {

  val problemForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "submitterId" -> longNumber,
      "hackathonId" -> longNumber)(model.Problem.apply)(model.Problem.unapply))

  def index(hid: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.problems.index(model.Hackathon.lookup(hid), request.user))
    }
  }

  def view(hid: Long, id: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.problems.view(model.Hackathon.lookup(hid), model.Problem.lookup(id), request.user))
    }
  }

  def create(hid: Long) = SecuredAction() { implicit request =>
    transaction {
      val hackathon = model.Hackathon.lookup(hid)
      val problem = model.Problem("", "", request.user.hackathonUserId, hid)
      Ok(views.html.problems.create(hackathon, problemForm.fill(problem), request.user))
    }
  }

  def save(hid: Long) = SecuredAction() { implicit request =>
    problemForm.bindFromRequest.fold(
      errors => transaction {
      val hackathon = model.Hackathon.lookup(hid)
        BadRequest(views.html.problems.create(hackathon, errors, request.user))
      },
      problem => transaction {
        model.Problem.insert(problem)
        Redirect(routes.Problem.index(hid)).flashing("status" -> "added", "title" -> problem.name)
      })
  }

  def edit(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Problem.lookup(id).map { problem =>
        Ok(views.html.problems.edit(Some(problem.hackathon), id, problemForm.fill(problem), request.user))
      }.getOrElse {
        // no problem found
        Redirect(routes.Problem.view(hid, id)).flashing()
      }
    }
  }

  def update(hid: Long, id: Long) = SecuredAction() { implicit request =>
    problemForm.bindFromRequest.fold(
      errors => transaction {
        val hackathon = model.Hackathon.lookup(hid)
        BadRequest(views.html.problems.edit(hackathon, id, errors, request.user))
      },
      problem => transaction {
        model.Problem.update(id, problem)
        Redirect(routes.Problem.index(hid)).flashing("status" -> "updated", "title" -> problem.name)
      })

  }

  def delete(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Problem.delete(id)
    }
    Redirect(routes.Problem.index(hid)).flashing("status" -> "deleted")
  }
}