import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }
import com.mohiva.play.silhouette.impl.providers.SocialProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
import slick.lifted.ProvenShape.proveShapeOf

package object models {

  case class User(id: Option[Int], firstName: Option[String], lastName: Option[String], email: Option[String])
      extends Identity

  class Users(tag: Tag) extends Table[User](tag, "USER") {
    def id        = column[Option[Int]]("USER_ID", O.PrimaryKey, O.AutoInc)
    def firstName = column[Option[String]]("FIRST_NAME")
    def lastName  = column[Option[String]]("LAST_NAME")
    def email     = column[Option[String]]("EMAIL")

    def * : ProvenShape[User] = (id, firstName, lastName, email) <> (User.tupled, User.unapply)
  }

  // A profile contains a login info, and a single identity can contain multiple profiles.
  case class Profile(userId: Option[Int], providerID: String, providerKey: String) extends SocialProfile {
    def loginInfo: LoginInfo = LoginInfo(providerID, providerKey)
  }

  class Profiles(tag: Tag) extends Table[Profile](tag, "USER_PROFILE") {
    def userId      = column[Option[Int]]("USER_ID")
    def providerID  = column[String]("PROVIDER_ID")
    def providerKey = column[String]("PROVIDER_KEY")

    // Define a primary key
    def pk = primaryKey("profile_pk_id", (providerID, providerKey))

    // Define foreign key back to the user
    def user = foreignKey("profile_user_id_fk", userId, userTable)(_.id)

    def * : ProvenShape[Profile] = (userId, providerID, providerKey) <> (Profile.tupled, Profile.unapply)
  }

  case class Password(key: String, hasher: String, hash: String, salt: Option[String])

  class Passwords(tag: Tag) extends Table[Password](tag, "PASSWORD") {
    def key    = column[String]("PROVIDER_KEY", O.PrimaryKey)
    def hasher = column[String]("HASHER")
    def hash   = column[String]("HASH")
    def salt   = column[Option[String]]("SALT")
    def *      = (key, hasher, hash, salt) <> (Password.tupled, Password.unapply)
  }

  lazy val userTable     = TableQuery[Users]
  lazy val profileTable  = TableQuery[Profiles]
  lazy val passwordTable = TableQuery[Passwords]

}
