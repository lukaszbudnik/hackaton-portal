package model

import org.squeryl.dsl._
import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.annotations.Column

case class Team(name: String,
  @Column("creator_id") creatorId: Long,
  @Column("hackathon_id") hackathonId: Long,
  @Column("problem_id") problemId: Option[Long] = None) extends KeyedEntity[Long] {
  val id: Long = 0L
  
  protected[model] lazy val creatorRel: ManyToOne[User] = Team.creatorToTeams.right(this)
  protected[model] lazy val hackathonRel: ManyToOne[Hackathon] = Team.hackathonToTeams.right(this)
  protected[model] lazy val problemRel = Team.problemToTeams.right(this);
  protected[model] lazy val membersRel = Team.usersToTeams.right(this)
  
  def creator = creatorRel.head
  def hackathon = hackathonRel.head
  def problem = problemRel.headOption
  def members = membersRel.toIterable

  def hasMember(userId: Long): Boolean = {
    membersRel.exists(u => u.id == userId)
  }

  def addMember(user: User) = {
    membersRel.associate(user)
  }

  def deleteMember(user: User) = {
    membersRel.dissociate(user)
  }
}

object Team extends Schema {
  protected[model] val teams = table[Team]("teams")

  protected[model] val creatorToTeams = oneToManyRelation(model.User.users, teams).via((u, t) => u.id === t.creatorId)
  protected[model] val hackathonToTeams = oneToManyRelation(Model.hackathons, teams).via((h, t) => h.id === t.hackathonId)
  protected[model] val problemToTeams = oneToManyRelation(Model.problems, teams).via((p, t) => p.id === t.problemId)

  protected[model] val usersToTeams =
    manyToManyRelation(model.User.users, teams, "users_teams").
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

  def update(id: Long, team: Team): Int = {
    teams.update(t =>
      where(t.id === id)
        set (
          t.name := team.name,
          t.creatorId := team.creatorId,
          t.hackathonId := team.hackathonId,
          t.problemId := team.problemId))
  }

  def delete(id: Long): Int = {
    teams.deleteWhere(team => team.id === id)
  }
}