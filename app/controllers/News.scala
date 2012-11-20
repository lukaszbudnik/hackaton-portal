package controllers

import org.squeryl.PrimitiveTypeMode.transaction
import play.api.data.Forms.date
import play.api.data.Forms.longNumber
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.optional
import play.api.data.Form
import play.api.mvc.Controller
import core.LangAwareController
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
    transaction {
      Ok(views.html.news.index(model.News.all, request.user))
    }
  }

  def search(label: String) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.news.index(model.News.findByLabel(label), request.user, label))
    }
  }

  def indexH(hid: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.news.indexH(model.Hackathon.lookup(hid), request.user))
    }
  }

  def view(id: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.news.view(model.News.lookup(id), request.user))
    }
  }

  def viewH(hid: Long, id: Long) = UserAwareAction { implicit request =>
    transaction {
      val news = model.News.lookup(id)
      val hackathon = news.map { news => news.hackathon }.getOrElse { model.Hackathon.lookup(hid) }
      Ok(views.html.news.viewH(hackathon, news, request.user))
    }
  }

  def create = SecuredAction() { implicit request =>
    helpers.Security.verifyIfAllowed(request.user)
    transaction {
      model.User.lookupByOpenId(request.user.id.id + request.user.id.providerId).map { user =>
        val news = new model.News(user.id)
        Ok(views.html.news.create(newsForm.fill(news), request.user))
      }.getOrElse(Redirect(routes.News.index))
    }
  }

  def createH(hid: Long) = SecuredAction() { implicit request =>
    transaction {
      val hackathon = model.Hackathon.lookup(hid)
      hackathon.map { hackathon =>
        helpers.Security.verifyIfAllowed(hackathon.organiserId)(request.user)
      }
      model.User.lookupByOpenId(request.user.id.id + request.user.id.providerId).map { user =>
      val news = new model.News(user.id, Some(hid))
      Ok(views.html.news.createH(hackathon, newsForm.fill(news), request.user))
      }.getOrElse(Redirect(routes.News.indexH(hid)))
    }
  }

  def save = SecuredAction() { implicit request =>
    helpers.Security.verifyIfAllowed(request.user)
    newsForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.news.create(errors, request.user))
      },
      news => transaction {
        helpers.Security.verifyIfAllowed(request.user)
        model.News.insert(news)
        Redirect(routes.News.index).flashing("status" -> "added", "title" -> news.title)
      })
  }

  def saveH(hid: Long) = SecuredAction() { implicit request =>
    newsForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.news.createH(model.Hackathon.lookup(hid), errors, request.user))
      },
      news => transaction {
        model.Hackathon.lookup(hid).map { hackathon =>
          helpers.Security.verifyIfAllowed(hackathon.organiserId)(request.user)
        }
        model.News.insert(news)
        Redirect(routes.News.indexH(hid)).flashing("status" -> "added", "title" -> news.title)
      })
  }

  def edit(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.News.lookup(id).map { news =>
        helpers.Security.verifyIfAllowed(news.authorId)(request.user)
        Ok(views.html.news.edit(id, newsForm.fill(news.copy(labelsAsString = news.labels.map(_.value).mkString(","))), request.user))
      }.getOrElse {
        // no news found
        Redirect(routes.News.view(id)).flashing()
      }
    }
  }

  def editH(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.News.lookup(id).map { news =>
        helpers.Security.verifyIfAllowed(Some(hid) == news.hackathonId)(request.user)
        news.hackathon.map { hackathon =>
          helpers.Security.verifyIfAllowed(news.authorId, hackathon.organiserId)(request.user)
        }.getOrElse {
          helpers.Security.verifyIfAllowed(news.authorId)(request.user)
        }
        Ok(views.html.news.editH(news.hackathon, id, newsForm.fill(news.copy(labelsAsString = news.labels.map(_.value).mkString(","))), request.user))
      }.getOrElse {
        // no news found
        Redirect(routes.News.viewH(hid, id)).flashing()
      }
    }
  }

  def update(id: Long) = SecuredAction() { implicit request =>
    newsForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.news.edit(id, errors, request.user))
      },
      news => transaction {
        model.News.lookup(id).map { news =>
          helpers.Security.verifyIfAllowed(news.authorId)(request.user)
        }
        model.News.update(id, news)
        Redirect(routes.News.index).flashing("status" -> "updated", "title" -> news.title)
      })
  }

  def updateH(hid: Long, id: Long) = SecuredAction() { implicit request =>
    newsForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.news.editH(model.Hackathon.lookup(hid), id, errors, request.user))
      },
      news => transaction {
        model.News.lookup(id).map { news =>
          helpers.Security.verifyIfAllowed(Some(hid) == news.hackathonId)(request.user)
          news.hackathon.map { hackathon =>
            helpers.Security.verifyIfAllowed(news.authorId, hackathon.organiserId)(request.user)
          }.getOrElse {
            helpers.Security.verifyIfAllowed(news.authorId)(request.user)
          }
        }
        model.News.update(id, news)
        Redirect(routes.News.indexH(hid)).flashing("status" -> "updated", "title" -> news.title)
      })
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.News.lookup(id).map { news =>
        helpers.Security.verifyIfAllowed(news.authorId)(request.user)
      }
      model.News.delete(id)
      Redirect(routes.News.index).flashing("status" -> "deleted")
    }
  }

  def deleteH(hid: Long, id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.News.lookup(id).map { news =>
        helpers.Security.verifyIfAllowed(Some(hid) == news.hackathonId)(request.user)
        news.hackathon.map { hackathon =>
          helpers.Security.verifyIfAllowed(news.authorId, hackathon.organiserId)(request.user)
        }.getOrElse {
          helpers.Security.verifyIfAllowed(news.authorId)(request.user)
        }
      }
      model.News.delete(id)
      Redirect(routes.News.indexH(hid)).flashing("status" -> "deleted")
    }
  }
}
