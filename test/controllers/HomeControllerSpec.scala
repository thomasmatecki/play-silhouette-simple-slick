package controllers

import com.mohiva.play.silhouette.api.Silhouette
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.test.Helpers._
import play.api.test._
import utils.DefaultEnv

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class HomeControllerSpec extends PlaySpec with MockitoSugar {
  val silhouette = mock[Silhouette[DefaultEnv]]

  "HomeController GET" should {


    "render the index page from a new instance of controller" in {
      val controller = new HomeController(stubControllerComponents(), silhouette)
      val home = controller.index().apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to Play")
    }

    /*    "render the index page from the application" in {
          val controller = inject[HomeController]
          val home = controller.index().apply(FakeRequest(GET, "/"))

          status(home) mustBe OK
          contentType(home) mustBe Some("text/html")
          contentAsString(home) must include("Welcome to Play")
        }

        "render the index page from the router" in {
          val request = FakeRequest(GET, "/")
          val home = route(app, request).get

          status(home) mustBe OK
          contentType(home) mustBe Some("text/html")
          contentAsString(home) must include("Welcome to Play")
        }*/
  }
}
