package security

import core.SecurityAbuseException
import play.api.mvc._
import org.squeryl.PrimitiveTypeMode.inTransaction

trait Security extends securesocial.core.SecureSocial {

  /**
   * general purpose ensure method
   */
  def ensure(condition: model.User => Boolean, optionalCondition: Boolean = true)(f: => Result)(implicit request: SecuredRequest[AnyContent]): Result = {
    inTransaction {
      val socialUser = request.user
      val user = model.User.lookupByOpenId(socialUser.id.id + socialUser.id.providerId)

      user match {
        case Some(u: model.User) if optionalCondition && condition(u) => f
        case _ => throw new SecurityAbuseException(socialUser)
      }
    }
  }

  /**
   * helper methods
   */
  def ensureAdmin(f: => Result)(implicit request: SecuredRequest[AnyContent]): Result = {
    inTransaction {
      ensure(u => u.isAdmin)(f)
    }
  }

  def ensureAdmin(optionalCondition: Boolean = true)(f: => Result)(implicit request: SecuredRequest[AnyContent]): Result = {
    inTransaction {
      ensure(u => optionalCondition && u.isAdmin)(f)
    }
  }

  def ensureHackathonOrganiserOrAdmin(hackathon: model.Hackathon, optionalCondition: Boolean = true)(f: => Result)(implicit request: SecuredRequest[AnyContent]): Result = {
    inTransaction {
      ensure(u => (optionalCondition && (u.isAdmin || hackathon.organiserId == u.id)))(f)
    }
  }

  def ensureHackathonOrganiserOrTeamLeaderOrAdmin(hackathon: model.Hackathon, team: model.Team, optionalCondition: Boolean = true)(f: => Result)(implicit request: SecuredRequest[AnyContent]): Result = {
    inTransaction {
      ensure(u => (optionalCondition && (u.isAdmin || hackathon.organiserId == u.id || team.creatorId == u.id)))(f)
    }
  }

  def ensureTeamLeaderOrAdmin(team: model.Team, optionalCondition: Boolean = true)(f: => Result)(implicit request: SecuredRequest[AnyContent]): Result = {
    inTransaction {
      ensure(u => (optionalCondition && (u.isAdmin || team.creatorId == u.id)))(f)
    }
  }

  def ensureHackathonOrganiserOrProblemSubmitterOrAdmin(hackathon: model.Hackathon, problem: model.Problem, optionalCondition: Boolean = true)(f: => Result)(implicit request: SecuredRequest[AnyContent]): Result = {
    inTransaction {
      ensure(u => (optionalCondition && (u.isAdmin || hackathon.organiserId == u.id || problem.submitterId == u.id)))(f)
    }
  }

  def ensureProblemSubmitterOrAdmin(problem: model.Problem, optionalCondition: Boolean = true)(f: => Result)(implicit request: SecuredRequest[AnyContent]): Result = {
    inTransaction {
      ensure(u => (optionalCondition && (u.isAdmin || problem.submitterId == u.id)))(f)
    }
  }
  
  def ensureLocationSubmitterOrAdmin(location: model.Location, optionalCondition: Boolean = true)(f: => Result)(implicit request: SecuredRequest[AnyContent]): Result = {
    inTransaction {
      ensure(u => (optionalCondition && (u.isAdmin || location.submitterId == u.id)))(f)
    }
  }
  
  def ensureNewsAuthorOrAdmin(news: model.News, optionalCondition: Boolean = true)(f: => Result)(implicit request: SecuredRequest[AnyContent]): Result = {
    inTransaction {
      ensure(u => (optionalCondition && (u.isAdmin || news.authorId == u.id)))(f)
    }
  }
  
  def ensureHackathonOrganiserOrNewsAuthorOrAdmin(hackathon: model.Hackathon, news: model.News, optionalCondition: Boolean = true)(f: => Result)(implicit request: SecuredRequest[AnyContent]): Result = {
    inTransaction {
      ensure(u => (optionalCondition && (u.isAdmin || news.authorId == u.id || hackathon.organiserId == u.id)))(f)
    }
  }
  
}