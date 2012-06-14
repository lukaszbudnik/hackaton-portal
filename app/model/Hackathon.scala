package model
import java.util.Date
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity

case class News(title: String, text: String, author_id: Long, published: Date) extends KeyedEntity[Long] {
  val id: Long = 0L
}

case class User(name: String, email: String, github_username: String, open_id: String) extends KeyedEntity[Long] {
  val id: Long = 0L
}

object Hackathon extends Schema {
  val news = table[News]
  val users = table[User]("Users")
}
