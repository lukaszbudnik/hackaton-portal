package helpers

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class ConditionsSpec extends Specification {

  "Team" should {

    "render always if status approved" in {

      val hackathon = new model.Hackathon()
      val team = new model.Team(-1, -1).copy(status = model.TeamStatus.Approved)

      val result = helpers.Conditions.Team.canRender(hackathon, team, None)

      result must beTrue

    }

    "render always when admin" in {

      val hackathon = new model.Hackathon()
      val team = new model.Team(-1, -1)
      val user = model.User("name", "email", "pl", "github", "twitter", "avatar", "openId", true, false)

      val result = helpers.Conditions.Team.canRender(hackathon, team, Some(user))

      result must beTrue

    }

    "render always when hackathon organiser" in {

      val hackathon = new model.Hackathon(0)
      val team = new model.Team(-1, -1)
      val user = model.User("name", "email", "pl", "github", "twitter", "avatar", "openId", false, false)

      val result = helpers.Conditions.Team.canRender(hackathon, team, Some(user))

      result must beTrue

    }

    "render always when team creator" in {

      val hackathon = new model.Hackathon(0)
      val team = new model.Team(0, -1)
      val user = model.User("name", "email", "pl", "github", "twitter", "avatar", "openId", false, false)

      val result = helpers.Conditions.Team.canRender(hackathon, team, Some(user))

      result must beTrue

    }

    "not render when normal user or not logged in and status other than approved" in {

      val hackathon = new model.Hackathon(0)
      val team = new model.Team(-1, -1)

      val result = helpers.Conditions.Team.canRender(hackathon, team, None)

      result must beFalse

    }

  }

  "Problem" should {

    "render always if status approved" in {

      val hackathon = new model.Hackathon()
      val problem = new model.Problem(-1, -1).copy(status = model.ProblemStatus.Approved)

      val result = helpers.Conditions.Problem.canRender(hackathon, problem, None)

      result must beTrue

    }

    "render always when admin" in {

      val hackathon = new model.Hackathon()
      val problem = new model.Problem(-1, -1)

      val user = model.User("name", "email", "pl", "github", "twitter", "avatar", "openId", true, false)

      val result = helpers.Conditions.Problem.canRender(hackathon, problem, Some(user))

      result must beTrue

    }

    "render always when hackathon organiser" in {

      val hackathon = new model.Hackathon(0)
      val problem = new model.Problem(-1, -1)

      val user = model.User("name", "email", "pl", "github", "twitter", "avatar", "openId", false, false)

      val result = helpers.Conditions.Problem.canRender(hackathon, problem, Some(user))

      result must beTrue

    }

    "render always when problem submitter" in {

      val hackathon = new model.Hackathon(0)
      val problem = new model.Problem(0, -1)

      val user = model.User("name", "email", "pl", "github", "twitter", "avatar", "openId", false, false)

      val result = helpers.Conditions.Problem.canRender(hackathon, problem, Some(user))

      result must beTrue

    }

    "not render when normal user or not logged in and status other than approved" in {

      val hackathon = new model.Hackathon(0)
      val problem = new model.Problem(-1, -1)

      val result = helpers.Conditions.Problem.canRender(hackathon, problem, None)

      result must beFalse

    }

  }

  "Hackathon" should {

    "render always if status other than Unverified" in {

      val hackathon = new model.Hackathon().copy(status = model.HackathonStatus.InProgress)

      val result = helpers.Conditions.Hackathon.canRender(hackathon, None)

      result must beTrue

    }

    "render always when admin" in {

      val hackathon = new model.Hackathon()

      val user = model.User("name", "email", "pl", "github", "twitter", "avatar", "openId", true, false)

      val result = helpers.Conditions.Hackathon.canRender(hackathon, Some(user))

      result must beTrue

    }

    "render always when hackathon organiser" in {

      val hackathon = new model.Hackathon(0)

      val user = model.User("name", "email", "pl", "github", "twitter", "avatar", "openId", false, false)

      val result = helpers.Conditions.Hackathon.canRender(hackathon, Some(user))

      result must beTrue

    }

    "not render when normal user or not logged in and status other than approved" in {

      val hackathon = new model.Hackathon(0)

      val result = helpers.Conditions.Hackathon.canRender(hackathon, None)

      result must beFalse

    }

  }

}