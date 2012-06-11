package model
import java.util.Date
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity

case class News(title: String, text: String, author: String, published: Date) extends KeyedEntity[Long] {
  val id: Long = 0L
}

object Hackathon extends Schema {
  val news = table[News]
}