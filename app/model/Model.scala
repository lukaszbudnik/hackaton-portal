package model
import java.util.Date
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column

case class News(title: String, text: String, labels: String, @Column("author_id") authorId: Long, published: Date) extends KeyedEntity[Long] {
  val id: Long = 0L
  lazy val author: ManyToOne[User] = Model.authorToNews.right(this)
}

case class User(name: String, email: String, @Column("github_username") githubUsername: String, @Column("twitter_account") twitterAccount: String, @Column("avatar_url") avatarUrl: String, @Column("open_id") openId: String) extends KeyedEntity[Long] {
  val id: Long = 0L
  lazy val roles = Model.usersToRoles.left(this)
  lazy val teams = Model.usersToTeams.left(this)
}

case class Role(name: String) extends KeyedEntity[Long] {
  val id: Long = 0L
}

case class UserRole(@Column("user_id") userId: Long, @Column("role_id") roleId: Long) extends KeyedEntity[CompositeKey2[Long,Long]] {
  def id = compositeKey(userId, roleId)
}

case class Problem(name: String, description: String, @Column("submitter_id") submitterId: Long, @Column("hackathon_id") hackathonId: Long) extends KeyedEntity[Long] {
  val id: Long = 0L
}

case class Prize(name: String, description: String, @Column("prize_order") order: String, @Column("hackathon_id") hackathonId: Long) extends KeyedEntity[Long] {
  val id: Long = 0L
}

case class Hackathon(subject: String, status: HackathonStatus.Value, @Column("submitter_id") submitterId: Long, @Column("location_id") locationId: Long) extends KeyedEntity[Long] {
  val id: Long = 0L
  lazy val location: ManyToOne[Location] = Model.locationToHackathons.right(this)

  def this() = this("", HackathonStatus.Planning, 1, 1)
}

case class Location(country: String,
                    city: String,
                    @Column("postal_code") postalCode: String,
                    @Column("full_address") fullAddress: String,
                    name: String,
                    latitude: Double,
                    longitude: Double) extends KeyedEntity[Long] {
  val id: Long = 0L
}

case class Team(name: String,
				@Column("creator_id") creatorId: Long,
				@Column("hackathon_id") hackathonId: Long,
				@Column("problem_id") problemId: Option[Long]) extends KeyedEntity[Long] {
  val id: Long = 0L
  def this() = this("", 0, 0, Some(0L))
  
  // Mapping to users for this team
  lazy val users = Model.usersToTeams.right(this)

}

case class UserTeam(@Column("user_id") userId: Long, @Column("team_id") teamId: Long) extends KeyedEntity[CompositeKey2[Long, Long]] {
  def id = compositeKey(userId, teamId)
}

object OrderByDirection extends Enumeration {
  type Direction = Value
  val Asc = Value("Asc")
  val Desc = Value("Desc")
}

object HackathonStatus extends Enumeration {
  val Planning = Value(1, "Planning")
  val InProgress = Value(2, "InProgress")
  val Finished = Value(3, "Finished")
}

object Model extends Schema {
  
  val news = table[News]
  val problems = table[Problem]("problems")
  val prizes = table[Prize]("prizes")
  val users = table[User]("users")
  val roles = table[Role]("roles")
  val hackathons = table[Hackathon]("hackathons")
  val locations = table[Location]("locations")
  val teams = table[Team]("teams")
  
  val locationToHackathons = oneToManyRelation(locations, hackathons).via((l, h) => l.id === h.locationId)
  val authorToNews = oneToManyRelation(users, news).via((u, n) => u.id === n.authorId)

  val usersToRoles =
    manyToManyRelation(users, roles, "users_roles").
      via[UserRole](f = (u, r, ur) => (u.id === ur.userId, r.id === ur.roleId))
      
  val usersToTeams =
    manyToManyRelation(users, teams, "users_teams").
      via[UserTeam](f = (u, t, ut) => (u.id === ut.userId, t.id === ut.teamId))

  def lookupNews(id: Long): Option[News] = {
    news.lookup(id)
  }

  def allNews(): Iterable[News] = {
    news.toIterable
  }

  def deleteAllNews() = {
    news.deleteWhere(n => n.id gt 0L)
  }
  
  def allNewsSortedByDateDesc(): Iterable[News] = {
	from (news)(n =>
        select(n)
        orderBy(n.published desc)
    )
  }
  
  def lookupProblem(id: Long): Option[Problem] = {
    problems.lookup(id)
  }
  
  def allProblems(): Iterable[Problem] = {
    problems.toIterable
  }
  
  def deleteAllProblems() = {
    problems.deleteWhere(p => p.id gt 0L)
  }
  
  def lookupHackathon(id: Long): Option[Hackathon] = {
    hackathons.lookup(id)
  }

  def lookupUser(id: Long) = {
    users.lookup(id)
  }
  
  def allPrizes(): Iterable[Prize] = {
    prizes.toIterable		  
  }
  
  def lookupPrize(id: Long): Option[Prize] = {
    prizes.lookup(id)
  }
    
  def findUserByOpenId(openId: String): Option[User] = {
    users.where(u => u.openId === openId).headOption
  }
  
  def allRoles(): Iterable[Role] = {
    roles.toIterable
  }
  
  def findRoleByName(name: String): Option[Role] = {
    roles.find(r => r.name == name)
  }
  
  def lookupTeam(id: Long): Option[Team] = {
    teams.lookup(id)
  }
  
  def allUsersForTeam(id : Long) = {
    lookupTeam(id).get.users
  }
  
}
