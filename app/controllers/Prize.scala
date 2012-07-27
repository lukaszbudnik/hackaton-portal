package controllers

import org.squeryl.PrimitiveTypeMode._
import play.api.data.Forms._
import play.api.data.Form
import play.api.mvc.Controller
import core.LangAwareController

object Prize extends LangAwareController with securesocial.core.SecureSocial {

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
      val prize = model.Prize.lookup(id)
      val hackathon = prize.map { p => Some(p.hackathon) }.getOrElse { model.Hackathon.lookup(hid) }
      Ok(views.html.prizes.view(hackathon, prize, request.user))
    }
  }

  def create(hid: Long) = SecuredAction() { implicit request =>
    transaction {
      val hackathon = model.Hackathon.lookup(hid)
      hackathon.map { h =>
        helpers.Security.verifyIfAllowed(h.organiserId)(request.user)
      }
      val prize = new model.Prize(1, hid)
      Ok(views.html.prizes.create(hackathon, prizeForm.fill(prize), request.user))
    }
  }

  def save(hid: Long) = SecuredAction() { implicit request =>
    prizeForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.prizes.create(model.Hackathon.lookup(hid), errors, request.user))
      },
      prize => transaction {
        model.Hackathon.lookup(hid).map { h =>
          helpers.Security.verifyIfAllowed(h.organiserId)(request.user)
        }
        //TODO check if added
        model.Prize.insert(prize)
        Redirect(routes.Prize.index(hid)).flashing("status" -> "added", "title" -> prize.name)
      })
  }

  def edit(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Prize.lookup(id).map { prize =>
        helpers.Security.verifyIfAllowed(hid == prize.hackathonId)(request.user)
        helpers.Security.verifyIfAllowed(prize.hackathon.organiserId)(request.user)
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
        BadRequest(views.html.prizes.edit(model.Hackathon.lookup(hid), id, errors, request.user))
      },
      prize => transaction {
        model.Prize.lookup(id).map { prize =>
          helpers.Security.verifyIfAllowed(hid == prize.hackathonId)(request.user)
          helpers.Security.verifyIfAllowed(prize.hackathon.organiserId)(request.user)
        }
        //TODO check if updated
        model.Prize.update(id, prize)
        Redirect(routes.Prize.index(hid)).flashing("status" -> "updated", "title" -> prize.name)
      })
  }

  def delete(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Prize.lookup(id).map { prize =>
        helpers.Security.verifyIfAllowed(hid == prize.hackathonId)(request.user)
        helpers.Security.verifyIfAllowed(prize.hackathon.organiserId)(request.user)
      }
      //TODO check if deleted
      model.Prize.delete(id)
      Redirect(routes.Prize.index(hid)).flashing("status" -> "deleted")
    }
  }
}