package model.dto

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.ast.LogicalBoolean
import model.Hackathon
import model.Location

case class HackathonWithLocations(var hackathon: model.Hackathon, var locations: Seq[model.Location]) {
  def this(hackathon: model.Hackathon) = this(hackathon, List[model.Location]())
}

object HackathonWithLocations {

  private def hackathonLocations(whereLogic: (Hackathon, Location) => LogicalBoolean): Iterable[HackathonWithLocations] = {
    from(model.Hackathon.hackathons, model.Location.locations, model.Hackathon.hackathonLocations)((h, l, hl) =>
      where(h.id === hl.hackathonId and l.id === hl.locationId and whereLogic(h, l))
        select (h, l))
      .groupBy(x => x._1)
      .map {
        x =>
          new HackathonWithLocations(x._1, x._2.map { y => y._2 }.toSeq)
      }
  }

  def lookup(id: Long) = {
    hackathonLocations((h, l) => h.id === id).headOption
  }

  def all() = {
    hackathonLocations((h, l) => 1 === 1)
  }

}