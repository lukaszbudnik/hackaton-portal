package model

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.KeyedEntity
import org.squeryl.Schema
import org.squeryl.annotations.Column

case class Label(value: String) extends KeyedEntity[Long] {
  val id: Long = 0L
}

object Label extends Schema {
  protected[model] val labels = table[Label]("labels")
  on(labels)(l => declare(l.id is (primaryKey, autoIncremented("label_id_seq"))))

  def insert(label: Label): Label = {
    labels.insert(label)
  }

  def lookupByValue(value: String): Option[Label] = {
    labels.find(l => l.value == value)
  }

}