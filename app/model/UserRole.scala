package model

import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column


case class UserRole(@Column("user_id") userId: Long,
					@Column("role_id") roleId: Long) extends KeyedEntity[CompositeKey2[Long,Long]] {
  def id = compositeKey(userId, roleId)
}