package controllers

import org.squeryl.PrimitiveTypeMode._
import play.api.mvc._
import model.Model
import play.api.data._
import play.api.data.Forms._
import play.api.Logger
import javax.persistence.OrderBy
import model.HackathonSponsorHelper

object Sponsor extends Controller with securesocial.core.SecureSocial {
  
  def index = UserAwareAction { implicit request =>
  	transaction {
  	  
  	  val sponsors: Map[Long, model.Sponsor] = model.Sponsors.all.map({ s => (s.id, s) }).toMap
  	  
  	  val generalSponsors: Iterable[Long] = model.Sponsors.allGeneralSponsorsOrdered.map {s => s.id}
  	  
  	  val hackathonSponsors: Map[Long, Iterable[Long]] = 
  	    Model.hackathons.toList.map( { h => (
  	        h.id, 
  	        h.sponsors.map { s => s.id} 
  	    )}).toMap
  	  
  	  Ok(views.html.sponsors.index(Model.hackathons.toIterable, sponsors, generalSponsors, hackathonSponsors, request.user)) 
  	  
  	}
  }
  
  def view(id: Long) = UserAwareAction { implicit request =>
    transaction {
      
      val hackathons = Model.allHackathonsForSponsor(id)
      val hs = model.Sponsors.hackathonsToSponsors.where(hs => hs.sponsorId === id).map( { hs => (hs.hackathonId, hs.order) } ).toMap
      Ok(views.html.sponsors.view(model.Sponsors.lookup(id), hackathons, hs, request.user))
      
    }
  }
  
  val sponsorForm = Form(
      mapping(
          "name" 				-> nonEmptyText,
          "description"			-> nonEmptyText,
          "website"				-> nonEmptyText,
          "order"  				-> number,
          "isGeneralSponsor" 	-> boolean,
          "hackathons"			-> list(mapping(
        		  "hackathonId"		-> longNumber,
        		  "order"			-> number
          )((model.HackathonSponsorHelper).apply)(model.HackathonSponsorHelper.unapply))
      )(model.Sponsor.apply)(model.Sponsor.unapply)
  )
  
  def create = SecuredAction() { implicit request =>
  	transaction {
  	  Ok(views.html.sponsors.create(sponsorForm, Model.hackathons.toList, request.user))
  	}
  }
  
  def save = SecuredAction() { implicit request =>
    sponsorForm.bindFromRequest.fold(
        errors =>  transaction {
          BadRequest(views.html.sponsors.create(errors, Model.hackathons.toList, request.user))
        },sponsor => transaction {
          
          model.Sponsors.sponsors.insert(sponsor)
          
          sponsor.hackathonsList.map { hs =>
            
            if (hs.order != -1) {
              
              val hackathon = Model.hackathons.lookup(hs.hackathonId).get
              //TODO: handle case with no hackathon returned from db
              val hsTable: model.HackathonSponsor = model.HackathonSponsor(hs.hackathonId, sponsor.id, hs.order)
              sponsor.hackathons.associate(hackathon, hsTable)
            }
          }
          
          Redirect(routes.Sponsor.index).flashing("status" -> "sponsors.added")
        }
	)
  }
  
  def edit(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Sponsors.sponsors.lookup(id).map { sponsor =>
                   
        val hackathons = Model.hackathons.toList
        
        val hsList : List[model.HackathonSponsorHelper] = 
          hackathons.map ({ hackathon =>
            model.HackathonSponsorHelper(
                hackathon.id,
                model.Sponsors.findSponsorHackatonOrder(id, hackathon.id).getOrElse(-1)
            )
        }).toList
        
        Ok(views.html.sponsors.edit(id, sponsorForm.fill(sponsor.copy(hackathonsList = hsList)), hackathons, request.user))
        
      }.get
    }
  }
    
  def update(id: Long) = SecuredAction() { implicit request =>
    sponsorForm.bindFromRequest.fold(
        errors =>  transaction {
          BadRequest(views.html.sponsors.edit(id, errors, Model.hackathons.toList, request.user))
        },
        sponsor => transaction {
          model.Sponsors.sponsors.update(s =>
            where(s.id === id)
            set(
                s.name := sponsor.name,
                s.description := sponsor.description,
                s.order := sponsor.order,
                s.website := sponsor.website,
                s.isGeneralSponsor := sponsor.isGeneralSponsor
            )
          )
          
          sponsor.hackathonsList.map { hs =>
            
            if(hs.order == -1)
              model.Sponsors.deleteSponsorHackathon(id, hs.hackathonId)
            else
              model.Sponsors.updateSponsorHackathonOrder(id, hs.hackathonId, hs.order)
            
          }
          
          Redirect(routes.Sponsor.index)
          	.flashing("status" -> "sponsors.updated", "title" -> sponsor.name)
        }
    )
  }
  
  def delete(id: Long) = SecuredAction() { implicit request => 
  	transaction {
  	  
  	  model.Sponsors.sponsors.lookup(id).map { sponsor => 
  	    sponsor.hackathons.dissociateAll
  	  }
  	  
  	  model.Sponsors.sponsors.deleteWhere(s => s.id === id)
  	}
  	Redirect(routes.Sponsor.index).flashing("status" -> "sponsors.deleted")
  }
   
  
}