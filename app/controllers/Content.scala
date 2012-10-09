package controllers

import play.mvc.Controller
import play.api.mvc.Action
import cms.ContentManager

object Content extends core.LangAwareController {
	def index = Action{
	  var entityList = ContentManager.all	  
	  Ok(views.html.content.index(entityList))
	}
	
	def view(key: String) = Action{
	  Ok("in progress, in future will fetch details for entry "+key)
	}
}