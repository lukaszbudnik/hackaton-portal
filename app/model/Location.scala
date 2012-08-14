package model

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.KeyedEntity
import org.squeryl.Schema
import org.squeryl.annotations.Column
import org.squeryl.dsl.ast.LogicalBoolean

object LocationStatus extends Enumeration {
  val Unverified = Value(1, "Unverified")
  val Approved = Value(2, "Approved")
  val Suspended = Value(3, "Suspended")
  val Blocked = Value(4, "Blocked")
}

case class Location(id : Long, 
  country: String,
  city: String,
  @Column("postal_code") postalCode: String,
  @Column("full_address") fullAddress: String,
  name: String,
  latitude: Double,
  longitude: Double,
  @Column("submitter_id") submitterId : Long,
  status : LocationStatus.Value = LocationStatus.Unverified) extends KeyedEntity[Long] {

  
   def this() = this(id = 0
       , country = ""
       , city = ""
       , postalCode = ""
       , fullAddress = ""
       , name = ""
       , latitude = 0
       , longitude =  0
       , submitterId =  0
       , status = LocationStatus.Unverified) // need for status enumeration

      

}

object Location extends Schema {
  protected[model] val locations = table[Location]("locations")
  on(locations)(l => declare(l.id is (primaryKey, autoIncremented("location_id_seq"))))

  def all(): Seq[Location] = {
    locations.toSeq
  }

  def findByPattern(pattern : String, cond: (model.Location) => LogicalBoolean) = {
       from(locations)((l) => 
         where (((lower(l.name) like lower(pattern)) 
             or (lower(l.country) like lower(pattern))
             or (lower(l.fullAddress) like lower(pattern))
             or (lower(l.city) like lower(pattern))
             ) and cond(l))
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
          l.longitude := location.longitude,
          l.status := location.status))
  }

  def delete(id: Long): Int = {
    locations.deleteWhere(l => l.id === id)
  }
}