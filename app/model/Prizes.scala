package model

import java.util.Date
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column

case class Prize(name: String, 
				 description: String, 
				 @Column("prize_order") order: Int,
				 @Column("founder_name") founderName: Option[String],
				 @Column("founder_web_page") founderWebPage: Option[String],
				 @Column("hackathon_id") hackathonId: Long) extends KeyedEntity[Long] {
  val id: Long = 0L
}

object Prizes extends Schema {
  
  val prizes = table[Prize]("prizes")

  def all(): Iterable[Prize] = {
    prizes.toIterable		  
  }
  
  def allOrdered(): Iterable[Prize] = {
	from (prizes)(p =>
	  select(p)
	  orderBy(p.order asc)
    )
  }
    
  def lookup(id: Long): Option[Prize] = {
    prizes.lookup(id)
  }
}  