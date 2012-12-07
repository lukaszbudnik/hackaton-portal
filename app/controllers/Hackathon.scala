package controllers

import java.text.SimpleDateFormat
import org.squeryl.PrimitiveTypeMode.inTransaction
import helpers.Forms.enum
import model.dto.HackathonWithLocations
import play.api.data.Forms.date
import play.api.data.Forms.list
import play.api.data.Forms.boolean
import play.api.data.Forms.of
import play.api.data.Forms._
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

object Hackathon extends LangAwareController {

  private lazy val unVerifiedStatus = Seq(model.HackathonStatus.Unverified.toString -> model.HackathonStatus.Unverified.toString)
  private lazy val editStatuses = model.HackathonStatus.values.filterNot(_ == model.HackathonStatus.Unverified).map{ s => s.toString -> s.toString}.toSeq
  private lazy val allStatuses = model.HackathonStatus.values.map{ s => s.toString -> s.toString}.toSeq
  
  private def hackathonWithLocations2Json(h: model.dto.HackathonWithLocations): JsValue = {

    toJson(Map(
      "id" -> toJson(h.hackathon.id),
      "subject" -> toJson(h.hackathon.subject),
      "status" -> toJson(h.hackathon.status.id.toString),
      "date" -> toJson(new SimpleDateFormat("dd-MM-yyyy").format(h.hackathon.date)),
      "locations" -> toJson(h.locations.map {
        l =>
          toJson(Map(
            "id" -> toJson(l.id),
            "city" -> toJson(l.city),
            "country" -> toJson(l.country),
            "fullAddress" -> toJson(l.fullAddress),
            "name" -> toJson(l.name),
            "postalCode" -> toJson(l.postalCode),
            "latitude" -> toJson(l.latitude),
            "longitude" -> toJson(l.longitude)))
      }.toSeq)))

  }

  def hackathonsJson = UserAwareAction { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      val hackathons = model.dto.HackathonWithLocations.all

      Ok(toJson(hackathons.filter(h => helpers.Conditions.Hackathon.canRender(h.hackathon, user)).map {
        hWl => hackathonWithLocations2Json(hWl)
      }.toSeq))
    }
  }

  def hackathonJson(id: Long) = UserAwareAction { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      Ok(model.dto.HackathonWithLocations.lookup(id).filter(h => helpers.Conditions.Hackathon.canRender(h.hackathon, user)) match {
        case Some(h) =>
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
      "fullAddress" -> text,
      "submitterId" -> longNumber) // apply location
      ((id, name, city, country, fullAddress, submitterId) =>
        new model.Location(id, country, city, "", fullAddress, name, 0, 0, submitterId, model.LocationStatus.Unverified)) // unapply location
        ((l: model.Location) =>
        Some(l.id, l.name, l.city, l.country, l.fullAddress, l.submitterId))))

  val hackathonForm = Form(
    mapping(
      "subject" -> helpers.Forms.nonEmptyTextNonHtml,
      "status" -> enum(model.HackathonStatus),
      "date" -> date("dd-MM-yyyy"),
      "description" -> helpers.Forms.nonEmptyTextSimpleHtmlOnly,
      "organizerId" -> longNumber,
      "new_problems_disabled" -> boolean,
      "new_teams_disabled" -> boolean,
      "locations" -> list(mapping(
        "id" -> longNumber,
        "name" -> helpers.Forms.nonEmptyTextNonHtml,
        "city" -> helpers.Forms.nonEmptyTextNonHtml,
        "country" -> helpers.Forms.nonEmptyTextNonHtml,
        "fullAddress" -> helpers.Forms.nonEmptyTextNonHtml,
        "submitterId" -> longNumber) // apply location
        ((id, name, city, country, fullAddress, submitterId) =>
          model.Location(id, country, city, "", fullAddress, name, 0, 0, submitterId, model.LocationStatus.Unverified)) // unapply location
          ((l: model.Location) =>
          Some(l.id, l.name, l.city, l.country, l.fullAddress, l.submitterId)))) // apply HackathonWithLocations
          ((subject, status, date, description, organizerId, newProblemsDisabled, newTeamsDisabled, locations) =>
        HackathonWithLocations(
          model.Hackathon(subject, status, date, description, organizerId, newProblemsDisabled, newTeamsDisabled), locations)) // unapply HackathonWithLocations
          ((hWl: HackathonWithLocations) =>
        Some(hWl.hackathon.subject, hWl.hackathon.status, hWl.hackathon.date, hWl.hackathon.description, hWl.hackathon.organiserId, hWl.hackathon.newProblemsDisabled, hWl.hackathon.newTeamsDisabled, hWl.locations.toList)))

  def addLocation = UserAwareAction { implicit request =>
    val lb = ListBuffer[model.Location]()
    locationsSubForm.bindFromRequest.fold(
      errors => {
        sys.error("addLocation - should never happen!")
      }, locations => {
        lb ++= locations
        lb += new model.Location()
        Ok(views.html.hackathons.locationsContainer(
          hackathonForm.fill(HackathonWithLocations(new model.Hackathon(), lb.toList))))
      })
  }

  def deleteLocation(idx: Int) = UserAwareAction { implicit request =>
    val lb = ListBuffer[model.Location]()
    locationsSubForm.bindFromRequest.fold(
      errors => {
        sys.error("deleteLocation - should never happen!")
      }, locations => {
        lb ++= locations
        lb.remove(idx)
        Ok(views.html.hackathons.locationsContainer(
          hackathonForm.fill(HackathonWithLocations(new model.Hackathon(), lb.toList))))
      })
  }

  def index = UserAwareAction { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      Ok(views.html.hackathons.index(model.Hackathon.all.filter(h => helpers.Conditions.Hackathon.canRender(h, user)).toSeq.sortWith((a, b) => a.date.after(b.date)), userFromRequest(request)))
    }
  }

  def view(id: Long) = UserAwareAction { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      model.Hackathon.lookup(id).map { hackathon =>
        Ok(views.html.hackathons.view(Some(hackathon), user))
      }.getOrElse {
        NotFound(views.html.hackathons.view(None, user))
      }
    }
  }

  def chat(id: Long) = UserAwareAction { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      model.Hackathon.lookup(id).map { hackathon =>
        Ok(views.html.hackathons.chat(Some(hackathon), user))
      }.getOrElse {
        NotFound(views.html.hackathons.view(None, user))
      }
    }
  }

  def create = SecuredAction() { implicit request =>
    inTransaction {
      val user = userFromRequest(request)

      val hackathon = new model.dto.HackathonWithLocations(new model.Hackathon(user.id), List[model.Location](new model.Location))
      
      Ok(views.html.hackathons.create(hackathonForm.fill(hackathon), user, unVerifiedStatus))
    }
  }

  def save = SecuredAction() { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      hackathonForm.bindFromRequest.fold(
        errors => BadRequest(views.html.hackathons.create(errors, user, unVerifiedStatus)),
        hackathonWithLocations => {
          val newH = model.Hackathon.insert(hackathonWithLocations.hackathon.copy(organiserId = user.id, status = model.HackathonStatus.Unverified))
          hackathonWithLocations.locations.map {
            location =>
              newH.addLocation(location.copy(submitterId = user.id))
          }
          Redirect(routes.Hackathon.index).flashing("status" -> "added", "title" -> newH.subject)
        })
    }
  }

  def edit(id: Long) = SecuredAction() { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      model.dto.HackathonWithLocations.lookup(id).map { hackathonWithL =>
        ensureHackathonOrganiserOrAdmin(hackathonWithL.hackathon) {
          
          val statuses = if (user.isAdmin) allStatuses else editStatuses
          
          Ok(views.html.hackathons.edit(id, hackathonForm.fill(hackathonWithL), user, statuses))
        }
      }.getOrElse {
        NotFound(views.html.hackathons.view(None, Some(user)))
      }
    }
  }

  def update(id: Long) = SecuredAction() { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      model.Hackathon.lookup(id).map { dbHackathon =>

        ensureHackathonOrganiserOrAdmin(dbHackathon) {
          
          val statuses = if (user.isAdmin) allStatuses else editStatuses

          hackathonForm.bindFromRequest.fold(
            errors => BadRequest(views.html.hackathons.edit(id, errors, user, statuses)),
            hackathonWithL => {

              val newStatus = if (user.id == dbHackathon.organiserId && dbHackathon.status == model.HackathonStatus.Unverified) model.HackathonStatus.Unverified else hackathonWithL.hackathon.status 
              
              val newHackathon = hackathonWithL.hackathon.copy(organiserId = dbHackathon.organiserId, status = newStatus)
              
              model.Hackathon.update(id, newHackathon)

              // we have to restore previous statuses
              val locationsMap = dbHackathon.locations.map { t => (t.id, t) }.toMap
              dbHackathon.deleteLocations()
              hackathonWithL.locations.map {
                location =>
                  val lookupLoc = locationsMap.get(location.id)
                  if (lookupLoc.isDefined) {
                    newHackathon.addLocation(location.copy(status = lookupLoc.get.status))
                  } else {
                    newHackathon.addLocation(location)
                  }

              }
              Redirect(routes.Hackathon.index).flashing("status" -> "updated", "title" -> hackathonWithL.hackathon.subject)
            })
        }
      }.getOrElse(NotFound(views.html.hackathons.view(None, Some(user))))
    }
  }

  def delete(id: Long) = SecuredAction() { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      model.Hackathon.lookup(id).map { hackathon =>

        ensureHackathonOrganiserOrAdmin(hackathon) {
          model.Hackathon.delete(id)
          Redirect(routes.Hackathon.index).flashing("status" -> "deleted")
        }

      }.getOrElse(NotFound(views.html.hackathons.view(None, Some(user))))
    }
  }

  def join(id: Long) = SecuredAction() { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      model.Hackathon.lookup(id).map { hackathon =>
        if (!hackathon.hasMember(user.id)) {
          hackathon.addMember(user)
        }
        Redirect(routes.Hackathon.view(id)).flashing("status" -> "joined")
      }.getOrElse {
        NotFound(views.html.hackathons.view(None, Some(user)))
      }
    }
  }

  def disconnect(id: Long) = SecuredAction() { implicit request =>
    inTransaction {
      val user = userFromRequest(request)
      model.Hackathon.lookup(id).map { hackathon =>
        hackathon.deleteMember(user)
        Redirect(routes.Hackathon.view(id)).flashing("status" -> "disconnected")
      }.getOrElse {
        NotFound(views.html.hackathons.view(None, Some(user)))
      }
    }
  }

  def disconnectUser(id: Long, userId: Long) = SecuredAction() { implicit request =>
    inTransaction {
      ensureAdmin {
        val user = userFromRequest(request)
        model.Hackathon.lookup(id).map { hackathon =>
          model.User.lookup(userId).filter(u => hackathon.hasMember(u.id)).map { userToRemove =>
            hackathon.deleteMember(userToRemove)
            Redirect(routes.Hackathon.view(id)).flashing("status" -> "disconnectedUser")
          }.getOrElse {
            Redirect(routes.Hackathon.view(id)).flashing("status" -> "error")
          }
        }.getOrElse {
          NotFound(views.html.hackathons.view(None, Some(user)))
        }
      }
    }
  }
}
