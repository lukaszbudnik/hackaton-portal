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

  protected[model] lazy val submitterRel: ManyToOne[User] = Hackathon.submitterToHackathons.right(this)
  protected[model] lazy val locationRel: ManyToOne[Location] = Hackathon.locationToHackathons.right(this)
  protected[model] lazy val teamsRel = Team.hackathonToTeams.left(this)
  protected[model] lazy val problemsRel = Problem.hackathonToProblems.left(this)
  protected[model] lazy val sponsorsRel = Sponsors.hackathonsToSponsors.left(this)

  def location = locationRel.head
  def sponsors = from(sponsorsRel)(hs => select(hs) orderBy (hs.order asc))
  def this() = this("", HackathonStatus.Planning, 1, 1)
}

object HackathonStatus extends Enumeration {
  val Planning = Value(1, "Planning")
  val InProgress = Value(2, "InProgress")
  val Finished = Value(3, "Finished")
}

object Hackathon extends Schema {
  protected[model] val hackathons = table[Hackathon]("hackathons")

  protected[model] val locationToHackathons = oneToManyRelation(Location.locations, hackathons).via((l, h) => l.id === h.locationId)
  protected[model] val submitterToHackathons = oneToManyRelation(User.users, hackathons).via((u, h) => u.id === h.submitterId)

  def all(): Iterable[Hackathon] = {
    hackathons.toIterable
  }

  def allHackathonsForSponsor(id: Long) = {
    model.Sponsors.lookup(id).get.hackathons
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
          h.submitterId := hackathon.submitterId,
          h.locationId := hackathon.locationId))
  }

  def delete(id: Long): Int = {
    hackathons.deleteWhere(h => h.id === id)
  }
}
