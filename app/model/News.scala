package model

import java.util.Date
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column

case class News(title: String, text: String, labels: String, @Column("author_id") authorId: Long, published: Date, @Column("hackathon_id") hackathonId: Option[Long]) extends KeyedEntity[Long] {
  val id: Long = 0L
  lazy val author: ManyToOne[User] = News.authorToNews.right(this)
}

object News extends Schema {
  
  val news = table[News]
  val authorToNews = oneToManyRelation(Model.users, News.news).via((u, n) => u.id === n.authorId)
  
  def lookup(id: Long): Option[News] = {
    news.lookup(id)
  }

  def all(): Iterable[News] = {
    news.toIterable
  }
  
  def all(hackathonId: Long): Iterable[News] = {
    news.where(n => n.hackathonId === hackathonId)
  }

  def deleteAll() = {
    news.deleteWhere(n => n.id gt 0L)
  }
  
  def allNewsSortedByDateDesc(): Iterable[News] = {
	from (news)(n =>
        select(n)
        orderBy(n.published desc)
    )
  }
}