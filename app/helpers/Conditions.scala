package helpers

object Conditions {

  object Hackathon {

    def canRender(hackathon: model.Hackathon, user: Option[model.User]) = {
      hackathon.status != model.HackathonStatus.Unverified || user.map { user =>
        user.isAdmin || user.id == hackathon.organiserId
      }.getOrElse(false)
    }

  }

  object Team {

    def canAdd(hackathon: model.Hackathon, user: Option[model.User]) = {

      user.map { user =>
        !hackathon.newTeamsDisabled || user.isAdmin || user.id == hackathon.organiserId
      }.getOrElse(false)

    }

    def canEdit(team: model.Team, user: Option[model.User]): Boolean = {

      user.map { user =>
        user.isAdmin || team.creatorId == user.id || team.hackathon.organiserId == user.id
      }.getOrElse(false)

    }

    def canRender(hackathon: model.Hackathon, team: model.Team, user: Option[model.User]) = {
      team.status == model.TeamStatus.Approved || user.map { user =>
        user.isAdmin || user.id == hackathon.organiserId || user.id == team.creatorId
      }.getOrElse(false)
    }

  }

  object Problem {

    def canAdd(hackathon: model.Hackathon, user: Option[model.User]) = {

      user.map { user =>
        !hackathon.newProblemsDisabled || user.isAdmin || user.id == hackathon.organiserId
      }.getOrElse(false)

    }

    def canRender(hackathon: model.Hackathon, problem: model.Problem, user: Option[model.User]) = {

      problem.status == model.ProblemStatus.Approved || user.map { user =>
        user.isAdmin || hackathon.organiserId == user.id || problem.submitterId == user.id
      }.getOrElse(false)

    }

  }

}