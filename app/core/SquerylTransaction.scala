package core

import play.api._
import play.api.mvc._
import play.api.i18n._
import model.Model
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column

trait SquerylTransaction {
  def TxAction(f: Request[AnyContent] => Result): Action[AnyContent] = {
    Action { request =>
      transaction {
        f(request)
      }
    }
  }
}