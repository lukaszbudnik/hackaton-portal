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
  	  
  	  val generalSponsors: Iterable[Long] = Model.findGeneralSponsorsIds()
  	  
  	  val hackathonSponsors: Map[Long, Iterable[Long]] = 
  	    Model.hackathons.toList.map( { h => (
  	        h.id, 
  	        Model.findSponsorsIdsByHackathonId(h.id)
  	    )}).toMap
  	  
  	  Ok(views.html.sponsors.index(Model.hackathons.toIterable, sponsors, generalSponsors, hackathonSponsors, request.user)) 
  	}
  }
  
}