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

    if (alreadyLoggedInSocialUser.isDefined) {
      if (Logger.isDebugEnabled) {
        Logger.debug("Already logged in updating users maps" + socialUser)
      }
      users = users + (socialUser.id.id + socialUser.id.providerId -> socialUser)
    }

    transaction {
      val dbUser: User = model.User.lookupByOpenId(socialUser.id.id + socialUser.id.providerId).getOrElse(addNewUserToDatabase(socialUser))

      val hackathonSocialUser = socialUser.copy(hackathonUserId = dbUser.id, isAdmin = dbUser.isAdmin)

      users = users + (hackathonSocialUser.id.id + hackathonSocialUser.id.providerId -> hackathonSocialUser)
    }
  }

  private def addNewUserToDatabase(socialUser: SocialUser): User = {
    if (Logger.isDebugEnabled) {
      Logger.debug("Adding a new user for social user " + socialUser)
    }

    val newUser = User(socialUser.displayName, socialUser.email.getOrElse(""), core.LangAwareController.DEFAULT_LANG, "", "", socialUser.avatarUrl.getOrElse(""), socialUser.id.id + socialUser.id.providerId)
    model.User.insert(newUser)
  }

}
