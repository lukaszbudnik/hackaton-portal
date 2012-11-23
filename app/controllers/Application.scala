package controllers

import play.api.mvc.Controller
import play.api.cache.Cached
import play.api.Play.current
import play.api.mvc.Action
import play.api.Logger
import play.api.mvc.RequestHeader
import play.api.i18n.Lang
import play.api.cache.Cache
import org.squeryl.PrimitiveTypeMode.transaction
import play.api.data.Forms._
import play.api.data.Form

object Application extends LangAwareController {

  val userForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "email" -> nonEmptyText,
      "language" -> nonEmptyText,
      "github_username" -> text,
      "twitter_account" -> text,
      "avatar_url" -> text)((name, email, language, github_username, twitter_account, avatar_url) => model.User(name, email, language, github_username, twitter_account, avatar_url, "", false, false))((user: model.User) => Some(user.name, user.email, user.language, user.githubUsername, user.twitterAccount, user.avatarUrl)))

  def index = UserAwareAction { implicit request =>
    Ok(views.html.index(userFromRequest))
  }

  def about = UserAwareAction { implicit request =>
    Ok(views.html.about(userFromRequest))
  }

  def contact = UserAwareAction { implicit request =>
    Ok(views.html.contact(userFromRequest))
  }

  def profile = SecuredAction() {
    implicit request =>
      transaction {
        val user = userFromRequest(request)
        Ok(views.html.profile(userForm.fill(user), user))
      }
  }

  def updateProfile = SecuredAction() { implicit request =>
    val requestUser = userFromRequest(request)
    userForm.bindFromRequest.fold(
      errors => BadRequest(views.html.profile(errors, requestUser)),
      user => transaction {
        model.User.update(requestUser.id, user)
        Redirect(routes.Application.profile).flashing("status" -> "updated", "title" -> user.name).withSession(request.session + (LangAwareController.SESSION_LANG_KEY -> user.language))
      })
  }

  def changeLanguage(lang: String) = UserAwareAction { implicit request =>
    val result = Redirect(request.headers.get(REFERER).getOrElse("/")).withSession(request.session + (LangAwareController.SESSION_LANG_KEY -> lang))

    val user = userFromRequest(request)

    user match {
      case Some(user: model.User) => {
        transaction {
          val newUser = user.copy(language = lang)
          model.User.update(user.id, newUser)
        }
        result.flashing("language.status" -> "language.updated")
      }
      case _ => result
    }

  }

}
