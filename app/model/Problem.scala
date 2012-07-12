package model

import org.squeryl.dsl._
import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.annotations.Column

case class Problem(name: String,
  description: String,
  @Column("submitter_id") submitterId: Long,
  @Column("hackathon_id") hackathonId: Long) extends KeyedEntity[Long] {
  val id: Long = 0L

  private lazy val submitterRel: ManyToOne[User] = Problem.submitterToProblems.right(this)
  private lazy val hackathonRel: ManyToOne[Hackathon] = Problem.hackathonToProblems.right(this)

  def submitter = submitterRel.head
  def hackathon = hackathonRel.head
}

object Problem extends Schema {
  protected[model] val problems = table[Problem]("problems")

  protected[model] val submitterToProblems = oneToManyRelation(User.users, Problem.problems).via((u, p) => u.id === p.submitterId)
  protected[model] val hackathonToProblems = oneToManyRelation(Hackathon.hackathons, Problem.problems).via((h, p) => h.id === p.hackathonId)

  def all(): Iterable[Problem] = {
    problems.toIterable
  }

  def lookup(id: Long): Option[Problem] = {
    problems.lookup(id)
  }

  def insert(problem: Problem): Problem = {
    problems.insert(problem)
  }

  def update(id: Long, problem: Problem): Int = {
    problems.update(p =>
      where(p.id === id)
        set (
          p.name := problem.name,
          p.description := problem.description,
          p.submitterId := problem.submitterId,
          p.hackathonId := problem.hackathonId))
  }

  def delete(id: Long): Int = {
    problems.deleteWhere(p => p.id === id)
  }
}