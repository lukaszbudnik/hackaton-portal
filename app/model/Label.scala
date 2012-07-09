package model

import java.util.Date
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column

case class Label(value: String) extends KeyedEntity[Long] {
  val id: Long = 0L
}

object Label extends Schema {

  val labels = table[Label]("labels")

  def findByValue(value: String): Option[Label] = {
    labels.find(l => l.value == value)
  }

}