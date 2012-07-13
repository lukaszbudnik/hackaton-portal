package controllers

import org.squeryl.PrimitiveTypeMode._
import play.api.data.Forms._
import play.api.data.Form
import play.api.mvc.Controller
import javax.persistence.OrderBy
import model.HackathonSponsorHelper

object Sponsor extends Controller with securesocial.core.SecureSocial {

  val sponsorForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "website" -> nonEmptyText,
      "order" -> number,
      "isGeneralSponsor" -> boolean,
      "hackathons" -> list(mapping(
        "hackathonId" -> longNumber,
        "order" -> number)((model.HackathonSponsorHelper).apply)(model.HackathonSponsorHelper.unapply)))(model.Sponsor.apply)(model.Sponsor.unapply))

  def index = UserAwareAction { implicit request =>
    transaction {

      val sponsors: Map[Long, model.Sponsor] = model.Sponsor.all.map({ s => (s.id, s) }).toMap

      val generalSponsors: Iterable[Long] = model.Sponsor.allGeneralSponsorsOrdered.map { s => s.id }

      val hackathonSponsors: Map[Long, Iterable[Long]] =
        model.Hackathon.all.toList.map({ h =>
          (
            h.id,
            h.sponsors.map { s => s.id })
        }).toMap

      Ok(views.html.sponsors.index(model.Hackathon.all, sponsors, generalSponsors, hackathonSponsors, request.user))

    }
  }

  def view(id: Long) = UserAwareAction { implicit request =>
    transaction {
      val hs = model.Sponsor.hackathonsToSponsors.where(hs => hs.sponsorId === id).map({ hs => (hs.hackathonId, hs.order) }).toMap
      Ok(views.html.sponsors.view(model.Sponsor.lookup(id), hs, request.user))
    }
  }

  def create = SecuredAction() { implicit request =>
    transaction {
      Ok(views.html.sponsors.create(sponsorForm, model.Hackathon.all.toList, request.user))
    }
  }

  def save = SecuredAction() { implicit request =>
    sponsorForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.sponsors.create(errors, model.Hackathon.all.toList, request.user))
      }, sponsor => transaction {

        model.Sponsor.insert(sponsor)

        sponsor.hackathonsList.map { hs =>

          if (hs.order != -1) {

            val hackathon = model.Hackathon.lookup(hs.hackathonId).get
            //TODO: handle case with no hackathon returned from db
            val hsTable: model.HackathonSponsor = model.HackathonSponsor(hs.hackathonId, sponsor.id, hs.order)
            sponsor.hackathons.associate(hackathon, hsTable)
          }
        }

        Redirect(routes.Sponsor.index).flashing("status" -> "sponsors.added")
      })
  }

  def edit(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Sponsor.lookup(id).map { sponsor =>

        val hackathons = model.Hackathon.all.toList

        val hsList: List[model.HackathonSponsorHelper] =
          hackathons.map({ hackathon =>
            model.HackathonSponsorHelper(
              hackathon.id,
              model.Sponsor.findSponsorHackatonOrder(id, hackathon.id).getOrElse(-1))
          }).toList

        Ok(views.html.sponsors.edit(id, sponsorForm.fill(sponsor.copy(hackathonsList = hsList)), hackathons, request.user))

      }.get
    }
  }

  def update(id: Long) = SecuredAction() { implicit request =>
    sponsorForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.sponsors.edit(id, errors, model.Hackathon.all.toList, request.user))
      },
      sponsor => transaction {
        model.Sponsor.update(id, sponsor)
        sponsor.hackathonsList.map { hs =>

          if (hs.order == -1)
            model.Sponsor.deleteSponsorHackathon(id, hs.hackathonId)
          else
            model.Sponsor.updateSponsorHackathonOrder(id, hs.hackathonId, hs.order)

        }

        Redirect(routes.Sponsor.index).flashing("status" -> "sponsors.updated", "title" -> sponsor.name)
      })
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    transaction {

      // TODO squeryl cascade
      model.Sponsor.lookup(id).map { sponsor =>
        sponsor.hackathons.dissociateAll
      }

      model.Sponsor.delete(id)
    }
    Redirect(routes.Sponsor.index).flashing("status" -> "sponsors.deleted")
  }

}