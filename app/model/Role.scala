package model

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.KeyedEntity
import org.squeryl.Schema

case class Role(name: String) extends KeyedEntity[Long] {
  val id: Long = 0L
}

object Role extends Schema {
  protected[model] val roles = table[Role]("roles")
  on(roles)(r => declare(r.id is (primaryKey, autoIncremented("role_id_seq"))))
  
  def all(): Iterable[Role] = {
    roles.toIterable
  }

  def lookupByName(name: String): Option[Role] = {
    roles.find(r => r.name == name)
  }
}