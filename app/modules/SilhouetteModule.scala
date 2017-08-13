package modules

import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{PasswordHasher, PasswordInfo}
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import models.PasswordDAO
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.Execution.Implicits._

class SilhouetteModule extends AbstractModule with ScalaModule {

  /**
    * @see https://www.playframework.com/documentation/2.6.x/ScalaDependencyInjection#programmatic-bindings
    */
  override def configure(): Unit = {
    bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordDAO]
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
  }

  @Provides
  def provideAuthInfoRepository(passwordDAO: DelegableAuthInfoDAO[PasswordInfo]): AuthInfoRepository =
    new DelegableAuthInfoRepository(passwordDAO)
}
