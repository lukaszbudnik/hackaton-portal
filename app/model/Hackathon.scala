package model

import java.text.SimpleDateFormat
import java.util.Date
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Column
import org.squeryl.dsl.ManyToOne
import org.squeryl.KeyedEntity
import org.squeryl.Schema
import play.api.libs.json._
import play.api.i18n.Messages
import scala.annotation.target.field
import org.squeryl.annotations.Transient
import org.squeryl.dsl.CompositeKey2

case class Hackathon(subject: String,
  status: HackathonStatus.Value,
  date: Date,
  @Column("organiser_id") organiserId: Long,
  @Column("location_id") locationId: Long,
  @(Transient @field) var locationName: String) extends KeyedEntity[Long] {
  val id: Long = 0L

  def this() = this("", HackathonStatus.Planning, new Date(), 0, 0, "") // need for status enumeration
  def this(organiserId: Long) = this("", HackathonStatus.Planning, new Date(), organiserId, 0, "")

  private lazy val organiserRel: ManyToOne[User] = Hackathon.organiserToHackathons.right(this)
  private lazy val locationRel: ManyToOne[Location] = Hackathon.locationToHackathons.right(this)
  private lazy val teamsRel = Team.hackathonToTeams.left(this)
  private lazy val problemsRel = Problem.hackathonToProblems.left(this)
  private lazy val prizeRel = Prize.hackathonToPrizes.left(this)
  private lazy val newsRel = News.hackathonToNews.left(this)
  private lazy val sponsorsRel = Sponsor.hackathonToSponsors.left(this)
  private lazy val membersRel = Hackathon.hackathonsToUsers.left(this)

  def organiser = organiserRel.head
  def location = locationRel.head
  def teams = teamsRel.toIterable
  def problems = problemsRel.toIterable
  def prizes = prizeRel.toIterable
  def news = newsRel.toIterable
  def sponsors = from(sponsorsRel)(s => select(s) orderBy (s.order asc)).toSeq
  def members = membersRel.toIterable

  def hasMember(userId: Long): Boolean = {
    membersRel.exists(u => u.id == userId)
  }

  def addMember(user: User): HackathonUser = {
    membersRel.associate(user)
  }

  def addMember(user: User, teamId: Long) = {
    membersRel.associate(user, new HackathonUser(0, 0, Some(teamId)))
  }

  def deleteMember(user: User) = {
    membersRel.dissociate(user)
  }
}

case class HackathonUser(
  @Column("hackathon_id") hackathonId: Long,
  @Column("user_id") userId: Long,
  @Column("team_id") teamId: Option[Long] = None) extends KeyedEntity[CompositeKey2[Long, Long]] {
  def id = compositeKey(hackathonId, userId)
}

object HackathonStatus extends Enumeration {
  val Planning = Value(1, "Planning")
  val InProgress = Value(2, "InProgress")
  val Finished = Value(3, "Finished")
  val Unverified = Value(4, "Unverified")
}

object Hackathon extends Schema {
  protected[model] val hackathons = table[Hackathon]("hackathons")
  on(hackathons)(h => declare(h.id is (primaryKey, autoIncremented("hackathon_id_seq"))))

  protected[model] val organiserToHackathons = oneToManyRelation(User.users, hackathons).via((u, h) => u.id === h.organiserId)
  protected[model] val locationToHackathons = oneToManyRelation(Location.locations, hackathons).via((l, h) => l.id === h.locationId)

  protected[model] val hackathonsToUsers =
    manyToManyRelation(hackathons, User.users, "hackathons_users").
      via[HackathonUser](f = (h, u, hu) => (h.id === hu.hackathonId, u.id === hu.userId))

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
          h.date := hackathon.date,
          h.locationId := hackathon.locationId))
  }

  def delete(id: Long): Int = {
    hackathons.deleteWhere(h => h.id === id)
  }

  implicit object HackathonFormat extends Format[Hackathon] {
    def reads(json: JsValue): Hackathon = new Hackathon()

    def writes(h: Hackathon): JsValue = JsObject(List(
      "id" -> JsNumber(h.id),
      "subject" -> JsString(h.subject),
      "status" -> JsString(h.status.id.toString),
      "date" -> JsString(new SimpleDateFormat(Messages("global.dateformat")).format(h.date)),
      "organiserName" -> JsString(h.organiser.name),
      "location" -> JsObject(List(
        "id" -> JsNumber(h.location.id),
        "city" -> JsString(h.location.city),
        "country" -> JsString(h.location.country),
        "fullAddress" -> JsString(h.location.fullAddress),
        "name" -> JsString(h.location.name),
        "postalCode" -> JsString(h.location.postalCode),
        "latitude" -> JsNumber(h.location.latitude),
        "longitude" -> JsNumber(h.location.longitude)))))
  }
}
