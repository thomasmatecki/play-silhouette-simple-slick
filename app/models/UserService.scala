package models

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class UserService @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                           (implicit ec: ExecutionContext)
  extends IdentityService[User]
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    db.run(
      userTable
        .filter(user => user.providerKey === loginInfo.providerKey && user.providerID === loginInfo.providerID)
        .result
        .headOption)
      .map(_.map(user => user))
  }
}

