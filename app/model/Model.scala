package model
import java.util.Date
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column

case class News(title: String, text: String, labels: String, @Column("author_id") authorId: Long, published: Date) extends KeyedEntity[Long] {
  val id: Long = 0L
}

case class User(name: String, email: String, @Column("github_username") githubUsername: String, @Column("open_id") openId: String) extends KeyedEntity[Long] {
  val id: Long = 0L
}

case class Problem(name: String, description: String, @Column("submitter_id") submitterId: Long, @Column("hackathon_id") hackathonId: Long) extends KeyedEntity[Long] {
  val id: Long = 0L
}

case class Hackathon(subject: String, status: String, @Column("submitter_id") submitterId: Long, @Column("location_id") locationId: Long) extends KeyedEntity[Long] {
  val id: Long = 0L
}

object Model extends Schema {
  val news = table[News]
  val problems = table[Problem]("Problems")
  val users = table[User]("Users")
  val hackathons = table[Hackathon]("Hackathons")

  def lookupNews(id: Long): Option[News] = {
    news.lookup(id)
  }

  def allNews(): Iterable[News] = {
    news.toList
  }

  def deleteAllNews() = {
    news.deleteWhere(n => n.id gt 0L)
  }
  
  def lookupProblem(id: Long): Option[Problem] = {
    problems.lookup(id)
  }
  
  def allProblems(): Iterable[Problem] = {
    problems.toList
  }
  
  def deleteAllProblems() = {
    problems.deleteWhere(p => p.id gt 0L)
  }
}
