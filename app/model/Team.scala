package model

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.CompositeKey2
import org.squeryl.dsl.ManyToOne
import org.squeryl.KeyedEntity
import org.squeryl.Schema
import org.squeryl.annotations.Column

object TeamStatus extends Enumeration {
  val Unverified = Value(1, "Unverified")
  val Approved = Value(2, "Approved")
  val Suspended = Value(3, "Suspended")
  val Blocked = Value(4, "Blocked")
}

case class Team(name: String,
  status: TeamStatus.Value,
  @Column("creator_id") creatorId: Long,
  @Column("hackathon_id") hackathonId: Long,
  @Column("problem_id") problemId: Option[Long] = None) extends KeyedEntity[Long] {
  val id: Long = 0L
  
  def this(creatorId: Long, hackathonId: Long) = this("", TeamStatus.Unverified, creatorId, hackathonId)

  private lazy val creatorRel: ManyToOne[User] = Team.creatorToTeams.right(this)
  private lazy val hackathonRel: ManyToOne[Hackathon] = Team.hackathonToTeams.right(this)
  private lazy val problemRel: ManyToOne[Problem] = Team.problemToTeams.right(this);
  private lazy val membersRel = Team.usersToTeams.right(this)

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

case class UserTeam(@Column("user_id") userId: Long,
  @Column("team_id") teamId: Long) extends KeyedEntity[CompositeKey2[Long, Long]] {
  def id = compositeKey(userId, teamId)
}

object Team extends Schema {
  protected[model] val teams = table[Team]("teams")
  on(teams)(t => declare(t.id is (primaryKey, autoIncremented("team_id_seq"))))

  protected[model] val creatorToTeams = oneToManyRelation(User.users, Team.teams).via((u, t) => u.id === t.creatorId)
  protected[model] val hackathonToTeams = oneToManyRelation(Hackathon.hackathons, Team.teams).via((h, t) => h.id === t.hackathonId)
  protected[model] val problemToTeams = oneToManyRelation(Problem.problems, Team.teams).via((p, t) => p.id === t.problemId)

  protected[model] val usersToTeams =
    manyToManyRelation(User.users, Team.teams, "users_teams").
      via[UserTeam](f = (u, t, ut) => (u.id === ut.userId, t.id === ut.teamId))

  def all(): Iterable[Team] = {
    teams.toIterable
  }

  def lookup(id: Long): Option[Team] = {
    teams.lookup(id)
  }

  def insert(team: Team): Team = {
    teams.insert(team)
  }

  def update(id: Long, team: Team): Int = {
    teams.update(t =>
      where(t.id === id)
        set (
          t.name := team.name,
          t.status := team.status,
          t.creatorId := team.creatorId,
          t.hackathonId := team.hackathonId,
          t.problemId := team.problemId))
  }

  def delete(id: Long): Int = {
    teams.deleteWhere(t => t.id === id)
  }
}