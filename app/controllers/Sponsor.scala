package controllers

import org.squeryl.PrimitiveTypeMode._
import play.api.mvc._
import model.Model
import play.api.data._
import play.api.data.Forms._
import play.api.Logger
import javax.persistence.OrderBy

object Sponsor extends Controller with securesocial.core.SecureSocial {
  
  def index = UserAwareAction { implicit request =>
  	transaction {
  	    	  
  	  val sponsors: Map[Long, model.Sponsor] = Model.sponsors.toList.map({ s => (s.id, s) }).toMap
  	  
  	  val generalSponsors: Iterable[Long] = Model.findGeneralSponsorsOrdered().map {s => s.id}
  	  
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
      Ok(views.html.sponsors.view(Model.sponsors.lookup(id), hackathons, request.user))
      
    }
  }
  
  val sponsorForm = Form(
      mapping(
          "name" 				-> nonEmptyText,
          "description"			-> nonEmptyText,
          "website"				-> nonEmptyText,
          "order"  				-> number,
          "isGeneralSponsor" 	-> boolean
      )(model.Sponsor.apply)(model.Sponsor.unapply)
  )
  
  def create = SecuredAction() { implicit request =>
  	transaction {
  	  Ok(views.html.sponsors.create(sponsorForm, request.user))
  	}
  }
  
  def save = SecuredAction() { implicit request =>
    sponsorForm.bindFromRequest.fold(
        errors =>  transaction {
          BadRequest(views.html.sponsors.create(errors, request.user))
        },sponsor => transaction {
          Model.sponsors.insert(sponsor)
          Redirect(routes.Sponsor.index).flashing("status" -> "sponsors.added")
        }
	)
  }
  
  def edit(id: Long) = SecuredAction() { implicit request =>
    transaction {
      Model.sponsors.lookup(id).map { sponsor =>
        Ok(views.html.sponsors.edit(id, sponsorForm.fill(sponsor), request.user))
      }.get
    }
  }
    
  def update(id: Long) = SecuredAction() { implicit request =>
    sponsorForm.bindFromRequest.fold(
        errors =>  transaction {
          BadRequest(views.html.sponsors.edit(id, errors, request.user))
        },
        sponsor => transaction {
          Model.sponsors.update(s =>
            where(s.id === id)
            set(
                s.name := sponsor.name,
                s.description := sponsor.description,
                s.order := sponsor.order,
                s.website := sponsor.website,
                s.isGeneralSponsor := sponsor.isGeneralSponsor
            )
          )
          Redirect(routes.Sponsor.index)
          	.flashing("status" -> "sponsors.updated", "title" -> sponsor.name)
        }
    )
  }
  
  def delete(id: Long) = SecuredAction() { implicit request => 
  	transaction {
  	  Model.sponsors.deleteWhere(s => s.id === id)
  	}
  	Redirect(routes.Sponsor.index).flashing("status" -> "sponsors.deleted")
  }
   
  
}