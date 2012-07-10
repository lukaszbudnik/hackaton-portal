package model

import java.util.Date
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.{Column, Transient}
import scala.annotation.target.field

case class News(title: String, text: String, @(Transient @field) labelsAsString: String, @Column("author_id") authorId: Long, published: Date, @Column("hackathon_id") hackathonId: Option[Long]) extends KeyedEntity[Long] {
  val id: Long = 0L
  protected[model] lazy val authorRel: ManyToOne[User] = News.authorToNews.right(this)
  protected[model] lazy val labelsRel = News.newsToLabels.left(this)
  
  def author = authorRel.head
  
  def labels = labelsRel.toSeq.sortBy(l => l.value)
  
  def addLabel(label: Label) = {
    labelsRel.associate(label)
  }
  
  def removeLabel(label: Label) = {
    labelsRel.dissociate(label)
  }
  
}

object News extends Schema {

  protected[model] val news = table[News]
  val authorToNews = oneToManyRelation(Model.users, News.news).via((u, n) => u.id === n.authorId)
  val newsToLabels =
    manyToManyRelation(news, Label.labels, "news_labels").
      via[NewsLabel](f = (n, l, nl) => (n.id === nl.newsId, l.id === nl.labelId))
  
  def insert(newsToBeInserted: News) = {
    news.insert(newsToBeInserted)
  }
  
  def update(id:Long, newsToBeUpdate: News) = {
    news.update(n =>
      where(n.id === id)
        set (
          n.title := newsToBeUpdate.title,
          n.text := newsToBeUpdate.text,
          n.authorId := newsToBeUpdate.authorId,
          n.published := newsToBeUpdate.published))
  }
  
  def delete(id:Long) = {
    news.deleteWhere(n => n.id === id)
  }
  
  def lookup(id: Long): Option[News] = {
    news.lookup(id)
  }

  def all(): Iterable[News] = {
    news.toIterable
  }
  
  def all(hackathonId: Long): Iterable[News] = {
    news.where(n => n.hackathonId === hackathonId)
  }

  def allNewsSortedByDateDesc(): Iterable[News] = {
	from (news)(n =>
        select(n)
        orderBy(n.published desc)
    )
  }
}