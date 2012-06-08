package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index("Wiamy w portalu hackaton.pl"))
  }
  
}