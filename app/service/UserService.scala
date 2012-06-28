package service

import play.api.{Logger, Application}
import org.squeryl.PrimitiveTypeMode.{transaction, inTransaction}
import securesocial.core.{UserServicePlugin, UserId, SocialUser}
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
      Logger.info("Already logged in updating users maps" + socialUser)
      users = users + (socialUser.id.id + socialUser.id.providerId -> socialUser)
      return socialUser
    }

    transaction {
      val dbUser:User = Model.findUserByOpenId(socialUser.id.id + socialUser.id.providerId).getOrElse(addNewUserToDatabase(socialUser))

      val roles = dbUser.roles.map { r => r.name }
      val socialUserWithRoles = socialUser.copy(roles = roles)

      users = users + (socialUserWithRoles.id.id + socialUserWithRoles.id.providerId -> socialUserWithRoles)
      socialUserWithRoles
    }
  }

  private def addNewUserToDatabase(socialUser: SocialUser): User = {
      inTransaction {
        Logger.info("trying to save/load: " + socialUser)

        Logger.info("adding new user!")
        val newUser = User(socialUser.displayName, socialUser.email.getOrElse(""), "", "", socialUser.avatarUrl.getOrElse(""), socialUser.id.id + socialUser.id.providerId)
        Logger.info("adding new user " + newUser)
        Model.users.insert(newUser)
        Logger.info("new id " + newUser.id)
        
        val userRole = Model.findRoleByName("user").get
        
        Logger.info("associating " + userRole + " with user " + newUser.id)
        newUser.roles.associate(userRole)

        newUser
      }
    }

}
