package api

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.UserService
import org.specs2.mutable.Specification
import play.api.Application
import play.api.test.WithApplicationLoader

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class SilhouetteAuthenticationSpec @Inject()(implicit ec: ExecutionContext) extends Specification {


  "CredentialsProvider" should {

    "authenticate a test user" in new WithApplicationLoader {

      val credentialsProviderCache = Application.instanceCache[CredentialsProvider]
      val userServiceCache = Application.instanceCache[UserService]

      val credentialsProvider = credentialsProviderCache(app)
      val userService = userServiceCache(app)

      Await.result(
        credentialsProvider.authenticate(Credentials("test@test.com", "test")), Duration.Inf
      ) should be equals LoginInfo(CredentialsProvider.ID, "test@test.com")
    }

    "fail to authenticate an incorrect password" in new WithApplicationLoader {

      val credentialsProviderCache = Application.instanceCache[CredentialsProvider]
      val userServiceCache = Application.instanceCache[UserService]

      val credentialsProvider = credentialsProviderCache(app)
      val userService = userServiceCache(app)

      Await.result(
        credentialsProvider.authenticate(Credentials("test@test.com", "wrong_password")), Duration.Inf
      ) should not equals LoginInfo(CredentialsProvider.ID, "test@test.com")

    }

  }
}
