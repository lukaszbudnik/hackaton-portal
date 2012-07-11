package model
import java.util.Date
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column

case class Hackathon(subject: String,
					 status: HackathonStatus.Value,
					 @Column("submitter_id") submitterId: Long,
					 @Column("location_id") locationId: Long) extends KeyedEntity[Long] {
  val id: Long = 0L
  lazy val submitter : ManyToOne[User] = Model.submitterToHackathons.right(this)  
  lazy val location: ManyToOne[Location] = Model.locationToHackathons.right(this)
  lazy val teams = Team.hackathonToTeams.left(this)
  lazy val problems = Problem.hackathonToProblems.left(this)
  lazy val sponsors = from(Model.hackathonsToSponsors.left(this))(hs => select(hs) orderBy(hs.order asc))
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
  lazy val hackathons = Model.hackathonsToSponsors.right(this)
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
  
  val prizes = table[Prize]("prizes")
  val hackathons = table[Hackathon]("hackathons")
  val sponsors = table[Sponsor]("sponsors")
  val locations = table[Location]("locations")

    
  val hackathonsToSponsors = 
    manyToManyRelation(hackathons, sponsors, "hackathons_sponsors").
    via[HackathonSponsor](f = (h, s, hs) => (h.id === hs.hackathonId, s.id === hs.sponsorId))
  
  val submitterToHackathons = oneToManyRelation(User.users, hackathons).via((u, h) => u.id === h.submitterId)  
  val locationToHackathons = oneToManyRelation(locations, hackathons).via((l, h) => l.id === h.locationId)
 
  def lookupHackathon(id: Long): Option[Hackathon] = {
    hackathons.lookup(id)
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
        
  def findGeneralSponsorsOrdered() : Iterable[Sponsor] = {
    from(sponsors)(s =>
      where(s.isGeneralSponsor === true)
      select(s)
      orderBy(s.order)
      )
  }
  
  def allHackathonsForSponsor(id: Long) = {
    sponsors.lookup(id).get.hackathons
  }
}
