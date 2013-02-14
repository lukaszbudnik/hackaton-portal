package security

import org.squeryl.PrimitiveTypeMode.transaction

import model.User
import play.api.Application
import play.api.Logger
import securesocial.core.Identity
import securesocial.core.UserId
import securesocial.core.UserServicePlugin
import securesocial.core.providers.Token

class UserService(application: Application) extends UserServicePlugin(application) {

  private var users = Map[String, Identity]()

  def find(id: UserId) = {
    Logger.info("trying to load " + id)
    users.get(id.id + id.providerId)
  }

  def save(socialUser: Identity): Identity = {

    val alreadyLoggedInSocialUser = find(socialUser.id)

    if (alreadyLoggedInSocialUser.isEmpty) {
      // user not logged in, see if user is already stored in our database
      transaction {
        model.User.lookupByOpenId(socialUser.id.id + socialUser.id.providerId).getOrElse(addNewUserToDatabase(socialUser))
      }
    }

    // refresh users map
    users = users + (socialUser.id.id + socialUser.id.providerId -> socialUser)
    socialUser
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    // empty
    None
  }

  def save(token: Token) = {
    // empty
  }

  def findToken(token: String): Option[Token] = {
    // empty
    None
  }

  def deleteToken(uuid: String) {
    // empty
  }

  def deleteExpiredTokens() {
    // empty
  }

  private def addNewUserToDatabase(socialUser: Identity): User = {
    if (Logger.isDebugEnabled) {
      Logger.debug("Adding a new user for social user " + socialUser)
    }

    val openId = socialUser.id.id + socialUser.id.providerId;
    val newUser = User(socialUser.fullName, socialUser.email.getOrElse(""), controllers.LangAwareController.DEFAULT_LANG, "", "", socialUser.avatarUrl.getOrElse(""), openId)
    model.User.insert(newUser)
  }

}
