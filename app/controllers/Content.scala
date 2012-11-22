package controllers

import cms.dto.Entry
import cms.ContentManager
import play.api.data.Forms.list
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.text
import play.api.data.Form

object Content extends LangAwareController {

  val entryForm = Form(
    mapping(
      "key" -> nonEmptyText,
      "entryType" -> helpers.Forms.enum(cms.dto.EntryType),
      "content" -> list(mapping(
        "lang" -> text,
        "value" -> text)(cms.dto.Content.apply)(cms.dto.Content.unapply)))(Entry.apply)(Entry.unapply))

  def index = SecuredAction() { implicit request =>

    ensureAdmin {
      val user = userFromRequest(request)

      val entityList = ContentManager.all.sortWith(_.key < _.key)

      Ok(views.html.contents.index(entityList, user))
    }

  }

  def create = SecuredAction() { implicit request =>
    ensureAdmin {
      val user = userFromRequest(request)

      val entry = Entry("", cms.dto.EntryType.HTML, List.empty)
      Ok(views.html.contents.create(entryForm.fill(entry), user))
    }
  }

  def save = SecuredAction() { implicit request =>
    ensureAdmin {
      val user = userFromRequest(request)

      entryForm.bindFromRequest.fold(
        errors => BadRequest(views.html.contents.create(errors, user)),
        entry => {
          ContentManager.create(entry)
          Redirect(routes.Content.index)
        })
    }
  }

  def edit(key: String) = SecuredAction() { implicit request =>
    ensureAdmin {
      val user = userFromRequest(request)

      val entry = ContentManager.find(key)
      entry.map { entry =>
        Ok(views.html.contents.edit(key, entryForm.fill(entry), user))
      }.getOrElse {
        Redirect(routes.Content.index)
      }
    }
  }

  def update(key: String) = SecuredAction() { implicit request =>
    ensureAdmin {
      val user = userFromRequest(request)

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
  }

  def delete(key: String) = SecuredAction() { implicit request =>
    ensureAdmin {
      val user = userFromRequest(request)

      val entry = ContentManager.find(key)
      entry.map { entry =>
        ContentManager.remove(entry)
        Redirect(routes.Content.index)
      }.getOrElse {
        Redirect(routes.Content.index)
      }
    }
  }
}
