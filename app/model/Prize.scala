package model

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.ManyToOne
import org.squeryl.KeyedEntity
import org.squeryl.Schema
import org.squeryl.annotations.Column

case class Prize(name: String,
  description: String,
  @Column("prize_order") order: Int,
  @Column("founder_name") founderName: Option[String],
  @Column("founder_web_page") founderWebPage: Option[String],
  @Column("hackathon_id") hackathonId: Long) extends KeyedEntity[Long] {
  val id: Long = 0L

  private lazy val hackathonRel: ManyToOne[Hackathon] = Prize.hackathonToPrizes.right(this)

  def hackathon = hackathonRel.head
}

object Prize extends Schema {
  protected[model] val prizes = table[Prize]("prizes")
  on(prizes)(p => declare(p.id is (primaryKey, autoIncremented("prize_id_seq"))))

  protected[model] val hackathonToPrizes = oneToManyRelation(Hackathon.hackathons, Prize.prizes).via((h, p) => h.id === p.hackathonId)

  def all(): Seq[Prize] = {
    from(prizes)(p =>
      select(p)
        orderBy (p.order asc)).toSeq
  }

  def lookup(id: Long): Option[Prize] = {
    prizes.lookup(id)
  }

  def insert(prize: Prize) = {
    prizes.insert(prize)
  }

  def update(id: Long, prize: Prize) = {
    prizes.update(p =>
      where(p.id === id)
        set (
          p.name := prize.name,
          p.description := prize.description,
          p.order := prize.order,
          p.hackathonId := prize.hackathonId,
          p.founderName := prize.founderName,
          p.founderWebPage := prize.founderWebPage))
  }

  def delete(id: Long) = {
    prizes.deleteWhere(p => p.id === id)
  }

}  