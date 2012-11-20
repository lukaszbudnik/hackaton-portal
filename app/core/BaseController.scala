package core

import play.api.mvc.Controller
import play.api.mvc.AnyContent
import org.squeryl.PrimitiveTypeMode.inTransaction

class BaseController extends Controller with securesocial.core.SecureSocial {

  def userFromRequest(implicit request: RequestWithUser[AnyContent]) = {
    inTransaction {
      request.user.map { user =>
        model.User.lookupByOpenId(user.id.id + user.id.providerId)
      }.flatMap(u => u)
    }
  }

  def userFromRequest(implicit request: SecuredRequest[AnyContent]) = {
    inTransaction {
      val user = request.user
      model.User.lookupByOpenId(user.id.id + user.id.providerId).head
    }
  }

}