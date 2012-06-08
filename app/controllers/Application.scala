package controllers

import play.api._
import play.api.mvc._
import play.i18n._

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index(Messages.get(Lang.forCode("pl"), "home.welcome")))
  }
  
}