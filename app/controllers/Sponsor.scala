package controllers

import java.io.FileInputStream

import scala.math.BigDecimal.long2bigDecimal

import org.squeryl.PrimitiveTypeMode.__thisDsl
import org.squeryl.PrimitiveTypeMode.long2ScalarLong
import org.squeryl.PrimitiveTypeMode.transaction

import model.Resource
import play.api.data.Forms.boolean
import play.api.data.Forms.list
import play.api.data.Forms.longNumber
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.Json.toJson
import play.api.libs.json.JsArray
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.mvc.Controller
import play.api.Logger
import plugin.cloudimage.CloudImageErrorResponse
import plugin.cloudimage.CloudImageService
import plugin.cloudimage.CloudImageSuccessResponse

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
        "order" -> number)((model.HackathonSponsorHelper).apply)(model.HackathonSponsorHelper.unapply)),
      "logoResourceId" -> optional(longNumber))(model.Sponsor.apply)(model.Sponsor.unapply))

  val uploadError = toJson(Seq(toJson(Map("error" -> toJson(Messages("fileupload.server.error"))))))

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

  def uploadLogo = UserAwareAction(parse.multipartFormData) { implicit request =>

    var temporaryHandle = request.body.file("files").get
    val temporaryFile = temporaryHandle.ref.file
    val in = new FileInputStream(temporaryFile)
    val bytes = new Array[Byte](temporaryFile.length.toInt);
    in.read(bytes)
    in.close()
    val filename = temporaryHandle.filename
    val response = CloudImageService.upload(filename, bytes)

    response match {
      case success: CloudImageSuccessResponse =>

        transaction {
          val res = new Resource(success.url, success.publicId);
          val newRes = Resource.insert(res)

          val result = Ok(toJson((Seq(JsObject(List(
            "url" -> JsString(newRes.url),
            "resourceId" -> JsNumber(newRes.id)))))))

          result.withHeaders(CONTENT_TYPE -> "text/plain")
        }
      case error: CloudImageErrorResponse =>

        Logger.debug("Sponsor - cloudinaryService - error: " + error.message)
        Ok(uploadError)
    }

  }

  def getLogoDetails(id: Long) = UserAwareAction { implicit request =>

    transaction {
      val r = Resource.lookup(id).get
      Ok(JsArray(Seq(JsObject(List(
        "url" -> JsString(r.url),
        "resourceId" -> JsNumber(r.id))))))
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

        model.Sponsor.lookup(id).map { oldSponsor =>

          if (oldSponsor.logoResourceId.isDefined && sponsor.logoResourceId.isEmpty) {
            val resId = oldSponsor.logoResourceId.get
            model.Resource.lookup(resId) map { resource =>
              model.Sponsor.update(id, sponsor)
              model.Resource.delete(resId);
              CloudImageService.destroy(resource.publicId)
            }
          } else {
            model.Sponsor.update(id, sponsor)
          }

          model.Sponsor.update(id, sponsor)
          sponsor.hackathonsList.map { hs =>

            if (hs.order == -1)
              model.Sponsor.deleteSponsorHackathon(id, hs.hackathonId)
            else
              model.Sponsor.updateSponsorHackathonOrder(id, hs.hackathonId, hs.order)
          }
        }
        Redirect(routes.Sponsor.index).flashing("status" -> "sponsors.updated", "title" -> sponsor.name)
      })
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    transaction {

      // TODO squeryl cascade
      model.Sponsor.lookup(id).map { sponsor =>
        sponsor.hackathons.dissociateAll
        model.Sponsor.delete(id)
        sponsor.logoResourceId map { rId =>
          model.Resource.lookup(rId) map { resource =>
            model.Resource.delete(resource.id)
            CloudImageService.destroy(resource.publicId)
          }
        }
      }

    }
    Redirect(routes.Sponsor.index).flashing("status" -> "sponsors.deleted")
  }

}




 