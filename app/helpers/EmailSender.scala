package helpers

import model.Hackathon
import model.Team
import play.api.Play.current
import plugins.emailnotifier.EmailNotifierPlugin
import plugins.use
import play.api.Play
import model.Problem
import play.api.i18n.Lang
import model.User

object EmailSender {

  private val from = Play.current.configuration.getString("application.noReply").get
  private val emailNotifierService = use[EmailNotifierPlugin].emailNotifierService

  def sendEmailToAdministrators(admins: Iterable[User], subject: String, body: String, params: Seq[String] = Seq()) = {
      admins.map { admin =>
      implicit val lang = Lang(admin.language)
      val mailBody = CmsMessages(body, params: _*)
      val mailSubject = CmsMessages(subject, params: _*)
      emailNotifierService.send(from, admin.email, mailSubject, mailBody, Some(mailBody))
    }
  }
  
  def sendEmailToHackathonOrganiser(hackathon: Hackathon, subject: String, body: String, params: Seq[String] = Seq()) = {
    implicit val lang = Lang(hackathon.organiser.language)
    val mailBody = CmsMessages(body, params: _*)
    val mailSubject = CmsMessages(subject, params: _*)

    emailNotifierService.send(from, hackathon.organiser.email, mailSubject, mailBody, Some(mailBody))
  }

  def sendEmailToProblemSubmitter(problem: Problem, subject: String, body: String, params: Seq[String] = Seq()) = {
    implicit val lang = Lang(problem.submitter.language)

    val mailBody = CmsMessages(body, params: _*)
    val mailSubject = CmsMessages(subject, params: _*)

    emailNotifierService.send(from, problem.submitter.email, mailSubject, mailBody, Some(mailBody))
  }

  def sendEmailToTeamCreator(team: Team, subject: String, body: String, params: Seq[String] = Seq()) = {
    implicit val lang = Lang(team.creator.language)

    val mailBody = CmsMessages(body, params: _*)
    val mailSubject = CmsMessages(subject, params: _*)

    emailNotifierService.send(from, team.creator.email, mailSubject, mailBody, Some(mailBody))
  }

  def sendEmailToWholeTeam(team: Team, subject: String, body: String, params: Seq[String] = Seq()) = {

    val teamMembers = Seq(team.creator) ++ team.members

    teamMembers.distinct.map { member =>
      implicit val lang = Lang(member.language)
      val mailBody = CmsMessages(body, params: _*)
      val mailSubject = CmsMessages(subject, params: _*)
      emailNotifierService.send(from, member.email, mailSubject, mailBody, Some(mailBody))
    }
  }

}