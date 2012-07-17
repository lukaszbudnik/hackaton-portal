package model

import java.util.Date
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.CompositeKey2
import org.squeryl.dsl.ManyToOne
import org.squeryl.KeyedEntity
import org.squeryl.Schema
import org.squeryl.annotations.Column
import org.squeryl.annotations.Transient
import scala.annotation.target.field

case class News(title: String,
  text: String,
  @(Transient @field) labelsAsString: String,
  @Column("author_id") authorId: Long,
  @Column("published_date") publishedDate: Date,
  @Column("hackathon_id") hackathonId: Option[Long]) extends KeyedEntity[Long] {
  val id: Long = 0L

  private lazy val authorRel: ManyToOne[User] = News.authorToNews.right(this)
  private lazy val hackathonRel: ManyToOne[Hackathon] = News.hackathonToNews.right(this)
  private lazy val labelsRel = News.newsToLabels.left(this)

  def author = authorRel.head
  def hackathon = hackathonRel.headOption
  def labels = labelsRel.toSeq.sortBy(l => l.value)

  def addLabel(label: Label) = {
    labelsRel.associate(label)
  }

  def removeLabel(label: Label) = {
    labelsRel.dissociate(label)
  }

  def storeLabels(newLabelsAsString: String): News = {
    val newLabels = newLabelsAsString.split(",").filter(s => !s.isEmpty()).map(_.trim().toLowerCase()).distinct.toSeq
    val existingLabels = labels.map(_.value).toSeq

    // remove old labels
    val labelsToBeRemoved = existingLabels.diff(newLabels).map(v => labels.find(l => l.value == v).get)
    labelsToBeRemoved.map { label =>
      removeLabel(label)
    }

    // add new labels
    val labelsToBeAdded = newLabels.diff(existingLabels).map(v => model.Label.lookupByValue(v).getOrElse(model.Label.insert(Label(v))))
    labelsToBeAdded.map { label =>
      addLabel(label)
    }

    this
  }
}

case class NewsLabel(@Column("news_id") newsId: Long,
  @Column("label_id") labelId: Long) extends KeyedEntity[CompositeKey2[Long, Long]] {
  def id = compositeKey(newsId, labelId)
}

object NewsLabel extends Schema {
  protected[model] val newsLabels = table[NewsLabel]("news_labels")
}

object News extends Schema {
  protected[model] val news = table[News]("news")
  on(news)(n => declare(n.id is (primaryKey, autoIncremented("news_id_seq"))))

  protected[model] val authorToNews = oneToManyRelation(User.users, News.news).via((u, n) => u.id === n.authorId)
  protected[model] val hackathonToNews = oneToManyRelation(Hackathon.hackathons, News.news).via((h, n) => h.id === n.hackathonId)

  protected[model] val newsToLabels =
    manyToManyRelation(News.news, Label.labels, "news_labels").
      via[NewsLabel](f = (n, l, nl) => (n.id === nl.newsId, l.id === nl.labelId))

  def all(): Seq[News] = {
    from(news)(n =>
      select(n)
        orderBy (n.publishedDate desc)).toSeq
  }

  def lookup(id: Long): Option[News] = {
    news.lookup(id)
  }

  def insert(newsToBeInserted: News): News = {
    news.insert(newsToBeInserted).storeLabels(newsToBeInserted.labelsAsString)
  }

  def update(id: Long, newsToBeUpdate: News): Int = {
    news.lookup(id).map { n =>
      n.storeLabels(newsToBeUpdate.labelsAsString)
    }

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
  
  def findByLabel(label: String) = {
    from(news, Label.labels, NewsLabel.newsLabels)((n, l, nl) =>
        where(l.value === label and l.id === nl.labelId and n.id === nl.newsId)
        select(n)
        orderBy (n.publishedDate desc)
      )
  }
}