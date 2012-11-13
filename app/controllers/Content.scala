package controllers

import play.mvc.Controller
import cms.ContentManager
import cms.dto.Entry
import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.Constraints._
import org.squeryl.PrimitiveTypeMode.transaction
import play.api.mvc.Action
import helpers.Security

object Content extends core.LangAwareController with securesocial.core.SecureSocial {

  val entryForm = Form(
    mapping(
      "key" -> nonEmptyText,
      "entryType" -> helpers.Forms.enum(cms.dto.EntryType),
      "content" -> list(mapping(
        "lang" -> text,
        "value" -> text)(cms.dto.Content.apply)(cms.dto.Content.unapply)))(Entry.apply)(Entry.unapply))

  def index = SecuredAction() { implicit request =>

    implicit val user = request.user
    Security.verifyIfAllowed

    val entityList = ContentManager.all.sortWith(_.key < _.key)

    Ok(views.html.contents.index(entityList, user))
  }

  def create = SecuredAction() { implicit request =>

    implicit val user = request.user
    Security.verifyIfAllowed

    val entry = Entry("", cms.dto.EntryType.HTML, List.empty)
    Ok(views.html.contents.create(entryForm.fill(entry), user))
  }

  def save = SecuredAction() { implicit request =>

    implicit val user = request.user
    Security.verifyIfAllowed

    entryForm.bindFromRequest.fold(
      errors => BadRequest(views.html.contents.create(errors, user)),
      entry => {
        ContentManager.create(entry)
        Redirect(routes.Content.index)
      })

  }

  def edit(key: String) = SecuredAction() { implicit request =>

    implicit val user = request.user
    Security.verifyIfAllowed

    val entry = ContentManager.find(key)
    entry.map { entry =>
      Ok(views.html.contents.edit(key, entryForm.fill(entry), user))
    }.getOrElse {
      Redirect(routes.Content.index)
    }

  }

  def update(key: String) = SecuredAction() { implicit request =>

    implicit val user = request.user
    Security.verifyIfAllowed

    val entry = ContentManager.find(key)
    entry.map { entry =>
      entryForm.bindFromRequest.fold(
        errors => BadRequest(views.html.contents.edit(key, errors, user)),
        entry => {
          ContentManager.update(entry)
          Redirect(routes.Content.index)
        })
    }.getOrElse {
      Redirect(routes.Content.index)
    }

  }

  def delete(key: String) = SecuredAction() { implicit request =>

    implicit val user = request.user
    Security.verifyIfAllowed

    val entry = ContentManager.find(key)
    entry.map { entry =>
      ContentManager.remove(entry)
      Redirect(routes.Content.index)
    }.getOrElse {
      Redirect(routes.Content.index)
    }
  }
}
