package controllers

import org.squeryl.PrimitiveTypeMode.inTransaction
import play.api.data.Forms.date
import play.api.data.Forms.longNumber
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.optional
import play.api.data.Form
import play.api.mvc.Controller
import play.api.i18n.Messages

object News extends LangAwareController with securesocial.core.SecureSocial {

  val newsForm = Form(
    mapping(
      "title" -> nonEmptyText,
      "text" -> nonEmptyText,
      "labelsAsString" -> nonEmptyText,
      "authorId" -> longNumber,
      "publishedDate" -> date("dd-MM-yyyy"),
      "hackathonId" -> optional(longNumber))(model.News.apply)(model.News.unapply))

  def index = UserAwareAction { implicit request =>
    inTransaction {
      Ok(views.html.news.index(model.News.all, userFromRequest))
    }
  }

  def search(label: String) = UserAwareAction { implicit request =>
    inTransaction {
      Ok(views.html.news.index(model.News.findByLabel(label), userFromRequest, label))
    }
  }

  def indexH(hid: Long) = UserAwareAction { implicit request =>
    inTransaction {
      Ok(views.html.news.indexH(model.Hackathon.lookup(hid), userFromRequest))
    }
  }

  def view(id: Long) = UserAwareAction { implicit request =>
    inTransaction {
      Ok(views.html.news.view(model.News.lookup(id), userFromRequest))
    }
  }

  def viewH(hid: Long, id: Long) = UserAwareAction { implicit request =>
    inTransaction {
      val news = model.News.lookup(id)
      val hackathon = news.map { news => news.hackathon }.getOrElse { model.Hackathon.lookup(hid) }
      Ok(views.html.news.viewH(hackathon, news, userFromRequest))
    }
  }

  def create = SecuredAction() { implicit request =>
    inTransaction {
      ensureAdmin {
        val user = userFromRequest(request)
        val news = new model.News(user.id)
        Ok(views.html.news.create(newsForm.fill(news), user))
      }
    }
  }

  def createH(hid: Long) = SecuredAction() { implicit request =>
    inTransaction {
      val hackathon = model.Hackathon.lookup(hid)
      hackathon.map { hackathon =>
        ensureHackathonOrganiserOrAdmin(hackathon) {
          val user = userFromRequest(request)
          val news = new model.News(user.id, Some(hid))
          Ok(views.html.news.createH(Some(hackathon), newsForm.fill(news), user))
        }
      }.getOrElse(Redirect(routes.Hackathon.view(hid)))
    }
  }

  def save = SecuredAction() { implicit request =>
    ensureAdmin {
      val user = userFromRequest(request)
      newsForm.bindFromRequest.fold(
        errors => BadRequest(views.html.news.create(errors, user)),
        news => inTransaction {
          model.News.insert(news.copy(authorId = user.id))
          Redirect(routes.News.index).flashing("status" -> "added", "title" -> news.title)
        })
    }
  }

  def saveH(hid: Long) = SecuredAction() { implicit request =>
    val user = userFromRequest(request)
    inTransaction {
      model.Hackathon.lookup(hid).map { hackathon =>
        ensureHackathonOrganiserOrAdmin(hackathon) {
          newsForm.bindFromRequest.fold(
            errors => BadRequest(views.html.news.createH(model.Hackathon.lookup(hid), errors, user)),
            news => {
              model.News.insert(news.copy(authorId = user.id))
              Redirect(routes.News.indexH(hid)).flashing("status" -> "added", "title" -> news.title)
            })
        }
      }.getOrElse(Redirect(routes.Hackathon.view(hid)))
    }
  }

  def edit(id: Long) = SecuredAction() { implicit request =>
    val user = userFromRequest(request)
    inTransaction {
      model.News.lookup(id).map { news =>
        ensureNewsAuthorOrAdmin(news) {
          Ok(views.html.news.edit(id, newsForm.fill(news.copy(labelsAsString = news.labels.map(_.value).mkString(","))), user))
        }
      }.getOrElse {
        // news view will handle not found cases 
        Redirect(routes.News.view(id)).flashing()
      }
    }
  }

  def editH(hid: Long, id: Long) = SecuredAction() { implicit request =>
    inTransaction {
      (for (
        news <- model.News.lookup(id);
        hackathon <- model.Hackathon.lookup(hid)
      ) yield {
        ensureHackathonOrganiserOrNewsAuthorOrAdmin(hackathon, news) {
          val user = userFromRequest(request)
          Ok(views.html.news.editH(news.hackathon, id, newsForm.fill(news.copy(labelsAsString = news.labels.map(_.value).mkString(","))), user))
        }
      }).getOrElse {
        // no news found
        Redirect(routes.News.viewH(hid, id)).flashing()
      }
    }
  }

  def update(id: Long) = SecuredAction() { implicit request =>
    val user = userFromRequest(request)
    newsForm.bindFromRequest.fold(
      errors => BadRequest(views.html.news.edit(id, errors, user)),
      news => inTransaction {
        model.News.lookup(id).map { news =>
          ensureNewsAuthorOrAdmin(news) {
            model.News.update(id, news)
            Redirect(routes.News.index).flashing("status" -> "updated", "title" -> news.title)
          }
        }.getOrElse(Redirect(routes.News.view(id)))
      })
  }

  def updateH(hid: Long, id: Long) = SecuredAction() { implicit request =>
    val user = userFromRequest(request)
    newsForm.bindFromRequest.fold(
      errors => BadRequest(views.html.news.editH(model.Hackathon.lookup(hid), id, errors, user)),
      news => inTransaction {
        (for (
          dbNews <- model.News.lookup(id);
          hackathon <- model.Hackathon.lookup(hid)
        ) yield {
          ensureHackathonOrganiserOrNewsAuthorOrAdmin(hackathon, dbNews) {
            model.News.update(id, news.copy(hackathonId = Some(hid), authorId = dbNews.authorId))
            Redirect(routes.News.indexH(hid)).flashing("status" -> "updated", "title" -> news.title)
          }
        }).getOrElse(Redirect(routes.News.viewH(hid, id)))
      })
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    inTransaction {
      model.News.lookup(id).map { news =>
        ensureNewsAuthorOrAdmin(news) {
          model.News.delete(id)
          Redirect(routes.News.index).flashing("status" -> "deleted")
        }
      }.getOrElse(Redirect(routes.News.view(id)))
    }
  }

  def deleteH(hid: Long, id: Long) = SecuredAction() { implicit request =>
    inTransaction {
      (for (
        news <- model.News.lookup(id);
        hackathon <- model.Hackathon.lookup(hid)
      ) yield {
        ensureHackathonOrganiserOrNewsAuthorOrAdmin(hackathon, news, news.hackathonId == Some(hid) ) {
          model.News.delete(id)
          Redirect(routes.News.indexH(hid)).flashing("status" -> "updated", "title" -> news.title)
        }
      }).getOrElse(Redirect(routes.News.viewH(hid, id)))
    }
  }
}
