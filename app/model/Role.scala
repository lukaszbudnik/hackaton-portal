package model

import org.squeryl.PrimitiveTypeMode.view2QueryAll
import org.squeryl.KeyedEntity
import org.squeryl.Schema

case class Role(name: String) extends KeyedEntity[Long] {
  val id: Long = 0L
}

object Role extends Schema {
  protected[model] val roles = table[Role]("roles")

  def all(): Iterable[Role] = {
    roles.toIterable
  }

  def lookupByName(name: String): Option[Role] = {
    roles.find(r => r.name == name)
  }
}