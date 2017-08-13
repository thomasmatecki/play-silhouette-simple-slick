package models

import com.google.inject.Inject
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
    val formData = SignUpForm.Data("Thomas", "Matecki", "thomas.matecki@gmail.com", "SecretPassword")

    "create a new record into the database" in {

      val userService = cache(loader.app)
      val res = Await.result(userService.create(formData), Duration.Inf) should be(User(None, Some("Thomas"), Some("Matecki"), Some("thomas.matecki@gmail.com"), "credentials", "thomas.matecki@gmail.com"))
    }

  }
}
