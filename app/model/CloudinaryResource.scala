package model

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.KeyedEntity
import org.squeryl.Schema
import org.squeryl.annotations.Column

case class CloudinaryResource(url : String, publicId: String) extends KeyedEntity[Long] {
  val id: Long = 0L
}

object CloudinaryResource extends Schema {
  val resources = table[CloudinaryResource]("cloudinary_resources")
  on(resources)(r => declare(r.id is (primaryKey, autoIncremented("cloudinary_resource_id_seq"))))

  def all(): Iterable[CloudinaryResource] = {
    resources.toIterable
  }

  def lookup(id: Long): Option[CloudinaryResource] = {
    resources.lookup(id)
  }

  def insert(r: CloudinaryResource): CloudinaryResource = {
    resources.insert(r)
  }

  def update(id: Long, res: CloudinaryResource): Int = {
    resources.update(r =>
      where(r.id === id)
        set (
          r.url := res.url,
          r.publicId := res.publicId))
  }

  def delete(id: Long): Int = {
    resources.deleteWhere(r => r.id === id)
  }
}