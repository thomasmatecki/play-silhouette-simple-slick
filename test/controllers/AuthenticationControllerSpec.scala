package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.UserService
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Application
import play.api.test.Helpers._
import play.api.test._
import utils.DefaultEnv

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class AuthenticationControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  val userServiceCache = Application.instanceCache[UserService]
  val credProviderCache = Application.instanceCache[CredentialsProvider]
  val silhouetteCache = Application.instanceCache[Silhouette[DefaultEnv]]

  "AuthenticationController GET" should {

    "render the sign in  page from a new instance of controller" in {
      val controller = new AuthenticationController(stubControllerComponents(), userServiceCache(app), silhouetteCache(app), credProviderCache(app))

      val home = controller.signInForm.apply(FakeRequest(GET, "/signIn"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to Play")
    }

  }
}
