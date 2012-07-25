package model

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.KeyedEntity
import org.squeryl.Schema

import play.api.Play.current
import plugins.cloudimage.CloudImagePlugin
import plugins.use

case class Resource(url: String, publicId: String) extends KeyedEntity[Long] {
  val id: Long = 0L
}

object Resource extends Schema {
  protected[model] val resources = table[Resource]("resources")
  on(resources)(r => declare(r.id is (primaryKey, autoIncremented("resource_id_seq"))))

  def all(): Iterable[Resource] = {
    resources.toIterable
  }

  def lookup(id: Long): Option[Resource] = {
    resources.lookup(id)
  }

  def insert(r: Resource): Resource = {
    resources.insert(r)
  }

  def update(id: Long, res: Resource): Int = {
    resources.update(r =>
      where(r.id === id)
        set (
          r.url := res.url,
          r.publicId := res.publicId))
  }

  def delete(id: Long): Int = {
    resources.lookup(id).map { r =>
      use[CloudImagePlugin].cloudImageService.destroy(r.publicId)
    }
    
    resources.deleteWhere(r => r.id === id)
  }
}