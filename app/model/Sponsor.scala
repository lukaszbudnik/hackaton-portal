package model

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.CompositeKey2
import org.squeryl.KeyedEntity
import org.squeryl.Schema
import org.squeryl.annotations.Column
import org.squeryl.annotations.Transient
import scala.annotation.target.field

case class Sponsor(name: String,
  description: String,
  website: String,
  @Column("sponsor_order") order: Int,
  @Column("is_general_sponsor") isGeneralSponsor: Boolean,
  @(Transient @field) hackathonsList: List[HackathonSponsorHelper]) extends KeyedEntity[Long] {
  val id: Long = 0L

  lazy val hackathons = Sponsor.hackathonsToSponsors.right(this)

  // TODO 
  //def hackathons = hackathonsRel.toIterable
}

case class HackathonSponsor(@Column("hackathon_id") hackathonId: Long,
  @Column("sponsor_id") sponsorId: Long,
  @Column("sponsor_order") order: Int) extends KeyedEntity[CompositeKey2[Long, Long]] {
  def id = compositeKey(hackathonId, sponsorId)
}

case class HackathonSponsorHelper(hackathonId: Long, order: Int)

object Sponsor extends Schema {
  protected[model] val sponsors = table[Sponsor]("sponsors")
  on(sponsors)(s => declare(s.id is (primaryKey, autoIncremented("sponsor_id_seq"))))

  val hackathonsToSponsors =
    manyToManyRelation(Hackathon.hackathons, Sponsor.sponsors, "hackathons_sponsors").
      via[HackathonSponsor](f = (h, s, hs) => (h.id === hs.hackathonId, s.id === hs.sponsorId))

  def all(): Iterable[Sponsor] = {
    sponsors.toIterable
  }

  def lookup(id: Long): Option[Sponsor] = {
    sponsors.lookup(id)
  }

  def allGeneralSponsorsOrdered(): Iterable[Sponsor] = {
    from(sponsors)(s =>
      where(s.isGeneralSponsor === true)
        select (s)
        orderBy (s.order))
  }

  def findSponsorHackatonOrder(sponsorId: Long, hackathonId: Long): Option[Int] = {
    from(hackathonsToSponsors)(hs =>
      where(hs.sponsorId === sponsorId and hs.hackathonId === hackathonId)
        select (hs.order)).headOption

  }

  def deleteSponsorHackathon(sponsorId: Long, hackathonId: Long) = {
    hackathonsToSponsors.deleteWhere(hs =>
      hs.sponsorId === sponsorId and hs.hackathonId === hackathonId)
  }

  def updateSponsorHackathonOrder(sponsorId: Long, hackathonId: Long, order: Int) = {
    hackathonsToSponsors.deleteWhere(hs =>
      hs.sponsorId === sponsorId and hs.hackathonId === hackathonId)
    hackathonsToSponsors.insert(model.HackathonSponsor(hackathonId, sponsorId, order))
  }

  def insert(sponsor: Sponsor): Sponsor = {
    sponsors.insert(sponsor)
  }

  def update(id: Long, sponsor: Sponsor): Int = {
    sponsors.update(s =>
      where(s.id === id)
        set (
          s.name := sponsor.name,
          s.description := sponsor.description,
          s.order := sponsor.order,
          s.website := sponsor.website,
          s.isGeneralSponsor := sponsor.isGeneralSponsor))
  }

  def delete(id: Long): Int = {
    sponsors.deleteWhere(s => s.id === id)
  }
}