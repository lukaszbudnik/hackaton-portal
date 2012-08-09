package controllers

import java.text.SimpleDateFormat
import org.squeryl.PrimitiveTypeMode.transaction
import core.LangAwareController
import helpers.Forms.enum
import model.dto.HackathonWithLocations
import play.api.data.Forms.date
import play.api.data.Forms.list
import play.api.data.Forms.boolean
import play.api.data.Forms.of
import play.api.data.Forms.longNumber
import play.api.data.Forms.mapping
import play.api.data.Forms.text
import play.api.data.Forms.nonEmptyText
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.Json.toJson
import play.api.libs.json.JsValue
import play.api.mvc.Action
import play.api.mvc.Controller
import scala.collection.mutable.ListBuffer
import play.api.Logger


object Hackathon extends LangAwareController with securesocial.core.SecureSocial {

  private def hackathonWithLocations2Json(h : model.dto.HackathonWithLocations) : JsValue  = {
    
    toJson(Map(
      "id" -> toJson(h.hackathon.id),
      "subject" -> toJson(h.hackathon.subject),
      "status" -> toJson(h.hackathon.status.id.toString),
      "date" -> toJson(new SimpleDateFormat(Messages("global.dateformat")).format(h.hackathon.date)),
      "locations" -> toJson(h.locations.map {
    	  l => toJson(Map(
    	  	"id" -> toJson(l.id),
    	  	"city" -> toJson(l.city),
    	  	"country" -> toJson(l.country),
    	  	"fullAddress" ->toJson(l.fullAddress),
    	  	"name" -> toJson(l.name),
    	  	"postalCode" -> toJson(l.postalCode),
    	  	"latitude" -> toJson(l.latitude),
    	  	"longitude" -> toJson(l.longitude)
        ))}.toSeq )))
	 
  }

  def hackathonsJson = Action {
    transaction {
      val hackathons = model.dto.HackathonWithLocations.all
 
      Ok(toJson(hackathons.map {
        hWl => hackathonWithLocations2Json(hWl)
      }.toSeq))
    }
  }
  
  def hackathonJson(id: Long) = Action {
    transaction {
      Ok(model.dto.HackathonWithLocations.lookup(id) match {
        case Some(h)  =>
          	  hackathonWithLocations2Json(h)
        case _ =>
        	toJson("")
      }) 
    }
  }

  
 
  val locationsSubForm = Form(
	  "locations" -> list(mapping(
	          "id" -> longNumber,
	          "name" -> text,
	          "city" -> text,
	          "country" -> text,
	          "fullAddress" -> text)
	          // apply location
		          ((id, name, city, country, fullAddress) => 
		                new model.Location(id, country, city,  "", fullAddress, name, 0, 0))
		          // unapply location
		          ((l : model.Location) => 
		            Some(l.id , l.name, l.city, l.country, l.fullAddress)))
	          )    
  
  
  val hackathonForm = Form(
    mapping(
      "subject" -> nonEmptyText,
      "status" -> enum(model.HackathonStatus),
      "date" -> date(Messages("global.dateformat")),
      "description" -> nonEmptyText,
      "organizerId" -> longNumber,
      "new_problems_disabled" -> boolean,
      "new_teams_disabled" -> boolean,
      "locations" -> list(mapping(
          "id" -> longNumber,
          "name" -> nonEmptyText,
          "city" -> nonEmptyText,
          "country" -> nonEmptyText,
          "fullAddress" -> nonEmptyText)
          // apply location
	          ((id, name, city, country, fullAddress) => 
	                new model.Location(id, country, city, "" , fullAddress, name, 0, 0))
	          // unapply location
	          ((l : model.Location) => 
	            Some(l.id, l.name , l.city, l.country, l.fullAddress)))
          )
       // apply HackathonWithLocations
      ((subject, status, date, description, organizerId, newProblemsDisabled, newTeamsDisabled, locations) => 
        new HackathonWithLocations(
            new model.Hackathon(subject, status, date, description, organizerId, newProblemsDisabled, newTeamsDisabled), locations)
        ) // unapply HackathonWithLocations
      ((hWl : HackathonWithLocations) => 
        Some(hWl.hackathon.subject
            , hWl.hackathon.status
            , hWl.hackathon.date
            , hWl.hackathon.description
            , hWl.hackathon.organiserId
            , hWl.hackathon.newProblemsDisabled
            , hWl.hackathon.newTeamsDisabled
            , hWl.locations.toList)
            )
      )

  
      
  def addLocation = UserAwareAction { implicit request =>
    val lb = ListBuffer[model.Location]()
	locationsSubForm.bindFromRequest.fold(
	  				errors => {
	  				 	Logger.error("deleteLocation - should never happen!")
	  				}
	  				,locations  => transaction {
					   lb ++= locations
					   lb += new model.Location()		
			   }
	  		)  	
    Ok(views.html.hackathons.locationsContainer(
       hackathonForm.fill(HackathonWithLocations(new model.Hackathon(), lb.toList))))	  		
  }
      
  def deleteLocation(idx : Int) = UserAwareAction { implicit request =>
  	val lb = ListBuffer[model.Location]()
	locationsSubForm.bindFromRequest.fold(
		errors => {
		 	Logger.error("deleteLocation - should never happen!")
		}
		,locations  => transaction {
		   lb ++= locations
		   lb.remove(idx)
	   }
	)
	   Ok(views.html.hackathons.locationsContainer(
	       hackathonForm.fill(HackathonWithLocations(new model.Hackathon(), lb.toList))))
  }
      
  def index = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.hackathons.index(model.Hackathon.all, request.user))
    }
  }

  def view(id: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.hackathons.view(model.Hackathon.lookup(id), request.user))
    }
  }
  
  def chat(id: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.hackathons.chat(model.Hackathon.lookup(id), request.user))
    }
  }

  def create = SecuredAction() { implicit request =>
    transaction {
    
      val hackathon = new model.dto.HackathonWithLocations(new model.Hackathon(request.user.hackathonUserId)
      , List[model.Location](new model.Location)) 
      Ok(views.html.hackathons.create(hackathonForm.fill(hackathon), request.user))
    }
  }

  def save = SecuredAction() { implicit request =>
    hackathonForm.bindFromRequest.fold(
      errors => transaction { 
        BadRequest(views.html.hackathons.create(errors, request.user))
      },
      hackathonWithLocations => transaction {
        val newH = model.Hackathon.insert(hackathonWithLocations.hackathon)
        hackathonWithLocations.locations.map {
        	location =>
        	  newH.addLocation(location)
        }
        Redirect(routes.Hackathon.index).flashing("status" -> "added", "title" -> newH.subject)
      })
  }

  def edit(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.dto.HackathonWithLocations.lookup(id).map { hackathonWithL =>
        helpers.Security.verifyIfAllowed(hackathonWithL.hackathon.organiserId)(request.user)
     
        Ok(views.html.hackathons.edit(id, hackathonForm.fill(hackathonWithL),  request.user))
      }.getOrElse {
        // no hackathon found
        Redirect(routes.Hackathon.view(id)).flashing()
      }
    }
  }

  def update(id: Long) = SecuredAction() { implicit request =>
    hackathonForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.hackathons.edit(id, errors, request.user))
      },
      hackathonWithL => transaction {
        model.Hackathon.lookup(id).map { hackathon =>
          helpers.Security.verifyIfAllowed(hackathon.organiserId)(request.user)
          model.Hackathon.update(id, hackathonWithL.hackathon)
          hackathon.deleteLocations()
          hackathonWithL.locations.map {
							            location => 
							              	hackathon.addLocation(location)
          }
      }
        
        Redirect(routes.Hackathon.index).flashing("status" -> "updated", "title" -> hackathonWithL.hackathon.subject)
      })
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Hackathon.lookup(id).map { hackathon =>
        helpers.Security.verifyIfAllowed(hackathon.organiserId)(request.user)
      }
      model.Hackathon.delete(id)
      Redirect(routes.Hackathon.index).flashing("status" -> "deleted")
    }
  }
  
  def join(id: Long) = SecuredAction() { implicit request =>
    transaction {
      var status = "error"
      model.User.lookup(request.user.hackathonUserId).map { user =>
        model.Hackathon.lookup(id).map { hackathon =>
          if (!hackathon.hasMember(user.id)) {
            hackathon.addMember(user)
            status = "joined"
          }
        }
      }
      Redirect(routes.Hackathon.view(id)).flashing("status" -> status)
    }
  }

  def disconnect(id: Long) = SecuredAction() { implicit request =>
    transaction {
      var status = "error"
      model.User.lookup(request.user.hackathonUserId).map { user =>
        model.Hackathon.lookup(id).map { hackathon =>
          hackathon.deleteMember(user)
          status = "disconnected"
        }
      }
      Redirect(routes.Hackathon.view(id)).flashing("status" -> status)
    }
  }

  def disconnectUser(id: Long, userId: Long) = SecuredAction() { implicit request =>
    transaction {
      var status = "error"
      model.User.lookup(userId).map { user =>
        model.Hackathon.lookup(id).map { hackathon =>
          helpers.Security.verifyIfAllowed(hackathon.organiserId)(request.user)
          hackathon.deleteMember(user)
          status = "disconnectedUser"
        }
      }
      Redirect(routes.Hackathon.view(id)).flashing("status" -> status)
    }
  }
}