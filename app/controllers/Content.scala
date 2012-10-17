package controllers

import play.mvc.Controller
import cms.ContentManager
import cms.dto.Entry
import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.Constraints._
import org.squeryl.PrimitiveTypeMode.transaction
import play.api.mvc.Action

object Content extends core.LangAwareController with securesocial.core.SecureSocial {

  val entryForm = Form(
    mapping(
      "key" -> nonEmptyText,
      "entryType" -> helpers.Forms.enum(cms.dto.EntryType),
      "content" -> list(mapping(
        "lang" -> text,
        "value" -> text)(cms.dto.Content.apply)(cms.dto.Content.unapply)))(Entry.apply)(Entry.unapply))

  def index = Action { implicit request =>
    val entityList = ContentManager.all
    
    Ok(views.html.content.index(entityList, None))
  }

  def create = Action { implicit request =>
    val entry = Entry("", cms.dto.EntryType.HTML, List.empty)
    Ok(views.html.content.create(entryForm.fill(entry), None))
  }

  def save = Action { implicit request =>
    entryForm.bindFromRequest.fold(
      errors => BadRequest(views.html.content.create(errors, None)),
      entry => {
        ContentManager.create(entry)
        Redirect(routes.Content.index)
      })

  }

  def edit(key: String) = Action { implicit request =>
    val entry = ContentManager.find(key)
    entry.map { entry =>
      Ok(views.html.content.edit(key, entryForm.fill(entry), None))
    }.getOrElse {
      Redirect(routes.Content.index)
    }

  }

  def update(key: String) = Action { implicit request =>
    val entry = ContentManager.find(key)
    entry.map { entry =>
      entryForm.bindFromRequest.fold(
        errors => BadRequest(views.html.content.edit(key, errors, None)),
        entry => {
          ContentManager.update(entry)
          Redirect(routes.Content.index)
        })
    }.getOrElse {
      Redirect(routes.Content.index)
    }

  }

  def delete(key: String) = Action { implicit request =>
    val entry = ContentManager.find(key)
    entry.map { entry =>
      ContentManager.remove(entry)
      Redirect(routes.Content.index)
    }.getOrElse {
      Redirect(routes.Content.index)
    }
  }
}
