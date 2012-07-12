package model

import java.util.Date
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column

case class Hackathon(subject: String,
  status: HackathonStatus.Value,
  @Column("organiser_id") organiserId: Long,
  @Column("location_id") locationId: Long) extends KeyedEntity[Long] {
  val id: Long = 0L
  def this() = this("", HackathonStatus.Planning, 0, 0) // need for status enumeration
  
  private lazy val organiserRel: ManyToOne[User] = Hackathon.organiserToHackathons.right(this)
  private lazy val locationRel: ManyToOne[Location] = Hackathon.locationToHackathons.right(this)
  private lazy val teamsRel = Team.hackathonToTeams.left(this)
  private lazy val problemsRel = Problem.hackathonToProblems.left(this)
  private lazy val sponsorsRel = Sponsor.hackathonsToSponsors.left(this)

  def organiser = organiserRel.head
  def location = locationRel.head
  def teams = teamsRel.toIterable
  def problems = problemsRel.toIterable
  def sponsors = from(sponsorsRel)(hs => select(hs) orderBy (hs.order asc))
}

object HackathonStatus extends Enumeration {
  val Planning = Value(1, "Planning")
  val InProgress = Value(2, "InProgress")
  val Finished = Value(3, "Finished")
}

object Hackathon extends Schema {
  protected[model] val hackathons = table[Hackathon]("hackathons")

  protected[model] val organiserToHackathons = oneToManyRelation(User.users, hackathons).via((u, h) => u.id === h.organiserId)
  protected[model] val locationToHackathons = oneToManyRelation(Location.locations, hackathons).via((l, h) => l.id === h.locationId)

  def all(): Iterable[Hackathon] = {
    hackathons.toIterable
  }

  def lookup(id: Long): Option[Hackathon] = {
    hackathons.lookup(id)
  }

  def insert(hackathon: Hackathon): Hackathon = {
    hackathons.insert(hackathon)
  }

  def update(id: Long, hackathon: Hackathon): Int = {
    hackathons.update(h =>
      where(h.id === id)
        set (
          h.subject := hackathon.subject,
          h.status := hackathon.status,
          h.organiserId := hackathon.organiserId,
          h.locationId := hackathon.locationId))
  }

  def delete(id: Long): Int = {
    hackathons.deleteWhere(h => h.id === id)
  }
}
