package security
import securesocial.core.SocialUser

object Team {

  def canEdit(team: model.Team, user: Option[model.User]): Boolean = {
    
    user.map { user =>
      user.isAdmin || team.creatorId == user.id || team.hackathon.organiserId == user.id
    }.getOrElse(false)

  }

}