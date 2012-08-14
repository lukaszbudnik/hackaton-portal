package controllers

import org.squeryl.PrimitiveTypeMode.transaction
import play.api.data.Forms._
import play.api.data.Form
import play.api.mvc.Controller
import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import play.api.libs.json.Format
import play.api.libs.json.JsNumber
import play.api.libs.json.JsString
import play.api.libs.json.Json._
import play.api.libs.json.Json._
import play.api.mvc.Action
import play.api.Logger
import core.LangAwareController
import helpers.Forms.enum
import model.LocationStatus
import org.squeryl.PrimitiveTypeMode._


object Location extends LangAwareController with securesocial.core.SecureSocial {

  val locationForm = Form(
    mapping(
      "id" -> ignored(0L),
      "country" -> nonEmptyText,
      "city" -> nonEmptyText,
      "postalCode" -> nonEmptyText,
      "fullAddress" -> nonEmptyText,
      "name" -> nonEmptyText,
      "latitude" -> helpers.Forms.real,
      "longitude" -> helpers.Forms.real,
      "submitterId" -> ignored(0L),
      "status" -> enum(model.LocationStatus))(model.Location.apply)(model.Location.unapply))


      
  def index = SecuredAction() { implicit request =>
    helpers.Security.verifyIfAllowed()(request.user)
    transaction {
      helpers.Security.verifyIfAllowed(request.user)
      Ok(views.html.locations.index(model.Location.all, Some(request.user)))
    }
    
  }

  def view(id: Long) = SecuredAction() { implicit request =>
    helpers.Security.verifyIfAllowed()(request.user)
    transaction {
      helpers.Security.verifyIfAllowed(request.user)
      Ok(views.html.locations.view(model.Location.lookup(id), Some(request.user)))
    }
  }

  def create = SecuredAction() { implicit request =>
    transaction {
      Ok(views.html.locations.locationForm(routes.Location.save, locationForm, false))
    }
  }
  
  def createA = SecuredAction() { implicit request =>
    transaction {
      helpers.Security.verifyIfAllowed()(request.user)
      Ok(views.html.locations.create(routes.Location.saveA, locationForm, request.user))
    }
  }

  
  def save = SecuredAction() { implicit request =>
    locationForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.locations.locationForm(routes.Location.save, errors, false))
      },
      location => transaction {
        model.Location.insert(location.copy(submitterId = request.user.hackathonUserId, status = LocationStatus.Unverified))
        Ok("")
      })
  }
  
  def saveA = SecuredAction() { implicit request =>
    
    helpers.Security.verifyIfAllowed()(request.user)
    
    locationForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.locations.create(routes.Location.saveA, errors, request.user))
      },
      location => transaction {
        model.Location.insert(location.copy(submitterId = request.user.hackathonUserId))
         Redirect(routes.Location.index).flashing("status" -> "added", "title" -> location.name)
      })
  }
  

  def editA(id: Long) = SecuredAction() { implicit request =>
    
    helpers.Security.verifyIfAllowed()(request.user)
    
    transaction {
      model.Location.lookup(id).map { location =>
        Ok(views.html.locations.edit(id, routes.Location.updateA(id), locationForm.fill(location), request.user))
      }.getOrElse {
        // no location found
        Redirect(routes.Location.view(id)).flashing()
      }
    }
  }
  
  
  def edit(id: Long) = SecuredAction() { implicit request =>
        
    transaction {
      model.Location.lookup(id).map { dbLocation =>       
        helpers.Security.verifyIfAllowed(dbLocation.submitterId)(request.user)   
        Ok(views.html.locations.locationForm(routes.Location.edit(id), locationForm.fill(dbLocation), false))       
      }.getOrElse {
    	Ok(views.html.locations.locationForm(routes.Location.edit(id), locationForm, false))      
      }
    }
  }

  def update(id: Long) = SecuredAction() { implicit request =>
    locationForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.locations.locationForm(routes.Location.edit(id), errors, false))
      },
      location => transaction {
        model.Location.lookup(id).map { dbLocation =>
        	helpers.Security.verifyIfAllowed(dbLocation.submitterId)(request.user) 
        	model.Location.update(id, location)
        }
        Ok("")
      })
  }
  
 def updateA(id: Long) = SecuredAction() { implicit request =>
   helpers.Security.verifyIfAllowed()(request.user)
    locationForm.bindFromRequest.fold(
      errors => transaction {
        BadRequest(views.html.locations.edit(id, routes.Location.editA(id), errors, request.user))
      },
      location => transaction {
        model.Location.update(id, location.copy(submitterId = request.user.hackathonUserId))
        Redirect(routes.Location.index).flashing("status" -> "updated", "title" -> location.name)
      })
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    transaction {
      helpers.Security.verifyIfAllowed()(request.user)
      model.Location.delete(id)
    }
    Redirect(routes.Location.index).flashing("status" -> "deleted")
  }

  def findByPattern(term: String) = SecuredAction() { implicit request =>
    transaction {
      implicit object LocationFormat extends Format[model.Location] {
        def reads(json: JsValue): model.Location = new model.Location()

        def writes(l: model.Location): JsValue = JsObject(List(
          "id" -> JsNumber(l.id),
          "value" -> JsString(l.name),
          "fullAddress" -> JsString(l.fullAddress),
          "country" -> JsString(l.country),
          "submitterId" -> JsNumber(l.submitterId),
          "city" -> JsString(l.city)))
      }

      val locations: List[model.Location] = model.Location.findByPattern("%" + term + "%",
          (l) => l.status === LocationStatus.Approved or request.user.hackathonUserId === l.submitterId
          or request.user.isAdmin === true).toList
      Ok(toJson(locations))
    }
  }
}