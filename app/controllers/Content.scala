package controllers

import play.mvc.Controller

import cms.ContentManager
import cms.dto.Entry
import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.Constraints._
import org.squeryl.PrimitiveTypeMode.transaction

object Content extends core.LangAwareController with securesocial.core.SecureSocial {

  val entryForm = Form(
    mapping(
      "key" -> nonEmptyText,
      "entryType" -> helpers.Forms.enum(cms.dto.EntryType),
      "content" -> list(mapping(
        "lang" -> text,
        "value" -> text)(cms.dto.Content.apply)(cms.dto.Content.unapply)))(Entry.apply)(Entry.unapply))

  def index = SecuredAction() { implicit request =>
    val entityList = ContentManager.all
    Ok(views.html.content.index(entityList, Some(request.user)))

  }

  def create = SecuredAction() { implicit request =>
    val entry = Entry("", cms.dto.EntryType.HTML, List.empty)
    Ok(views.html.content.create(entryForm.fill(entry), Some(request.user)))

  }

  def save = SecuredAction() { implicit request =>
    entryForm.bindFromRequest.fold(
      errors => BadRequest(views.html.content.create(errors, Some(request.user))),
      entry => {
        ContentManager.create(entry)
        Redirect(routes.Content.index)
      })

  }

  def edit(key: String) = SecuredAction() { implicit request =>
    val entry = ContentManager.find(key)
    entry.map { entry =>
      Ok(views.html.content.edit(key, entryForm.fill(entry), Some(request.user)))
    }.getOrElse {
      Redirect(routes.Content.index)
    }

  }

  def update(key: String) = SecuredAction() { implicit request =>
    val entry = ContentManager.find(key)
    entry.map { entry =>
      entryForm.bindFromRequest.fold(
        errors => BadRequest(views.html.content.edit(key, errors, Some(request.user))),
        entry => {
          ContentManager.update(entry)
          Redirect(routes.Content.index)
        })
    }.getOrElse {
      Redirect(routes.Content.index)
    }

  }

  def delete(key: String) = TODO

}