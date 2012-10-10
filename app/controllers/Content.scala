package controllers

import play.mvc.Controller
import play.api.mvc.Action
import cms.ContentManager
import cms.dto.Entry

object Content extends core.LangAwareController {

	def index = Action{
	  var entityList = ContentManager.all	  
	  Ok(views.html.content.index(entityList))
	}
	
	//displays details of cms.dto.Entry in view only mode
	def view(key: String, mode: String) = Action{
	  var entry = ContentManager.find(key)
	  Ok(views.html.content.view(entry,mode))
	}
}