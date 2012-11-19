package security

object Hackathon {

  def canAddTeam(hackathon: model.Hackathon, user: Option[model.User]) = {

    user.map { user =>
      !hackathon.newTeamsDisabled || user.isAdmin || user.id == hackathon.organiserId
    }.getOrElse(false)

  }

  def canRenderTeam(hackathon: model.Hackathon, team: model.Team, user: Option[model.User]) = {
    user.map { user =>
      user.isAdmin || team.status == model.TeamStatus.Approved || (user.id == hackathon.organiserId || user.id == team.creatorId)
    }.getOrElse(false)
  }

}