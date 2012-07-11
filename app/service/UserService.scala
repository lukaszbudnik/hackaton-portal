package service

import play.api.{ Logger, Application }
import org.squeryl.PrimitiveTypeMode.{ transaction, inTransaction }
import securesocial.core.{ UserServicePlugin, UserId, SocialUser }
import model.Model
import model.User

class UserService(application: Application) extends UserServicePlugin(application) {

  private var users = Map[String, SocialUser]()

  def find(id: UserId) = {
    Logger.info("trying to load " + id)
    users.get(id.id + id.providerId)
  }

  def save(socialUser: SocialUser): SocialUser = {

    val alreadyLoggedInSocialUser = find(socialUser.id)

    if (alreadyLoggedInSocialUser.isDefined) {
      if (Logger.isDebugEnabled) {
        Logger.debug("Already logged in updating users maps" + socialUser)
      }
      users = users + (socialUser.id.id + socialUser.id.providerId -> socialUser)
      return socialUser
    }

    transaction {
      val dbUser: User = model.User.lookupByOpenId(socialUser.id.id + socialUser.id.providerId).getOrElse(addNewUserToDatabase(socialUser))

      val roles = dbUser.roles.map { r => r.name }

      if (Logger.isDebugEnabled) {
        Logger.debug("Adding the following roles " + roles + " to user " + socialUser)
      }

      val socialUserWithRoles = socialUser.copy(hackathonUserId = dbUser.id, roles = roles)

      users = users + (socialUserWithRoles.id.id + socialUserWithRoles.id.providerId -> socialUserWithRoles)
      socialUserWithRoles
    }
  }

  private def addNewUserToDatabase(socialUser: SocialUser): User = {
    inTransaction {

      if (Logger.isDebugEnabled) {
        Logger.debug("Adding a new user for social user " + socialUser)
      }

      val newUser = User(socialUser.displayName, socialUser.email.getOrElse(""), "", "", socialUser.avatarUrl.getOrElse(""), socialUser.id.id + socialUser.id.providerId)
      model.User.insert(newUser)

      val userRole = model.Role.lookupByName("user")

      if (Logger.isDebugEnabled) {
        Logger.debug("Associating " + userRole + " with newly created user " + newUser.id + " for social user " + socialUser)
      }
      
      userRole.map { role =>
    	  newUser.addRole(role)  
      }

      newUser
    }
  }

}
