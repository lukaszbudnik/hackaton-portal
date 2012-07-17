package controllers

import org.squeryl.PrimitiveTypeMode._
import play.api.data.Forms._
import play.api.data.Form
import play.api.mvc.Controller

object Prize extends Controller with securesocial.core.SecureSocial {

  val prizeForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "order" -> number,
      "founderName" -> optional(nonEmptyText),
      "founderWebPage" -> optional(nonEmptyText),
      "hackathonId" -> longNumber)(model.Prize.apply)(model.Prize.unapply))

  def index(hid: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.prizes.index(model.Hackathon.lookup(hid), request.user))
    }
  }

  def view(hid: Long, id: Long) = UserAwareAction { implicit request =>
    transaction {
      //TODO check if prize is in hackathon
      Ok(views.html.prizes.view(model.Hackathon.lookup(hid), model.Prize.lookup(id), request.user))
    }
  }

  def create(hid: Long) = SecuredAction() { implicit request =>
    transaction {
      val hackathon = model.Hackathon.lookup(hid)
      val prize = new model.Prize(1, hid)
      Ok(views.html.prizes.create(hackathon, prizeForm.fill(prize), request.user))
    }
  }

  def save(hid: Long) = SecuredAction() { implicit request =>
    prizeForm.bindFromRequest.fold(
      errors => transaction {
        val hackathon = model.Hackathon.lookup(hid)
        BadRequest(views.html.prizes.create(hackathon, errors, request.user))
      },
      prize => transaction {
        //TODO check if added
        model.Prize.insert(prize)
        Redirect(routes.Prize.index(hid)).flashing("status" -> "added", "title" -> prize.name)
      })
  }

  def edit(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Prize.lookup(id).map { prize =>
        Ok(views.html.prizes.edit(Some(prize.hackathon), id, prizeForm.fill(prize), request.user))
      }.getOrElse {
        // no prize found
        Redirect(routes.Prize.view(hid, id)).flashing()
      }
    }
  }

  def update(hid: Long, id: Long) = SecuredAction() { implicit request =>
    prizeForm.bindFromRequest.fold(
      errors => transaction {
        val hackathon = model.Hackathon.lookup(hid)
        BadRequest(views.html.prizes.edit(hackathon, id, errors, request.user))
      },
      prize => transaction {
        //TODO check if updated
        model.Prize.update(id, prize)
        Redirect(routes.Prize.index(hid)).flashing("status" -> "updated", "title" -> prize.name)
      })
  }

  def delete(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      //TODO check if deleted
      model.Prize.delete(id)
    }
    Redirect(routes.Prize.index(hid)).flashing("status" -> "deleted")
  }
}