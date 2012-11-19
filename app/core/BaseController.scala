package core
import play.api.mvc.Controller
import play.api.mvc.AnyContent

class BaseController extends Controller with securesocial.core.SecureSocial {

  def userFromRequest(implicit request: RequestWithUser[AnyContent]) = {
    request.user.map { user =>
      model.User.lookupByOpenId(user.id.id + user.id.providerId)
    }.flatMap(u => u)
  }

  def userFromRequest(implicit request: SecuredRequest[AnyContent]) = {
    val user = request.user
    model.User.lookupByOpenId(user.id.id + user.id.providerId).first
  }

}