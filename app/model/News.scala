package model

import java.util.Date
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.{ Column, Transient }
import scala.annotation.target.field

case class News(title: String,
  text: String,
  @(Transient @field) labelsAsString: String,
  @Column("author_id") authorId: Long,
  @Column("published_date") publishedDate: Date,
  @Column("hackathon_id") hackathonId: Option[Long]) extends KeyedEntity[Long] {
  val id: Long = 0L

  protected[model] lazy val authorRel: ManyToOne[User] = News.authorToNews.right(this)
  protected[model] lazy val hackathonRel: ManyToOne[Hackathon] = News.hackathonToNews.right(this)
  protected[model] lazy val labelsRel = News.newsToLabels.left(this)

  def author = authorRel.head
  def hackathon = hackathonRel.headOption
  def labels = labelsRel.toSeq.sortBy(l => l.value)

  def addLabel(label: Label) = {
    labelsRel.associate(label)
  }

  def removeLabel(label: Label) = {
    labelsRel.dissociate(label)
  }
}

case class NewsLabel(@Column("news_id") newsId: Long,
  @Column("label_id") labelId: Long) extends KeyedEntity[CompositeKey2[Long, Long]] {
  def id = compositeKey(newsId, labelId)
}

object News extends Schema {
  protected[model] val news = table[News]("news")

  protected[model] val authorToNews = oneToManyRelation(User.users, News.news).via((u, n) => u.id === n.authorId)
  protected[model] val hackathonToNews = oneToManyRelation(Hackathon.hackathons, News.news).via((h, n) => h.id === n.hackathonId)

  protected[model] val newsToLabels =
    manyToManyRelation(News.news, Label.labels, "news_labels").
      via[NewsLabel](f = (n, l, nl) => (n.id === nl.newsId, l.id === nl.labelId))

  def all(): Iterable[News] = {
    from(news)(n =>
      select(n)
        orderBy (n.publishedDate desc))
  }

  def all(hackathonId: Long): Iterable[News] = {
    from(news)(n =>
      where(n.hackathonId === hackathonId)
        select (n)
        orderBy (n.publishedDate desc))
  }

  def lookup(id: Long): Option[News] = {
    news.lookup(id)
  }

  def insert(newsToBeInserted: News) = {
    news.insert(newsToBeInserted)
  }

  def update(id: Long, newsToBeUpdate: News) = {
    news.update(n =>
      where(n.id === id)
        set (
          n.title := newsToBeUpdate.title,
          n.text := newsToBeUpdate.text,
          n.authorId := newsToBeUpdate.authorId,
          n.publishedDate := newsToBeUpdate.publishedDate))
  }

  def delete(id: Long) = {
    news.deleteWhere(n => n.id === id)
  }
}