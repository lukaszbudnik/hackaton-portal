package controllers

import org.squeryl.PrimitiveTypeMode._
import play.api.mvc._
import model.Model
import play.api.data._
import play.api.data.Forms._
import play.api.Logger

object Prize extends Controller with securesocial.core.SecureSocial {
  
  def index = UserAwareAction { implicit request =>
  	transaction {
  	  Ok(views.html.prizes.index(model.Prizes.allOrdered.toList, request.user)) 
  	}
  }
  
  def view(id: Long) = UserAwareAction { implicit request =>
    transaction {
      Ok(views.html.prizes.view(model.Prizes.lookup(id), request.user))
    }
  }
  
  val prizeForm = Form(
      mapping(
          "name" 			-> nonEmptyText,
          "description"		-> nonEmptyText,
          "order"  			-> number,
          "founderName"    	-> optional(nonEmptyText),
          "founderWebPage"	-> optional(nonEmptyText),
          "hackathonId" 	-> longNumber
      )(model.Prize.apply)(model.Prize.unapply)
  )
  
  def create = SecuredAction() { implicit request =>
  	transaction {
  	  Ok(views.html.prizes.create(prizeForm, Model.hackathons.toList, request.user))
  	}
  }
  
  def save = SecuredAction() { implicit request =>
    prizeForm.bindFromRequest.fold(
        errors =>  transaction {
          BadRequest(views.html.prizes.create(errors, Model.hackathons.toList, request.user))
        },prize => transaction {
          model.Prizes.prizes.insert(prize)
          Redirect(routes.Prize.index).flashing("status" -> "prizes.added")
        }
	)
  }
  
  def edit(id: Long) = SecuredAction() { implicit request =>
    transaction {
      model.Prizes.lookup(id).map { prize =>
        Ok(views.html.prizes.edit(id, prizeForm.fill(prize), Model.hackathons.toList, request.user))
      }.get
    }
  }
    
  def update(id: Long) = SecuredAction() { implicit request =>
    prizeForm.bindFromRequest.fold(
        errors =>  transaction {
          BadRequest(views.html.prizes.edit(id, errors, Model.hackathons.toList, request.user))
        },
        prize => transaction {
        	model.Prizes.prizes.update(p =>
            where(p.id === id)
            set(
                p.name := prize.name,
                p.description := prize.description,
                p.order := prize.order,
                p.hackathonId := prize.hackathonId,
                p.founderName := prize.founderName,
                p.founderWebPage := prize.founderWebPage
            )
          )
          Redirect(routes.Prize.index)
          	.flashing("status" -> "prizes.updated", "title" -> prize.name)
        }
    )
  }
  
  def delete(id: Long) = SecuredAction() { implicit request => 
  	transaction {
  	  model.Prizes.prizes.deleteWhere(p => p.id === id)
  	}
  	Redirect(routes.Prize.index).flashing("status" -> "prizes.deleted")
  }
  
}