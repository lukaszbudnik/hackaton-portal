package controllers

import play.api.mvc.Controller
import play.api.cache.Cached
import play.api.Play.current
import play.api.mvc.Action
import play.api.Logger
import play.api.mvc.RequestHeader
import play.api.i18n.Lang
import core.LangAwareController
import play.api.cache.Cache
import org.squeryl.PrimitiveTypeMode.transaction
import play.api.data.Forms._
import play.api.data.Form

object Application extends LangAwareController with securesocial.core.SecureSocial {

  val userForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "email" -> nonEmptyText,
      "language" -> nonEmptyText,
      "github_username" -> text,
      "twitter_account" -> text,
      "avatar_url" -> text)((name, email, language, github_username, twitter_account, avatar_url) => model.User(name, email, language, github_username, twitter_account, avatar_url, "", false, false))((user: model.User) => Some(user.name, user.email, user.language, user.githubUsername, user.twitterAccount, user.avatarUrl)))

  def index = UserAwareAction { implicit request =>
    Ok(views.html.index(request.user))
  }

  def about = UserAwareAction { implicit request =>
    Ok(views.html.about(request.user))
  }

  def contact = UserAwareAction { implicit request =>
    Ok(views.html.contact(request.user))
  }

  def profile = SecuredAction() {
    implicit request =>
      transaction {
        val user = model.User.lookupByOpenId(request.user.id.id + request.user.id.providerId)
        user.map { u =>
          Ok(views.html.profile(userForm.fill(u), request.user))
        }.getOrElse {
          Redirect(securesocial.controllers.routes.LoginPage.login).flashing()
        }
      }
  }

  def updateProfile = SecuredAction() { implicit request =>
    userForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.profile(errors, request.user))
      },
      user => transaction {
        model.User.update(request.user.id.id + request.user.id.providerId, user)
        Redirect(routes.Application.profile).flashing("status" -> "updated", "title" -> user.name).withSession(request.session + (LangAwareController.SESSION_LANG_KEY -> user.language))
      })
  }

  def changeLanguage(lang: String) = UserAwareAction { implicit request =>
    val result = Redirect(request.headers.get(REFERER).getOrElse("/")).withSession(request.session + (LangAwareController.SESSION_LANG_KEY -> lang))

    request.user match {
      case Some(user: securesocial.core.SocialUser) => {
        transaction {
          model.User.lookupByOpenId(user.id.id + user.id.providerId).map { hackatonUser =>
            val newUser = hackatonUser.copy(language = lang)
            model.User.update(hackatonUser.id, newUser)
          }
        }
        result.flashing("language.status" -> "language.updated")
      }
      case _ => result
    }

  }

}
