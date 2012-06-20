package controllers

import play.api._
import play.api.mvc._
import play.api.i18n._

object Application extends Controller with securesocial.core.SecureSocial {
  
  def index = UserAwareAction { implicit request =>
	Ok(views.html.index())
  }
  
  def about = Action {
    Ok(views.html.about())
  }
  
  def contact = Action {
    Ok(views.html.contact())
  }
  
  def sponsors = Action {
    Ok(views.html.sponsors())
  }
}