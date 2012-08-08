package model

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.KeyedEntity
import org.squeryl.Schema
import org.squeryl.annotations.Column

case class Location(id : Long, 
  country: String,
  city: String,
  @Column("postal_code") postalCode: String,
  @Column("full_address") fullAddress: String,
  name: String,
  latitude: Double,
  longitude: Double) extends KeyedEntity[Long] {

  
   def this() = this(0, "", "", "", "","",  0, 0) // need for status enumeration
   def this(id : Long, name : String) = this(0, "", "", "", "", name, 0, 0)

}

object Location extends Schema {
  protected[model] val locations = table[Location]("locations")
  on(locations)(l => declare(l.id is (primaryKey, autoIncremented("location_id_seq"))))

  def all(): Seq[Location] = {
    locations.toSeq
  }

  def findByPattern(pattern : String) = {
       from(locations)((l) => 
         where ((lower(l.name) like lower(pattern)) 
             or (lower(l.country) like lower(pattern))
             or (lower(l.fullAddress) like lower(pattern))
             or (lower(l.city) like lower(pattern))
             )
         select(l)
         orderBy(l.name desc, l.fullAddress, l.country, l.city))
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