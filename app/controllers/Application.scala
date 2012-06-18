package controllers

import play.api._
import play.api.mvc._
import play.api.i18n._

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index(Messages("home.welcome")))
  }
  
  def about = Action {
    Ok(views.html.about(Messages("about.title")))
  }
  
  def contact = Action {
    Ok(views.html.contact(Messages("contact.title")))
  }
  
}