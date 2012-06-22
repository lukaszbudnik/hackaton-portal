package model
import java.util.Date
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column

case class News(title: String, text: String, labels: String, @Column("author_id") authorId: Long, published: Date) extends KeyedEntity[Long] {
  val id: Long = 0L
  lazy val author: ManyToOne[User] = Model.authorToNews.right(this)
}

case class User(name: String, email: String, @Column("github_username") githubUsername: String, @Column("open_id") openId: String) extends KeyedEntity[Long] {
  val id: Long = 0L
  lazy val news: OneToMany[News] = Model.authorToNews.left(this)
}

case class Problem(name: String, description: String, @Column("submitter_id") submitterId: Long, @Column("hackathon_id") hackathonId: Long) extends KeyedEntity[Long] {
  val id: Long = 0L
}

case class Hackathon(subject: String, status: String, @Column("submitter_id") submitterId: Long, @Column("location_id") locationId: Long) extends KeyedEntity[Long] {
  val id: Long = 0L
  lazy val location: ManyToOne[Location] = Model.locationToHackathons.right(this)
}

case class Location(country: String,
                    city: String,
                    @Column("postal_code") postalCode: String,
                    @Column("full_address") fullAddress: String,
                    name: String,
                    latitude: Double,
                    longitude: Double) extends KeyedEntity[Long] {
  val id: Long = 0L
}

object OrderByDirection extends Enumeration {
  type Direction = Value
  val Asc = Value("Asc")
  val Desc = Value("Desc")
}

object Model extends Schema {
  val news = table[News]
  val problems = table[Problem]("problems")
  val users = table[User]("users")
  val hackathons = table[Hackathon]("hackathons")
  val locations = table[Location]("locations")
  
  val locationToHackathons = oneToManyRelation(locations, hackathons).via((l, h) => l.id === h.locationId)
  val authorToNews = oneToManyRelation(users, news).via((u, n) => u.id === n.authorId)

  def lookupNews(id: Long): Option[News] = {
    news.lookup(id)
  }

  def allNews(): Iterable[News] = {
    news.toList
  }

  def deleteAllNews() = {
    news.deleteWhere(n => n.id gt 0L)
  }
  
  def allNewsSortedByDateDesc(): Iterable[News] = {
	from (Model.news)(n =>
        select(n)
        orderBy(n.published desc)
    )
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
  
  def lookupHackathon(id: Long): Option[Hackathon] = {
    hackathons.lookup(id)
  }
}
