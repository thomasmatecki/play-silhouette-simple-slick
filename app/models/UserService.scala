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

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    val usersQuery = profileTable
      .filter(p => p.providerID === loginInfo.providerID && p.providerKey === loginInfo.providerKey)
      .flatMap(_.user)
    db.run(usersQuery.result.headOption)
  }

  def profiles(userId: Int): Future[Seq[LoginInfo]] = {
    lazy val loginInfoFromUserIdQuery = for {
      profile <- profileTable if profile.userId === Option(userId)
    } yield profile
    db.run(loginInfoFromUserIdQuery.result).map(_.map(_.loginInfo))
  }

  def create(data: SignUpForm.Data): Future[LoginInfo] = {
    val user =
      User(id = None, firstName = Some(data.firstName), lastName = Some(data.lastName), email = Some(data.email))

    def profile(userId: Int, email: String): Profile = Profile(
      userId = Some(userId),
      providerID = CredentialsProvider.ID,
      providerKey = email
    )

    db.run {
      for {
        userId <- (userTable returning userTable.map(_.id)) += user
        newProfile = profile(userId.head, data.email)
        _ <- profileTable += newProfile
      } yield newProfile.loginInfo
    } andThen {
      case Failure(e: Throwable) =>
        logger.error("Cannot create user!", e)
        None

      case Success(loginInfo: LoginInfo) =>
        logger.info(s"Successfully created loginInfo ${loginInfo}")
        val authInfo: PasswordInfo = passwordHasherRegistry.current.hash(data.password)
        authInfoRepository.add(loginInfo, authInfo)
    }
  }
}
