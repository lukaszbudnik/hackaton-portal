package model

import org.squeryl.annotations.Column
import org.squeryl.KeyedEntity
import org.squeryl.Schema
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._

case class Location(country: String,
  city: String,
  @Column("postal_code") postalCode: String,
  @Column("full_address") fullAddress: String,
  name: String,
  latitude: Double,
  longitude: Double) extends KeyedEntity[Long] {
  val id: Long = 0L
}

object Location extends Schema {
  protected[model] val locations = table[Location]("locations")

  def all(): Iterable[Location] = {
    locations.toIterable
  }

  def lookup(id: Long): Option[Location] = {
    locations.lookup(id)
  }

  def insert(location: Location): Location = {
    locations.insert(location)
  }

  def update(id: Long, location: Location): Int = {
    locations.update(l =>
      where(l.id === id)
        set (
          l.name := location.name,
          l.country := location.country,
          l.city := location.city,
          l.postalCode := location.postalCode,
          l.fullAddress := location.fullAddress,
          l.latitude := location.latitude,
          l.longitude := location.longitude))
  }

  def delete(id: Long): Int = {
    locations.deleteWhere(l => l.id === id)
  }
}