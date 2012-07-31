package controllers

import java.io.FileInputStream
import org.squeryl.PrimitiveTypeMode.__thisDsl
import org.squeryl.PrimitiveTypeMode.long2ScalarLong
import org.squeryl.PrimitiveTypeMode.transaction
import core.LangAwareController
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

object Sponsor extends LangAwareController with securesocial.core.SecureSocial {

  val sponsorForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "title" -> nonEmptyText,
      "description" -> nonEmptyText,
      "website" -> nonEmptyText,
      "order" -> number,
      "hackathonId" -> optional(longNumber),
      "logoResourceId" -> optional(longNumber),
      "logoUrl" -> optional(nonEmptyText))(model.Sponsor.apply)(model.Sponsor.unapply))

  def uploadError(implicit text : String,  lang : Lang) = toJson(Seq(toJson(Map("error" -> toJson(Messages(text))))))

  lazy val cloudImageService = use[CloudImagePlugin].cloudImageService
  
  lazy val SPONSOR_LOGO_TRANSFORMATION_PROPS = Map[TransformationProperty.Value, String] (
    TransformationProperty.WIDTH -> Play.current.configuration.getString("sponsors.logo.maxwidth").getOrElse(""),
    TransformationProperty.HEIGHT -> Play.current.configuration.getString("sponsors.logo.maxheight").getOrElse(""), 
    TransformationProperty.CROP_MODE -> "c_fit");
          

                                                                                                                                                                                                                                                        
  def index = UserAwareAction { implicit request =>
    transaction {   	
    	Ok(views.html.sponsors.index(model.Sponsor.all.map {
	        s =>
	          s.logoUrl = s.logoUrl.map (cloudImageService.getTransformationUrl(_, SPONSOR_LOGO_TRANSFORMATION_PROPS))
	          s
	      }, request.user))
    }
  }

  def indexH(hid: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.sponsors.indexH(model.Hackathon.lookup(hid), request.user))
    }
  }

  def view(id: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.sponsors.view(
          model.Sponsor.lookup(id) match {
            case Some(s) =>
              	s.logoUrl = s.logoUrl.map (cloudImageService.getTransformationUrl(_, SPONSOR_LOGO_TRANSFORMATION_PROPS))
              	Some(s)
            case _ => None
      }, request.user))
    }
  }

  def viewH(hid: Long, id: Long) = UserAwareAction { implicit request =>
    transaction {
      val sponsor = model.Sponsor.lookup(id) match {
            case Some(s) =>
              	s.logoUrl = s.logoUrl.map (cloudImageService.getTransformationUrl(_, SPONSOR_LOGO_TRANSFORMATION_PROPS))
              	Some(s)
            case _ => None        
      }
      val hackathon = sponsor.map { sponsor => sponsor.hackathon }.getOrElse { model.Hackathon.lookup(hid) }
      Ok(views.html.sponsors.viewH(hackathon, sponsor, request.user))
    }
  }

  def create = SecuredAction() { implicit request =>
    transaction {
      helpers.Security.verifyIfAllowed(request.user)
      val sponsor = new model.Sponsor(None)
      Ok(views.html.sponsors.create(sponsorForm.fill(sponsor), request.user))
    }
  }

  def createH(hid: Long) = SecuredAction() { implicit request =>
    transaction {
      val hackathon = model.Hackathon.lookup(hid)
      hackathon.map { h =>
        helpers.Security.verifyIfAllowed(h.organiserId)(request.user)
      }
      val sponsor = new model.Sponsor(Some(hid))
      Ok(views.html.sponsors.createH(hackathon, sponsorForm.fill(sponsor), request.user))
    }
  }

  def save = SecuredAction() { implicit request =>
    sponsorForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.sponsors.create(errors, request.user))
      },
      sponsor => transaction {
        helpers.Security.verifyIfAllowed(request.user)
        model.Sponsor.insert(sponsor)
        Redirect(routes.Sponsor.index).flashing("status" -> "added", "title" -> sponsor.name)
      })
  }

  def saveH(hid: Long) = SecuredAction() { implicit request =>
    sponsorForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.sponsors.createH(model.Hackathon.lookup(hid), errors, request.user))
      },
      sponsor => transaction {
        model.Hackathon.lookup(hid).map { h =>
          helpers.Security.verifyIfAllowed(h.organiserId)(request.user)
        }
        model.Sponsor.insert(sponsor)
        Redirect(routes.Sponsor.indexH(hid)).flashing("status" -> "added", "title" -> sponsor.name)
      })
  }

  def edit(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Sponsor.lookup(id).map { sponsor =>
        helpers.Security.verifyIfAllowed(request.user)
        Ok(views.html.sponsors.edit(id, sponsorForm.fill(sponsor), request.user))
      }.getOrElse {
        // no sponsor found
        Redirect(routes.Sponsor.view(id)).flashing()
      }
    }
  }

  def editH(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Sponsor.lookup(id).map { sponsor =>
        helpers.Security.verifyIfAllowed(Some(hid) == sponsor.hackathonId)(request.user)
        sponsor.hackathon.map { h =>
          helpers.Security.verifyIfAllowed(h.organiserId)(request.user)
        }.getOrElse {
          helpers.Security.verifyIfAllowed()(request.user)
        }
        Ok(views.html.sponsors.editH(sponsor.hackathon, id, sponsorForm.fill(sponsor), request.user))
      }.getOrElse {
        // no sponsor found
        Redirect(routes.Sponsor.viewH(hid, id)).flashing()
      }
    }
  }

  def update(id: Long) = SecuredAction() { implicit request =>
    sponsorForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.sponsors.edit(id, errors, request.user))
      },
      sponsor => transaction {
        helpers.Security.verifyIfAllowed(request.user)
        model.Sponsor.update(id, sponsor)
        Redirect(routes.Sponsor.index).flashing("status" -> "updated", "title" -> sponsor.name)
      })
  }

  def updateH(hid: Long, id: Long) = SecuredAction() { implicit request =>
    sponsorForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.sponsors.editH(model.Hackathon.lookup(hid), id, errors, request.user))
      },
      sponsor => transaction {
        model.Sponsor.lookup(id).map { sponsor =>
          helpers.Security.verifyIfAllowed(Some(hid) == sponsor.hackathonId)(request.user)
          sponsor.hackathon.map { h =>
            helpers.Security.verifyIfAllowed(h.organiserId)(request.user)
          }.getOrElse {
            helpers.Security.verifyIfAllowed(request.user)
          }
        }
        model.Sponsor.update(id, sponsor)
        Redirect(routes.Sponsor.indexH(hid)).flashing("status" -> "updated", "title" -> sponsor.name)
      })
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    transaction {
      helpers.Security.verifyIfAllowed(request.user)
      model.Sponsor.delete(id)
      Redirect(routes.Sponsor.index).flashing("status" -> "deleted")
    }
  }

  def deleteH(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Sponsor.lookup(id).map { sponsor =>
        helpers.Security.verifyIfAllowed(Some(hid) == sponsor.hackathonId)(request.user)
        sponsor.hackathon.map { h =>
          helpers.Security.verifyIfAllowed(h.organiserId)(request.user)
        }.getOrElse {
          helpers.Security.verifyIfAllowed(request.user)
        }
      }
      model.Sponsor.delete(id)
    }
    Redirect(routes.Sponsor.indexH(hid)).flashing("status" -> "deleted")
  }

  def uploadLogo = UserAwareAction(parse.multipartFormData) { implicit request =>
    var temporaryHandle = request.body.file("files").get
    val temporaryFile = temporaryHandle.ref.file

    val in = new FileInputStream(temporaryFile)
    val bytes = new Array[Byte](temporaryFile.length.toInt);
    in.read(bytes)
    in.close()
    
    val maxSize = Play.current.configuration.getString("sponsors.logo.maxsize").getOrElse("0").toLong * 1024

    if(bytes.length > maxSize) {
      Ok(uploadError("js.fileupload.filetoobig", lang))
    } else {
	
	    val filename = temporaryHandle.filename
	    val response = cloudImageService.upload(filename, bytes)

	    response match {
	      case success: CloudImageSuccessResponse =>
	        transaction {
	          val resource = model.Resource(success.url, success.publicId);
	          model.Resource.insert(resource)
	          val result = Ok(toJson((Seq(JsObject(List(
	            "url" -> JsString(cloudImageService.getTransformationUrl(resource.url, SPONSOR_LOGO_TRANSFORMATION_PROPS)),
	            "resourceId" -> JsNumber(resource.id)))))))
	          result.withHeaders(CONTENT_TYPE -> "text/plain")
	        }
	      case error: CloudImageErrorResponse =>
	        Logger.debug("Sponsor - cloudinaryService - error: " + error.message)
	        Ok(uploadError("fileupload.server.error", lang)).withHeaders(CONTENT_TYPE -> "text/plain")
	    }
    }
  }

  def getLogoDetails(id: Long) = UserAwareAction { implicit request =>
    transaction {
      val r = model.Resource.lookup(id).get
      Ok(JsArray(Seq(JsObject(List(
        "url" -> JsString(cloudImageService.getTransformationUrl(r.url, SPONSOR_LOGO_TRANSFORMATION_PROPS)),
        "resourceId" -> JsNumber(r.id))))))
    }
  }
}