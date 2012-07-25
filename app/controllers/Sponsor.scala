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
import play.api.i18n.Messages
import play.api.libs.json.Json.toJson
import play.api.libs.json.JsArray
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.mvc.Controller
import play.api.Logger
import plugins.cloudimage.CloudImageErrorResponse
import plugins.cloudimage.CloudImagePlugin
import plugins.cloudimage.CloudImageService
import plugins.cloudimage.CloudImageSuccessResponse
import plugins.use

object Sponsor extends Controller with securesocial.core.SecureSocial {

  val sponsorForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "title" -> nonEmptyText,
      "description" -> nonEmptyText,
      "website" -> nonEmptyText,
      "order" -> number,
      "hackathonId" -> optional(longNumber),
      "logoResourceId" -> optional(longNumber))(model.Sponsor.apply)(model.Sponsor.unapply))

  val uploadError = toJson(Seq(toJson(Map("error" -> toJson(Messages("fileupload.server.error"))))))

  def index = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.sponsors.index(model.Sponsor.all, request.user))
    }
  }

  def view(id: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.sponsors.view(model.Sponsor.lookup(id), request.user))
    }
  }

  def create = SecuredAction() { implicit request =>
    transaction {
      val sponsor = new model.Sponsor(None)
      Ok(views.html.sponsors.create(sponsorForm.fill(sponsor), request.user))
    }
  }

  def save = SecuredAction() { implicit request =>
    sponsorForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.sponsors.create(errors, request.user))
      },
      sponsor => transaction {
        model.Sponsor.insert(sponsor)
        Redirect(routes.Sponsor.index).flashing("status" -> "added", "title" -> sponsor.name)
      })
  }

  def edit(id: Long) = SecuredAction() { implicit request =>
  	transaction {
      model.Sponsor.lookup(id).map { sponsor =>
        Ok(views.html.sponsors.edit(id, sponsorForm.fill(sponsor), request.user))
      }.getOrElse {
        // no news found
        Redirect(routes.Sponsor.view(id)).flashing()
      }
    }
  }

  def update(id: Long) = SecuredAction() { implicit request =>
    sponsorForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.sponsors.edit(id, errors, request.user))
      },
      sponsor => transaction {
        model.Sponsor.update(id, sponsor)
        Redirect(routes.Sponsor.index).flashing("status" -> "updated", "title" -> sponsor.name)
      })
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Sponsor.delete(id)
      Redirect(routes.Sponsor.index).flashing("status" -> "deleted")
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
    val response = use[CloudImagePlugin].cloudImageService.upload(filename, bytes)

    response match {
      case success: CloudImageSuccessResponse =>
        transaction {
          val resource = model.Resource(success.url, success.publicId);
          model.Resource.insert(resource)
          val result = Ok(toJson((Seq(JsObject(List(
            "url" -> JsString(resource.url),
            "resourceId" -> JsNumber(resource.id)))))))
          result.withHeaders(CONTENT_TYPE -> "text/plain")
        }
      case error: CloudImageErrorResponse =>
        Logger.debug("Sponsor - cloudinaryService - error: " + error.message)
        Ok(uploadError)
    }
  }

  def getLogoDetails(id: Long) = UserAwareAction { implicit request =>
    transaction {
      val r = model.Resource.lookup(id).get
      Ok(JsArray(Seq(JsObject(List(
        "url" -> JsString(r.url),
        "resourceId" -> JsNumber(r.id))))))
    }
  }
}