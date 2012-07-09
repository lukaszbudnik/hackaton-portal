package model

import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column

case class NewsLabel(@Column("news_id") newsId: Long,
					@Column("label_id") labelId: Long) extends KeyedEntity[CompositeKey2[Long,Long]] {
  def id = compositeKey(newsId, labelId)
}