package model
import java.util.Date
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column

case class User(name: String,
				email: String,
				@Column("github_username") githubUsername: String,
				@Column("twitter_account") twitterAccount: String,
				@Column("avatar_url") avatarUrl: String,
				@Column("open_id") openId: String) extends KeyedEntity[Long] {
  val id: Long = 0L
  lazy val roles = Model.usersToRoles.left(this)
  lazy val teams = Model.usersToTeams.left(this)
}

case class Role(name: String) extends KeyedEntity[Long] {
  val id: Long = 0L
}

case class UserRole(@Column("user_id") userId: Long,
					@Column("role_id") roleId: Long) extends KeyedEntity[CompositeKey2[Long,Long]] {
  def id = compositeKey(userId, roleId)
}

case class Problem(name: String,
				   description: String,
				   @Column("submitter_id") submitterId: Long,
				   @Column("hackathon_id") hackathonId: Long) extends KeyedEntity[Long] {
  val id: Long = 0L
  lazy val submitter : ManyToOne[User] = Model.submitterToProblems.right(this)
  lazy val hackathon : ManyToOne[Hackathon] = Model.hackathonToProblems.right(this)
}

case class Hackathon(subject: String,
					 status: HackathonStatus.Value,
					 @Column("submitter_id") submitterId: Long,
					 @Column("location_id") locationId: Long) extends KeyedEntity[Long] {
  val id: Long = 0L
  lazy val submitter : ManyToOne[User] = Model.submitterToHackathons.right(this)  
  lazy val location: ManyToOne[Location] = Model.locationToHackathons.right(this)
  lazy val teams = Model.hackathonToTeams.left(this)
  lazy val problems = Model.hackathonToProblems.left(this)
  def this() = this("", HackathonStatus.Planning, 1, 1)
}

case class Prize(name: String, 
				 description: String, 
				 @Column("prize_order") order: Int,
				 @Column("founder_name") founderName: Option[String],
				 @Column("founder_web_page") founderWebPage: Option[String],
				 @Column("hackathon_id") hackathonId: Long) extends KeyedEntity[Long] {
  val id: Long = 0L
}

case class Sponsor(name: String, 
				 description: String,
				 website: String,
				 @Column("sponsor_order") order: Int,
				 @Column("is_general_sponsor") isGeneralSponsor: Boolean) extends KeyedEntity[Long] {
  val id: Long = 0L
}

case class HackathonSponsor(@Column("hackathon_id") hackathonId: Long,
							@Column("sponsor_id") sponsorId: Long,
							@Column("sponsor_order") order: Int) extends KeyedEntity[CompositeKey2[Long,Long]] {
  def id = compositeKey(hackathonId, sponsorId)
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
				@Column("problem_id") problemId: Option[Long] = Some(0L)) extends KeyedEntity[Long] {
  val id: Long = 0L
  lazy val creator : ManyToOne[User] = Model.creatorToTeams.right(this)  
  lazy val hackathon : ManyToOne[Hackathon] = Model.hackathonToTeams.right(this)
  lazy val problem = Model.problemToTeams.right(this);
  lazy val users = Model.usersToTeams.right(this)
  def hasMember(userId: Long) : Boolean = {
    users.map{
      u => if(u.id == userId) return true
    }
    false
  }
}

case class UserTeam(@Column("user_id") userId: Long,
					@Column("team_id") teamId: Long) extends KeyedEntity[CompositeKey2[Long, Long]] {
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
  
  val problems = table[Problem]("problems")
  val prizes = table[Prize]("prizes")
  val users = table[User]("users")
  val roles = table[Role]("roles")
  val hackathons = table[Hackathon]("hackathons")
  val sponsors = table[Sponsor]("sponsors")
  val locations = table[Location]("locations")
  val teams = table[Team]("teams")
    
  val hackathonsToSponsors = 
    manyToManyRelation(hackathons, sponsors, "hackathons_sponsors").
    via[HackathonSponsor](f = (h, s, hs) => (h.id === hs.hackathonId, s.id === hs.sponsorId))
  
  val submitterToHackathons = oneToManyRelation(users, hackathons).via((u, h) => u.id === h.submitterId)  
  val locationToHackathons = oneToManyRelation(locations, hackathons).via((l, h) => l.id === h.locationId)
  val submitterToProblems = oneToManyRelation(users, problems).via((u, p) => u.id === p.submitterId)
  val hackathonToProblems = oneToManyRelation(hackathons, problems).via((h, p) => h.id === p.hackathonId)
  val creatorToTeams = oneToManyRelation(users, teams).via((u, t) => u.id === t.creatorId)
  val hackathonToTeams = oneToManyRelation(hackathons, teams).via((h, t) => h.id === t.hackathonId)
  val problemToTeams = oneToManyRelation(problems, teams).via((p, t) => p.id === t.problemId)

  val usersToRoles =
    manyToManyRelation(users, roles, "users_roles").
      via[UserRole](f = (u, r, ur) => (u.id === ur.userId, r.id === ur.roleId))
      
  val usersToTeams =
    manyToManyRelation(users, teams, "users_teams").
      via[UserTeam](f = (u, t, ut) => (u.id === ut.userId, t.id === ut.teamId))
      
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
  
  def allPrizesOrdered(): Iterable[Prize] = {
	from (prizes)(p =>
	  select(p)
	  orderBy(p.order asc)
    )
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
  
  def findSponsorsIdsByHackathonId(hackathonId : Long) :Iterable[Long] = {
    from (Model.hackathonsToSponsors) (hs =>
      where(hs.hackathonId === hackathonId)
      select(hs.sponsorId)
      orderBy(hs.order asc)
    )
  }
  
  def findGeneralSponsorsIds() : Iterable[Long] = {
    from(Model.sponsors)(s =>
      where(s.isGeneralSponsor === true)
      select(s.id)
      orderBy(s.order asc)
    )
  }
}
