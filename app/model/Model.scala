package model
import java.util.Date
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column

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
  lazy val teams = Team.hackathonToTeams.left(this)
  lazy val problems = Model.hackathonToProblems.left(this)
  def sponsors = from(model.Sponsors.hackathonsToSponsors.left(this))(hs => select(hs) orderBy(hs.order asc))
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

case class UserTeam(@Column("user_id") userId: Long,
					@Column("team_id") teamId: Long) extends KeyedEntity[CompositeKey2[Long, Long]] {
  def id = compositeKey(userId, teamId)
}

object HackathonStatus extends Enumeration {
  val Planning = Value(1, "Planning")
  val InProgress = Value(2, "InProgress")
  val Finished = Value(3, "Finished")
}

object Model extends Schema {
  
  val problems = table[Problem]("problems")
  val hackathons = table[Hackathon]("hackathons")
  val locations = table[Location]("locations")

  
  val submitterToHackathons = oneToManyRelation(User.users, hackathons).via((u, h) => u.id === h.submitterId)  
  val locationToHackathons = oneToManyRelation(locations, hackathons).via((l, h) => l.id === h.locationId)
  val submitterToProblems = oneToManyRelation(User.users, problems).via((u, p) => u.id === p.submitterId)
  val hackathonToProblems = oneToManyRelation(hackathons, problems).via((h, p) => h.id === p.hackathonId)
    
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
  
  def allHackathonsForSponsor(id: Long) = {
    model.Sponsors.lookup(id).get.hackathons
  }
}
