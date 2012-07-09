package model

import org.squeryl.dsl._
import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.annotations.Column

case class Team(name: String,
  @Column("creator_id") creatorId: Long,
  @Column("hackathon_id") hackathonId: Long,
  @Column("problem_id") problemId: Option[Long] = Some(0L)) extends KeyedEntity[Long] {
  val id: Long = 0L
  lazy val creator: ManyToOne[User] = Team.creatorToTeams.right(this)
  lazy val hackathon: ManyToOne[Hackathon] = Team.hackathonToTeams.right(this)
  lazy val problem = Team.problemToTeams.right(this);
  lazy val members = Team.usersToTeams.right(this)

  def hasMember(userId: Long): Boolean = {
    members.exists(u => u.id == userId)
  }
}

object Team extends Schema {
  val teams = table[Team]("teams")

  val creatorToTeams = oneToManyRelation(Model.users, teams).via((u, t) => u.id === t.creatorId)
  val hackathonToTeams = oneToManyRelation(Model.hackathons, teams).via((h, t) => h.id === t.hackathonId)
  val problemToTeams = oneToManyRelation(Model.problems, teams).via((p, t) => p.id === t.problemId)

  val usersToTeams =
    manyToManyRelation(Model.users, teams, "users_teams").
      via[UserTeam](f = (u, t, ut) => (u.id === ut.userId, t.id === ut.teamId))

  def all(): Iterable[Team] = {
    teams.toIterable
  }

  def lookup(id: Long): Option[Team] = {
    teams.lookup(id)
  }
  
  def add(team: Team): Team = {
    teams.insert(team)
  }
  
  def delete(id: Long): Int = {
      teams.deleteWhere(team => team.id === id)
  }
}