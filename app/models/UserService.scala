package models

import javax.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.api.util.{ PasswordHasherRegistry, PasswordInfo }
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.SignUpForm
import play.api.Logging
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

class UserService @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                            passwordHasherRegistry: PasswordHasherRegistry,
                            authInfoRepository: AuthInfoRepository)(implicit ec: ExecutionContext)
    extends IdentityService[User]
    with HasDatabaseConfigProvider[JdbcProfile]
    with Logging {

  import profile.api._

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    db.run(
      userTable
        .filter(user => user.providerKey === loginInfo.providerKey && user.providerID === loginInfo.providerID)
        .result
        .headOption)

  def create(data: SignUpForm.Data): Future[LoginInfo] = {
    val user = User(
      id = None,
      firstName = Some(data.firstName),
      lastName = Some(data.lastName),
      email = Some(data.email),
      providerID = CredentialsProvider.ID,
      providerKey = data.email)

    db.run {
      (userTable returning userTable.map(_.id)) += user
    } andThen {
      case Failure(e: Throwable) =>
        logger.error("Cannot create user!", e)
        None

      case Success(id: Option[Int]) =>
        logger.info(s"Successfully created user ${id}")

        val loginInfo: LoginInfo   = LoginInfo(CredentialsProvider.ID, data.email)
        val authInfo: PasswordInfo = passwordHasherRegistry.current.hash(data.password)
        authInfoRepository.add(loginInfo, authInfo)
    } map { _id =>
      LoginInfo(CredentialsProvider.ID, user.email.get)
    }

  }
}
