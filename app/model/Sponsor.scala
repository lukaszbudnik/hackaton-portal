package model

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.ManyToOne
import org.squeryl.KeyedEntity
import org.squeryl.Schema
import org.squeryl.annotations.Column
import scala.annotation.target.field
import org.squeryl.annotations.Transient
import play.api.Logger

case class Sponsor(name: String,
  title: String,
  description: String,
  website: String,
  @Column("sponsor_order") order: Int,
  @Column("hackathon_id") hackathonId: Option[Long],
  @Column("logo_resource_id") logoResourceId: Option[Long],
  @(Transient @field) var logoUrl : Option[String]
) extends KeyedEntity[Long] {
  val id: Long = 0L

  def this(hackathonId: Option[Long]) = this("", "", "", "", 1, hackathonId, None, None)

  private lazy val hackathonRel: ManyToOne[Hackathon] = Sponsor.hackathonToSponsors.right(this)
  private lazy val logoResourceRel: ManyToOne[Resource] = Sponsor.resourceToSponsors.right(this)

  def hackathon = hackathonRel.headOption
  def logoResource = logoResourceRel.headOption
}

object Sponsor extends Schema {
  protected[model] val sponsors = table[Sponsor]("sponsors")
  on(sponsors)(s => declare(s.id is (primaryKey, autoIncremented("sponsor_id_seq"))))

  protected[model] val hackathonToSponsors = oneToManyRelation(Hackathon.hackathons, Sponsor.sponsors).via((h, s) => h.id === s.hackathonId)
  protected[model] val resourceToSponsors = oneToManyRelation(Resource.resources, Sponsor.sponsors).via((r, s) => r.id === s.logoResourceId)

  def all(): Seq[Sponsor] = {
    join(sponsors, model.Resource.resources.leftOuter)((s,r) =>
      where(s.hackathonId isNull)
        select (s, r.map(_.url))
        orderBy (s.order)
        on (s.logoResourceId === r.map(_.id))).toSeq map {
        	case (a,b) =>
        		a.logoUrl = b
        		a
    		}  
    }

  def lookup(id: Long): Option[Sponsor] = {
    join(sponsors, model.Resource.resources.leftOuter)((s,r) =>
      where((s.id === id) and (s.hackathonId isNull))
        select (s, r.map(_.url))
        orderBy (s.order)
        on (s.logoResourceId === r.map(_.id))).headOption match {
        	case Some(x) =>
        		x match  { 
        		  case (a, x) =>
        		    a.logoUrl = x
        		    Some(a)
        		}
        	case _ => None
    	}  
    }


  def insert(sponsor: Sponsor): Sponsor = {
    sponsors.insert(sponsor)
  }

  def update(id: Long, sponsor: Sponsor): Int = {
    var resourceIdToDelete: Option[Long] = None
    sponsors.lookup(id).map { s =>
      if (s.logoResourceId != sponsor.logoResourceId) {
        resourceIdToDelete = s.logoResourceId
      }
    }

    val result = sponsors.update(s =>
      where(s.id === id)
        set (
          s.name := sponsor.name,
          s.title := sponsor.title,
          s.description := sponsor.description,
          s.website := sponsor.website,
          s.order := sponsor.order,
          s.hackathonId := sponsor.hackathonId,
          s.logoResourceId := sponsor.logoResourceId))

    resourceIdToDelete.map { id =>
      model.Resource.delete(id)
    }

    result
  }

  def delete(id: Long): Int = {
    var resourceIdToDelete: Option[Long] = None
    sponsors.lookup(id).map { s =>
      resourceIdToDelete = s.logoResourceId
    }

    val result = sponsors.deleteWhere(s => s.id === id)

    resourceIdToDelete.map { id =>
      model.Resource.delete(id)
    }

    result
  }
}