package models

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class PasswordDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                            userService: UserService)(implicit ec: ExecutionContext)
  extends DelegableAuthInfoDAO[PasswordInfo]
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] =
    db.run(
      passwordTable
        .filter(password => password.key === loginInfo.providerKey)
        .result
        .headOption)
      .map(_.map(password => PasswordInfo(password.hasher, password.hash, password.salt)))

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    db.run(
      passwordTable += Password(loginInfo.providerKey, authInfo.hasher, authInfo.password, authInfo.salt)
    ).map(res => {
      authInfo
    })

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {

    val q = for {
      password <- passwordTable if password.key === loginInfo.providerKey
    } yield (password.hasher, password.hash, password.salt)

    db.run(q.update(authInfo.hasher, authInfo.password, authInfo.salt)).map(_ => authInfo)

  }
  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None => add(loginInfo, authInfo)
    }

  override def remove(loginInfo: LoginInfo): Future[Unit] = db.run(
    passwordTable.filter(password => password.key === loginInfo.providerKey).delete
  ).map(i => Unit)


}
