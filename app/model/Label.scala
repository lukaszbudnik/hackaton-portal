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

  protected[model] val labels = table[Label]("labels")
  
  def insert(label: Label) = {
    labels.insert(label)
  }

  def findByValue(value: String): Option[Label] = {
    labels.find(l => l.value == value)
  }

}