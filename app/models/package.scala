import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
import slick.lifted.ProvenShape.proveShapeOf

package object models {

  case class User(id: String,
                  firstName: Option[String],
                  lastName: Option[String],
                  email: Option[String],
                  providerID: String,
                  providerKey: String)

  class Users(tag: Tag) extends Table[User](tag, "USER") {
    def id = column[String]("USER_ID", O.PrimaryKey)
    def firstName = column[Option[String]]("FIRST_NAME")
    def lastName = column[Option[String]]("LAST_NAME")
    def email = column[Option[String]]("EMAIL")
    def providerID = column[String]("PROVIDER_ID")
    def providerKey = column[String]("PROVIDER_KEY")

    def * : ProvenShape[User] = (id, firstName, lastName, email, providerID, providerKey) <> (User.tupled, User.unapply)
  }

  case class PasswordInfo(hasher: String,
                          password: String,
                          salt: Option[String],
                          loginInfoId: Long)

  class PasswordInfos(tag: Tag) extends Table[PasswordInfo](tag, "PASSWORD_INFO") {
    def hasher = column[String]("HASHER")
    def password = column[String]("PASSWORD")
    def salt = column[Option[String]]("SALT")
    def userId = column[Long]("LOGIN_INFO_ID")

    def * = (hasher, password, salt, userId) <> (PasswordInfo.tupled, PasswordInfo.unapply)
  }

  val userTable = TableQuery[Users]
  val passwordInfoTable = TableQuery[PasswordInfos]

}
