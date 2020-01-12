package models

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import forms.SignUpForm
import org.specs2.mutable._
import play.api.Application
import play.api.test.WithApplicationLoader

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class UserServiceSpec @Inject()(implicit ec: ExecutionContext) extends Specification {

  "UserService" should {

    val cache = Application.instanceCache[UserService]
    val loader = new WithApplicationLoader() {}

    val formData = SignUpForm.Data("John", "Smith", "John.Smith@gmail.com", "SecretPassword")

    "create a new record in the database" in {

      val userService = cache(loader.app)
      val info = Await.result(userService.create(formData), Duration.Inf)
      info.providerKey must be("John.Smith@gmail.com")
    }
  }
}
