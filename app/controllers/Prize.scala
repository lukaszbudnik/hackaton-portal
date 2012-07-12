package controllers

import org.squeryl.PrimitiveTypeMode._
import play.api.data.Forms._
import play.api.data.Form
import play.api.mvc.Controller

object Prize extends Controller with securesocial.core.SecureSocial {

  def index = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.prizes.index(model.Prize.allOrdered.toList, request.user))
    }
  }

  def view(id: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.prizes.view(model.Prize.lookup(id), request.user))
    }
  }

  val prizeForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "order" -> number,
      "founderName" -> optional(nonEmptyText),
      "founderWebPage" -> optional(nonEmptyText),
      "hackathonId" -> longNumber)(model.Prize.apply)(model.Prize.unapply))

  def create = SecuredAction() { implicit request =>
    transaction {
      Ok(views.html.prizes.create(prizeForm, model.Hackathon.all.toList, request.user))
    }
  }

  def save = SecuredAction() { implicit request =>
    prizeForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.prizes.create(errors, model.Hackathon.all.toList, request.user))
      }, prize => transaction {
        model.Prize.insert(prize)
        Redirect(routes.Prize.index).flashing("status" -> "prizes.added")
      })
  }

  def edit(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Prize.lookup(id).map { prize =>
        Ok(views.html.prizes.edit(id, prizeForm.fill(prize), model.Hackathon.all.toList, request.user))
      }.get
    }
  }

  def update(id: Long) = SecuredAction() { implicit request =>
    prizeForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.prizes.edit(id, errors, model.Hackathon.all.toList, request.user))
      },
      prize => transaction {
        model.Prize.update(id, prize)
        Redirect(routes.Prize.index).flashing("status" -> "prizes.updated", "title" -> prize.name)
      })
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Prize.delete(id)
    }
    Redirect(routes.Prize.index).flashing("status" -> "prizes.deleted")
  }

}