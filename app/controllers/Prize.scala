package controllers

import org.squeryl.PrimitiveTypeMode._
import play.api.data.Forms._
import play.api.data.Form
import play.api.mvc.Controller

object Prize extends LangAwareController {

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
      Ok(views.html.prizes.index(model.Hackathon.lookup(hid), userFromRequest))
    }
  }

  def view(hid: Long, id: Long) = UserAwareAction { implicit request =>
    transaction {
      val prize = model.Prize.lookup(id)
      val hackathon = prize.map { p => Some(p.hackathon) }.getOrElse { model.Hackathon.lookup(hid) }
      Ok(views.html.prizes.view(hackathon, prize, userFromRequest))
    }
  }

  def create(hid: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Hackathon.lookup(hid).map { hackathon =>
        ensureHackathonOrganiserOrAdmin(hackathon) {
          val user = userFromRequest(request)
          val prize = new model.Prize(1, hid)
          Ok(views.html.prizes.create(Some(hackathon), prizeForm.fill(prize), user))
        }
      }.getOrElse(Redirect(routes.Hackathon.view(hid)))
    }
  }

  def save(hid: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Hackathon.lookup(hid).map { hackathon =>
        val user = userFromRequest(request)
        prizeForm.bindFromRequest.fold(
          errors => BadRequest(views.html.prizes.create(Some(hackathon), errors, user)),
          prize => {
            model.Prize.insert(prize)
            Redirect(routes.Prize.index(hid)).flashing("status" -> "added", "title" -> prize.name)
          })
      }.getOrElse(Redirect(routes.Hackathon.view(hid)))
    }
  }

  def edit(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Hackathon.lookup(id).map { hackathon =>
        model.Prize.lookup(id).map { prize =>
          ensureHackathonOrganiserOrAdmin(hackathon, hid == prize.hackathonId) {
            val user = userFromRequest(request)
            Ok(views.html.prizes.edit(Some(prize.hackathon), id, prizeForm.fill(prize), user))
          }
        }.getOrElse(Redirect(routes.Prize.view(hid, id)))
      }.getOrElse(Redirect(routes.Hackathon.view(hid)))
    }
  }

  def update(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Hackathon.lookup(id).map { hackathon =>
        model.Prize.lookup(id).map { prize =>

          ensureHackathonOrganiserOrAdmin(hackathon, hid == prize.hackathonId) {

            val user = userFromRequest(request)
            prizeForm.bindFromRequest.fold(
              errors => BadRequest(views.html.prizes.edit(Some(hackathon), id, errors, user)),
              prize => {
                model.Prize.update(id, prize)
                Redirect(routes.Prize.index(hid)).flashing("status" -> "updated", "title" -> prize.name)
              })

          }

        }.getOrElse(Redirect(routes.Prize.view(hid, id)))
      }.getOrElse(Redirect(routes.Hackathon.view(hid)))
    }
  }

  def delete(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {

      model.Hackathon.lookup(id).map { hackathon =>
        model.Prize.lookup(id).map { prize =>

          ensureHackathonOrganiserOrAdmin(hackathon, hid == prize.hackathonId) {
            model.Prize.delete(id)
            Redirect(routes.Prize.index(hid)).flashing("status" -> "deleted")
          }

        }.getOrElse(Redirect(routes.Prize.view(hid, id)))
      }.getOrElse(Redirect(routes.Hackathon.view(hid)))

    }
  }
}