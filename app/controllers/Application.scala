package controllers

import play.api._
import play.api.mvc._
import play.api.i18n._
import model.Model
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column

object Application extends Controller with securesocial.core.SecureSocial {
  
  def index = UserAwareAction { implicit request =>
	Ok(views.html.index(request.user))
  }
  
  def about = UserAwareAction { implicit request =>
    Ok(views.html.about(request.user))
  }
  
  def contact = UserAwareAction { implicit request =>
    Ok(views.html.contact(request.user))
  }
  
  def sponsors = UserAwareAction { implicit request =>
    Ok(views.html.sponsors(request.user))
  }
  
  def profile = SecuredAction() { implicit request =>
	Ok(views.html.profile(request.user))
  }

}