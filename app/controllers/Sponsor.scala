package controllers

import java.io.FileInputStream
import org.squeryl.PrimitiveTypeMode.__thisDsl
import org.squeryl.PrimitiveTypeMode.long2ScalarLong
import org.squeryl.PrimitiveTypeMode.transaction
import play.api.Play.current
import play.api.data.Forms.boolean
import play.api.data.Forms.list
import play.api.data.Forms.longNumber
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.data.Form
import play.api.i18n.Lang
import play.api.i18n.Messages
import play.api.libs.json.Json.toJson
import play.api.libs.json.JsArray
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.mvc.Controller
import play.api.Logger
import play.api.Play
import plugins.cloudimage.CloudImageErrorResponse
import plugins.cloudimage.CloudImagePlugin
import plugins.cloudimage.CloudImageService
import plugins.cloudimage.CloudImageSuccessResponse
import plugins.use
import plugins.cloudimage.TransformationProperty
import model.dto.SponsorWithLogo
import model.Resource

object Sponsor extends LangAwareController with securesocial.core.SecureSocial {

  private lazy val cloudImageService = use[CloudImagePlugin].cloudImageService

  /* Transformation properties for displaying a sponsor logo
   * We try to resize the image according to the predefined configuration (see application.conf) proportionally so that
   * the image fits into defined boundaries 
   */
  private lazy val SPONSOR_LOGO_TRANSFORMATION_PROPS = Map[TransformationProperty.Value, String](
    TransformationProperty.WIDTH -> Play.current.configuration.getString("sponsors.logo.maxwidth").getOrElse(""),
    TransformationProperty.HEIGHT -> Play.current.configuration.getString("sponsors.logo.maxheight").getOrElse(""),
    TransformationProperty.CROP_MODE -> "c_fit");

  // shortcut for image transformation
  private def transformLogo(url: String) = {
    cloudImageService.getTransformationUrl(url, SPONSOR_LOGO_TRANSFORMATION_PROPS)
  }

  val sponsorForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "title" -> nonEmptyText,
      "description" -> nonEmptyText,
      "website" -> nonEmptyText,
      "order" -> number,
      "hackathonId" -> optional(longNumber),
      "logoResourceId" -> optional(longNumber),
      "logoUrl" -> optional(nonEmptyText)) // apply
      ((name,
        title,
        description,
        website,
        order,
        hackathonId,
        logoResourceId,
        logoUrl) => new model.dto.SponsorWithLogo(
        new model.Sponsor(name,
          title,
          description,
          website,
          order,
          hackathonId,
          logoResourceId), if (logoUrl.isDefined) Some(new model.Resource(logoUrl.get, ""))
        else None)) // unapply 		
        ((sl: SponsorWithLogo) =>
        Some(sl.sponsor.name, sl.sponsor.title, sl.sponsor.description, sl.sponsor.website, sl.sponsor.order, sl.sponsor.hackathonId, sl.sponsor.logoResourceId, sl.logo.map(l => transformLogo(l.url)))))

  def uploadError(implicit text: String, lang: Lang) = toJson(Seq(toJson(Map("error" -> toJson(Messages(text))))))

  def index = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.sponsors.index(model.dto.SponsorWithLogo.portalSponsors, userFromRequest))
    }
  }

  def indexH(hid: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.sponsors.indexH(model.Hackathon.lookup(hid), model.dto.SponsorWithLogo.hackathonSponsors(hid), userFromRequest))
    }
  }

  def view(id: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.sponsors.view(model.dto.SponsorWithLogo.lookup(id), userFromRequest))
    }
  }

  def viewH(hid: Long, id: Long) = UserAwareAction { implicit request =>
    transaction {
      val sponsor = model.dto.SponsorWithLogo.lookup(id)
      val hackathon = model.Hackathon.lookup(hid)

      Ok(views.html.sponsors.viewH(hackathon, sponsor, userFromRequest))
    }
  }

  def create = SecuredAction { implicit request =>
    transaction {
      ensureAdmin {
        val user = userFromRequest(request)
        val sponsor = new model.dto.SponsorWithLogo()
        Ok(views.html.sponsors.create(sponsorForm.fill(sponsor), user))
      }
    }
  }

  def createH(hid: Long) = SecuredAction { implicit request =>
    transaction {
      model.Hackathon.lookup(hid).map { hackathon =>
        ensureHackathonOrganiserOrAdmin(hackathon) {
          val user = userFromRequest(request)
          val sponsor = new model.dto.SponsorWithLogo(new model.Sponsor(Some(hid)), None)
          Ok(views.html.sponsors.createH(Some(hackathon), sponsorForm.fill(sponsor), user))
        }
      }.getOrElse(Redirect(routes.Hackathon.view(hid)))
    }
  }

  def save = SecuredAction { implicit request =>
    val user = userFromRequest(request)
    sponsorForm.bindFromRequest.fold(
      errors => BadRequest(views.html.sponsors.create(errors, user)),
      sponsorWithLogo => transaction {
        ensureAdmin {
          model.Sponsor.insert(sponsorWithLogo.sponsor)
          Redirect(routes.Sponsor.index).flashing("status" -> "added", "title" -> sponsorWithLogo.sponsor.name)
        }
      })
  }

  def saveH(hid: Long) = SecuredAction { implicit request =>
    sponsorForm.bindFromRequest.fold(
      errors => transaction {
        val user = userFromRequest(request)
        BadRequest(views.html.sponsors.createH(model.Hackathon.lookup(hid), errors, user))
      },
      sponsorWithLogo => transaction {
        model.Hackathon.lookup(hid).map { hackathon =>
          ensureHackathonOrganiserOrAdmin(hackathon) {
            model.Sponsor.insert(sponsorWithLogo.sponsor)

            Redirect(routes.Sponsor.indexH(hid)).flashing("status" -> "added", "title" -> sponsorWithLogo.sponsor.name)
          }
        }.getOrElse(Redirect(routes.Hackathon.view(hid)))
      })
  }

  def edit(id: Long) = SecuredAction { implicit request =>
    transaction {
      model.dto.SponsorWithLogo.lookup(id).map { sponsorWithLogo =>
        ensureAdmin {
          val user = userFromRequest(request)
          Ok(views.html.sponsors.edit(id, sponsorForm.fill(sponsorWithLogo), user))
        }
      }.getOrElse (Redirect(routes.Sponsor.view(id)))
    }
  }

  def editH(hid: Long, id: Long) = SecuredAction { implicit request =>
    transaction {
      
      model.Hackathon.lookup(hid).map { hackathon =>
        
        model.dto.SponsorWithLogo.lookup(id).filter(_.sponsor.hackathonId == Some(hid)).map { sponsorWithLogo =>
          
          ensureHackathonOrganiserOrAdmin(hackathon) {
            val user = userFromRequest(request)
            Ok(views.html.sponsors.editH(Some(hackathon), id, sponsorForm.fill(sponsorWithLogo), user))
          }
          
        }.getOrElse(Redirect(routes.Sponsor.viewH(hid, id)))
        
      }.getOrElse(Redirect(routes.Hackathon.view(hid)))
      
    }
  }

  def update(id: Long) = SecuredAction { implicit request =>
    val user = userFromRequest(request)
    sponsorForm.bindFromRequest.fold(
      errors => BadRequest(views.html.sponsors.edit(id, errors, user)),
      sponsorWithLogo => transaction {
        ensureAdmin {
          model.Sponsor.update(id, sponsorWithLogo.sponsor)
          Redirect(routes.Sponsor.index).flashing("status" -> "updated", "title" -> sponsorWithLogo.sponsor.name)
        }
      })
  }

  def updateH(hid: Long, id: Long) = SecuredAction { implicit request =>
    sponsorForm.bindFromRequest.fold(
      errors => transaction {
    	  val user = userFromRequest(request)
        BadRequest(views.html.sponsors.editH(model.Hackathon.lookup(hid), id, errors, user))
      },
      sponsorWithLogo => transaction {
        model.Hackathon.lookup(hid).map { hackathon =>

          model.dto.SponsorWithLogo.lookup(id).map { sponsorWithLogo =>

            ensureHackathonOrganiserOrAdmin(hackathon) {
              model.Sponsor.update(id, sponsorWithLogo.sponsor)
              Redirect(routes.Sponsor.indexH(hid)).flashing("status" -> "updated", "title" -> sponsorWithLogo.sponsor.name)
            }

          }.getOrElse(Redirect(routes.Sponsor.viewH(hid, id)))

        }.getOrElse(Redirect(routes.Hackathon.view(hid)))
      })
  }

  def delete(id: Long) = SecuredAction { implicit request =>
    transaction {
      ensureAdmin {
        model.Sponsor.delete(id)
        Redirect(routes.Sponsor.index).flashing("status" -> "deleted")
      }
    }
  }

  def deleteH(hid: Long, id: Long) = SecuredAction { implicit request =>
    transaction {

      model.Hackathon.lookup(hid).map { hackathon =>

        model.Sponsor.lookup(id).filter(_.hackathonId == Some(hid)).map { sponsor =>

          ensureHackathonOrganiserOrAdmin(hackathon) {
            model.Sponsor.delete(id)
            Redirect(routes.Sponsor.indexH(hid)).flashing("status" -> "deleted")
          }

        }.getOrElse(Redirect(routes.Sponsor.viewH(hid, id)))

      }.getOrElse(Redirect(routes.Hackathon.view(hid)))

    }
  }

  private def logoDetailsAsJson(url: String, resourceId: Long) = {
    toJson(Seq(JsObject(List(
      "url" -> JsString(transformLogo(url)),
      "resourceId" -> JsNumber(resourceId)))))
  }

  def uploadLogo = UserAwareAction(parse.multipartFormData) { implicit request =>
    var temporaryHandle = request.body.file("files").get
    val temporaryFile = temporaryHandle.ref.file

    val in = new FileInputStream(temporaryFile)
    val bytes = new Array[Byte](temporaryFile.length.toInt);
    in.read(bytes)
    in.close()

    val maxSize = Play.current.configuration.getString("sponsors.logo.maxsize").getOrElse("0").toLong * 1024

    {
      if (bytes.length > maxSize) {
        Ok(uploadError("js.fileupload.filetoobig", lang))
      } else {

        val filename = temporaryHandle.filename
        val response = cloudImageService.upload(filename, bytes)

        response match {
          case success: CloudImageSuccessResponse =>
            transaction {
              val resource = model.Resource(success.url, success.publicId);
              model.Resource.insert(resource)
              Ok(logoDetailsAsJson(resource.url, resource.id))
            }
          case error: CloudImageErrorResponse =>
            Logger.debug("Sponsor - cloudinaryService - error: " + error.message)
            Ok(uploadError("fileupload.server.error", lang))
        }
      }
    }.withHeaders(CONTENT_TYPE -> "text/plain")
  }

  def getLogoDetails(id: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(model.Resource.lookup(id).map {
        r => logoDetailsAsJson(r.url, r.id)
      }.get)
    }
  }
}