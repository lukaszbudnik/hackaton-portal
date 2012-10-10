package controllers

import play.mvc.Controller
import play.api.mvc.Action
import cms.ContentManager
import cms.dto.Entry
import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.Constraints._
import org.squeryl.PrimitiveTypeMode.transaction
object Content extends core.LangAwareController {

	//Entry form
	val entryForm = Form(
	  mapping(
	    "key" -> nonEmptyText,
	    "entryType" -> helpers.Forms.enum(cms.dto.EntryType),
	    "content" -> list(mapping(
	        "lang" -> text,
	        "value" -> text
	        )(cms.dto.Content.apply)(cms.dto.Content.unapply)
	        )
	  )(Entry.apply)//((key, entryType) => Entry(key,entryType, List.empty))
	  (Entry.unapply)//((entry: Entry) => Some(entry.key, entry.entryType))
	)
	
	//lists all entries
	def index = Action{
	  var entityList = ContentManager.all	  
	  Ok(views.html.content.index(entityList))
	}
	
	//displays details of cms.dto.Entry in edit mode
	def view(key: String) = Action{
	  var entry = ContentManager.find(key)
	  Ok(views.html.content.view(entry,true))
	}
	
	//displays page for adding new Entry 
	def create = Action {
		var option : Option[Entry] = Some(Entry("", cms.dto.EntryType.HTML, List.empty))		
		Ok(views.html.content.view(option,false))
	}
	
	//updates Entry with values from form
	def update(key: String) = Action{ implicit request => 
	  entryForm.bindFromRequest.fold(
	      errors => transaction {BadRequest(views.html.content.index(Nil))}, //TODO: handle bad request
	      entry => transaction {
	        ContentManager.update(entry)
	      }
	  )
	  Redirect(routes.Content.index)
	}
	
	def save = Action {
	  TODO
	}
}