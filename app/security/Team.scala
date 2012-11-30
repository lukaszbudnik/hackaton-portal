package security
import securesocial.core.SocialUser

object Team {

  def canAddTeam(hackathon: model.Hackathon, user: Option[model.User]) = {

    user.map { user =>
      !hackathon.newTeamsDisabled || user.isAdmin || user.id == hackathon.organiserId
    }.getOrElse(false)

  }

  def canEdit(team: model.Team, user: Option[model.User]): Boolean = {

    user.map { user =>
      user.isAdmin || team.creatorId == user.id || team.hackathon.organiserId == user.id
    }.getOrElse(false)

  }

}