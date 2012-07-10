package model

import java.util.Date
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column

case class Sponsor(name: String, 
				 description: String,
				 website: String,
				 @Column("sponsor_order") order: Int,
				 @Column("is_general_sponsor") isGeneralSponsor: Boolean) extends KeyedEntity[Long] {
  val id: Long = 0L
  lazy val hackathons = Sponsors.hackathonsToSponsors.right(this)
}

case class HackathonSponsor(@Column("hackathon_id") hackathonId: Long,
							@Column("sponsor_id") sponsorId: Long,
							@Column("sponsor_order") order: Int) extends KeyedEntity[CompositeKey2[Long,Long]] {
  def id = compositeKey(hackathonId, sponsorId)
}

object Sponsors extends Schema {
  
  val sponsors = table[Sponsor]("sponsors")
      
  val hackathonsToSponsors = 
    manyToManyRelation(Model.hackathons, sponsors, "hackathons_sponsors").
    via[HackathonSponsor](f = (h, s, hs) => (h.id === hs.hackathonId, s.id === hs.sponsorId))
    
  def lookup(id: Long) : Option[Sponsor] = {
    sponsors.lookup(id)
  }
    
  def all() : Iterable[Sponsor] = {
    sponsors.toIterable
  }
    
  def allGeneralSponsorsOrdered() : Iterable[Sponsor] = {
    from(sponsors)(s =>
      where(s.isGeneralSponsor === true)
      select(s)
      orderBy(s.order)
      )
  }
}