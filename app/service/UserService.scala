package service

import play.api.{ Logger, Application }
import org.squeryl.PrimitiveTypeMode.{ transaction, inTransaction }
import securesocial.core.{ UserServicePlugin, UserId, SocialUser }
import model.User

class UserService(application: Application) extends UserServicePlugin(application) {

  private var users = Map[String, SocialUser]()

  def find(id: UserId) = {
    Logger.info("trying to load " + id)
    users.get(id.id + id.providerId)
  }

  def save(socialUser: SocialUser) = {

    val alreadyLoggedInSocialUser = find(socialUser.id)

    if (alreadyLoggedInSocialUser.isEmpty) {
      // user not logged in, see if user is already stored in our database
      transaction {
        model.User.lookupByOpenId(socialUser.id.id + socialUser.id.providerId).getOrElse(addNewUserToDatabase(socialUser))
      }
    }

    // refresh users map
    users = users + (socialUser.id.id + socialUser.id.providerId -> socialUser)
  }

  private def addNewUserToDatabase(socialUser: SocialUser): User = {
    if (Logger.isDebugEnabled) {
      Logger.debug("Adding a new user for social user " + socialUser)
    }

    val openId = socialUser.id.id + socialUser.id.providerId;
    val newUser = User(socialUser.displayName, socialUser.email.getOrElse(""), controllers.LangAwareController.DEFAULT_LANG, "", "", socialUser.avatarUrl.getOrElse(""), openId)
    model.User.insert(newUser)
  }

}
