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
    inTransaction {
      Ok(views.html.prizes.index(model.Hackathon.lookup(hid), userFromRequest))
    }
  }

  def view(hid: Long, id: Long) = UserAwareAction { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      val hackathon = model.Hackathon.lookup(hid)
      hackathon.map { hackathon =>

        model.Prize.lookup(id).filter(_.hackathonId == hid).map { prize =>
          Ok(views.html.prizes.view(Some(hackathon), Some(prize), userFromRequest))

        }.getOrElse {
          NotFound(views.html.prizes.view(Some(hackathon), None, user))
        }

      }.getOrElse {
        NotFound(views.html.hackathons.view(None, user))
      }

    }
  }

  def create(hid: Long) = SecuredAction { implicit request =>
    inTransaction {
      model.Hackathon.lookup(hid).map { hackathon =>
        ensureHackathonOrganiserOrAdmin(hackathon) {
          val user = userFromRequest(request)
          val prize = new model.Prize(1, hid)
          Ok(views.html.prizes.create(Some(hackathon), prizeForm.fill(prize), user))
        }
      }.getOrElse(Redirect(routes.Hackathon.view(hid)))
    }
  }

  def save(hid: Long) = SecuredAction { implicit request =>
    inTransaction {
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

  def edit(hid: Long, id: Long) = SecuredAction { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      model.Hackathon.lookup(hid).map { hackathon =>
        model.Prize.lookup(id).filter(_.hackathonId == hid).map { prize =>
          ensureHackathonOrganiserOrAdmin(hackathon) {
            Ok(views.html.prizes.edit(Some(prize.hackathon), id, prizeForm.fill(prize), user))
          }
        }.getOrElse {
          NotFound(views.html.prizes.view(Some(hackathon), None, Some(user)))
        }
      }.getOrElse {
        NotFound(views.html.hackathons.view(None, Some(user)))
      }
    }
  }

  def update(hid: Long, id: Long) = SecuredAction { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      model.Hackathon.lookup(hid).map { hackathon =>
        model.Prize.lookup(id).filter(_.hackathonId == hid).map { prize =>

          ensureHackathonOrganiserOrAdmin(hackathon) {
            prizeForm.bindFromRequest.fold(
              errors => BadRequest(views.html.prizes.edit(Some(hackathon), id, errors, user)),
              prize => {
                model.Prize.update(id, prize)
                Redirect(routes.Prize.index(hid)).flashing("status" -> "updated", "title" -> prize.name)
              })
          }

        }.getOrElse {
          NotFound(views.html.prizes.view(Some(hackathon), None, Some(user)))
        }
      }.getOrElse {
        NotFound(views.html.hackathons.view(None, Some(user)))
      }
    }
  }

  def delete(hid: Long, id: Long) = SecuredAction { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      model.Hackathon.lookup(hid).map { hackathon =>
        model.Prize.lookup(id).filter(_.hackathonId == hid).map { prize =>

          ensureHackathonOrganiserOrAdmin(hackathon) {
            model.Prize.delete(id)
            Redirect(routes.Prize.index(hid)).flashing("status" -> "deleted")
          }

        }.getOrElse {
          NotFound(views.html.prizes.view(Some(hackathon), None, Some(user)))
        }
      }.getOrElse {
        NotFound(views.html.hackathons.view(None, Some(user)))
      }

    }
  }
}
