package model.dto

import model.Sponsor
import model.Resource
import org.squeryl.dsl.ast.LogicalBoolean
import org.squeryl.PrimitiveTypeMode._

case class SponsorWithLogo(sponsor : Sponsor, logo : Option[Resource]) {
	def this() = this(new Sponsor(), None)
}

object SponsorWithLogo { 

 def lookup(id: Long) : Option[SponsorWithLogo] = {
	sponsorsWithLogo((s,r) => s.id === id).headOption
  }
 
  def hackathonSponsors(hackathonId : Long) : Seq[SponsorWithLogo] = {
    sponsorsWithLogo((s,r) => (s.hackathonId === hackathonId))
  }
  
  def portalSponsors() : Seq[SponsorWithLogo] = {
    sponsorsWithLogo( (s,r) => 1 === 1)
  }
  
  private def sponsorsWithLogo(whereLogic : (Sponsor, Option[Resource]) => LogicalBoolean) : Seq[SponsorWithLogo] = {
    join(model.Sponsor.sponsors, model.Resource.resources.leftOuter)((s,r) =>
      where(whereLogic(s,r))
        select (s, r)
        orderBy (s.order)
        on (s.logoResourceId === r.map(_.id)))
        .toSeq.map { t => new SponsorWithLogo(t._1, t._2) }.toSeq
  }
 
}