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
      "github_username" -> nonEmptyText,
      "twitter_account" -> nonEmptyText,
      "avatar_url" -> text)((name, email, github_username, twitter_account, avatar_url) => model.User(name, email, github_username, twitter_account, avatar_url, "", false, false))((user: model.User) => Some(user.name, user.email, user.githubUsername, user.twitterAccount, user.avatarUrl)))

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
        val id = request.user.hackathonUserId;
        val user = model.User.lookup(id)
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
        model.User.update(request.user.hackathonUserId, user)
        Redirect(routes.Application.profile).flashing("status" -> "updated", "title" -> user.name)
      })
  }

  def changeLanguage(lang: String) = UserAwareAction { implicit request =>
    Redirect(request.headers.get(REFERER).getOrElse("/")).withSession(request.session + (SESSION_LANG_KEY -> lang))
  }

}