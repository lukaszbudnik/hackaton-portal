package controllers

import play.api._
import play.api.mvc._
import play.api.i18n._
import model.Model
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column

object Hackathon extends Controller with securesocial.core.SecureSocial {

  def hackathonsJson = Action {
    transaction {
      val hackathons = Model.hackathons.toList
      Ok(com.codahale.jerkson.Json.generate(hackathons)).as("application/json")
    }
  }
  
}