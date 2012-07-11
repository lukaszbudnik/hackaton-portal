package model

import org.squeryl.dsl._
import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.annotations.Column

case class User(name: String,
				email: String,
				@Column("github_username") githubUsername: String,
				@Column("twitter_account") twitterAccount: String,
				@Column("avatar_url") avatarUrl: String,
				@Column("open_id") openId: String) extends KeyedEntity[Long] {
  val id: Long = 0L
  
  protected[model] lazy val rolesRel = User.usersToRoles.left(this)
  protected[model] lazy val teamsRel = Team.usersToTeams.left(this)
  
  def roles = rolesRel.toIterable
  
  def addRole(role: Role) = {
    rolesRel.associate(role)
  }

  def deleteRole(role: Role) = {
    rolesRel.dissociate(role)
  }
  
  def teams = teamsRel.toIterable
  
  def addTeam(team: Team) = {
    teamsRel.associate(team)
  }

  def deleteTeam(team: Team) = {
    teamsRel.dissociate(team)
  }
}

object User extends Schema {
  protected[model] val users = table[User]("users")

  protected[model] val usersToRoles =
    manyToManyRelation(users, model.Role.roles, "users_roles").
      via[UserRole](f = (u, r, ur) => (u.id === ur.userId, r.id === ur.roleId))
      
  def all(): Iterable[User] = {
    users.toIterable
  }

  def lookup(id: Long): Option[User] = {
    users.lookup(id)
  }

  def lookupByOpenId(openId: String): Option[User] = {
    users.find(u => u.openId == openId)
  }
  
  def add(user: User): User = {
    users.insert(user)
  }

  def delete(id: Long): Int = {
    users.deleteWhere(team => team.id === id)
  }
}