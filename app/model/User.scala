package model

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.CompositeKey2
import org.squeryl.KeyedEntity
import org.squeryl.Schema
import org.squeryl.annotations.Column
import org.squeryl.dsl.ast.ExpressionNode

case class User(name: String,
  email: String,
  language: String,
  @Column("github_username") githubUsername: String,
  @Column("twitter_account") twitterAccount: String,
  @Column("avatar_url") avatarUrl: String,
  @Column("open_id") openId: String,
  @Column("is_admin") isAdmin: Boolean = false,
  @Column("is_blocked") isBlocked: Boolean = false) extends KeyedEntity[Long] {
  val id: Long = 0L

  private lazy val hackathonsRel = Hackathon.hackathonsToUsers.right(this)
  private lazy val teamsRel = Team.teamsToUsers.right(this)
  
  def hackathons = hackathonsRel.toIterable
  def teams =    teamsRel.toIterable
}

object User extends Schema {
  protected[model] val users = table[User]("users")
  on(users)(t => declare(t.id is (primaryKey, autoIncremented("user_id_seq"))))

  def all(): Iterable[User] = {
    users.toIterable
  }
  
  def pagedUsers(orderBy: Int, filter: String, offset: Int, pageSize: Int): Iterable[User] = {
    from(users)(u =>
	    where(lower(u.name) like "%" + filter.toLowerCase() + "%")
		select(u)
		orderBy(getOrderByValue(u, orderBy))
	).page(offset, pageSize)
  }
  
  def pagedUsersTotalNumber(filter: String): Int = {
    from(users)(u =>
	    where(lower(u.name) like "%" + filter.toLowerCase() + "%")
		compute(count)
	).toInt
  }
  
  private def getOrderByValue(u: User, orderBy: Int): ExpressionNode = {
	  orderBy match {
	    case 1 => u.name asc
	    case -1 => u.name desc
	    case 2 => u.email asc
	    case -2 => u.email desc
	    case 3 => u.githubUsername asc
	    case -3 => u.githubUsername desc
	    case 4 => u.twitterAccount asc
	    case -4 => u.twitterAccount desc
	    // isAdmin is reversed 
	    case 5 => u.isAdmin desc
	    case -5 => u.isAdmin asc
	  }
  }

  def lookup(id: Long): Option[User] = {
    users.lookup(id)
  }

  def lookupByOpenId(openId: String): Option[User] = {
    users.find(u => u.openId == openId)
  }

  def insert(user: User): User = {
    users.insert(user)
  }
  
  def update(id: Long, userToBeUpdated: User): Int = {
     users.update(u =>
      where(u.id === id)
        set (
          u.avatarUrl := userToBeUpdated.avatarUrl,
          u.email := userToBeUpdated.email,
          u.language := userToBeUpdated.language,
          u.githubUsername := userToBeUpdated.githubUsername,
          u.isAdmin := userToBeUpdated.isAdmin,
          u.isBlocked := userToBeUpdated.isBlocked,
          u.openId := userToBeUpdated.openId,
          u.twitterAccount := userToBeUpdated.twitterAccount))
  }

  def delete(id: Long): Int = {
    users.deleteWhere(u => u.id === id)
  }
}