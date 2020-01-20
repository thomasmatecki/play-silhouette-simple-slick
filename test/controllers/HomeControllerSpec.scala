package controllers

import com.mohiva.play.silhouette.api.actions.{DefaultSecuredAction, DefaultSecuredErrorHandler, DefaultSecuredRequestHandler, DefaultUnsecuredErrorHandler, SecuredAction, SecuredActionBuilder, SecuredActionComponents, SecuredErrorHandler, SecuredRequestHandler, UnsecuredActionComponents, UnsecuredErrorHandler, UserAwareActionBuilder, UserAwareActionComponents}
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.api.{Environment, LoginInfo, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.password.{BCryptPasswordHasher, BCryptSha256PasswordHasher}
import com.mohiva.play.silhouette.persistence.daos.{DelegableAuthInfoDAO, InMemoryAuthInfoDAO}
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import com.mohiva.play.silhouette.test.{FakeEnvironment, FakeIdentityService}
import models.User
import org.scalatestplus.play._
import play.api.mvc.{AnyContent, AnyContentAsEmpty, BodyParsers, DefaultActionBuilder}
import play.api.test.Helpers._
import play.api.test._
import utils.DefaultEnv
import play.api.test.CSRFTokenHelper._

import scala.concurrent.ExecutionContext

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class HomeControllerSpec extends PlaySpec with org.scalatestplus.mockito.MockitoSugar {

  "HomeController GET" should {

    "render the index page from a new instance of controller" in {
      val passwordDAO = new InMemoryAuthInfoDAO[PasswordInfo]()
      val loginInfo = LoginInfo(CredentialsProvider.ID, "test@example.com")
      val user = User(Some(1), None, None, None, CredentialsProvider.ID, "test@example.com")
      val identities = Seq(loginInfo -> user)
      val userService = new FakeIdentityService[User](identities: _*)
      val components = controllerComponents(userService, passwordDAO, identities)

      val controller = new HomeController(components)
      val home = controller.index().apply(FakeRequest(GET, "/").withCSRFToken)

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Simple Silhouette App")
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

  val stubs = stubControllerComponents()

  def stubSilhouette(identities: Seq[(LoginInfo, DefaultEnv#I)]): Silhouette[DefaultEnv] = {
    implicit val ec: ExecutionContext = stubs.executionContext
    val fakeEnvironment = FakeEnvironment[DefaultEnv](identities = identities)

    val bodyParser = new BodyParsers.Default(stubs.parsers)

    val securedActionComponents = new SecuredActionComponents {
      override def securedErrorHandler: SecuredErrorHandler = new DefaultSecuredErrorHandler(stubs.messagesApi)
      override def securedBodyParser: BodyParsers.Default = bodyParser
    }

    val userAwareActionComponents = new UserAwareActionComponents {
      override def userAwareBodyParser: BodyParsers.Default = bodyParser
    }

    val unsecuredActionComponents = new UnsecuredActionComponents {
      override def unsecuredBodyParser: BodyParsers.Default = bodyParser
      override def unsecuredErrorHandler: UnsecuredErrorHandler = new DefaultUnsecuredErrorHandler(stubs.messagesApi)
    }

    new SilhouetteProvider[DefaultEnv](
      env = fakeEnvironment,
      securedAction = securedActionComponents.securedAction,
      userAwareAction = userAwareActionComponents.userAwareAction,
      unsecuredAction = unsecuredActionComponents.unsecuredAction
    )
  }

  def controllerComponents(userService: IdentityService[User], passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo], identities: Seq[(LoginInfo, DefaultEnv#I)]) = {
    implicit val ec: ExecutionContext = stubs.executionContext

    val passwordHasherRegistry = PasswordHasherRegistry(new BCryptSha256PasswordHasher(), Seq(new BCryptPasswordHasher()))
    val authInfoRepository = new DelegableAuthInfoRepository(passwordInfoDAO)
    DefaultMyControllerComponents(
      parsers  = stubs.parsers,
      messagesApi  = stubs.messagesApi,
      langs = stubs.langs,
      fileMimeTypes = stubs.fileMimeTypes,
      executionContext = stubs.executionContext,
      silhouette = stubSilhouette(identities),
      identityService = userService,
      authInfoRepository = authInfoRepository,
      passwordHasherRegistry = passwordHasherRegistry,
      clock = Clock(),
      credentialsProvider = new CredentialsProvider(authInfoRepository, passwordHasherRegistry),
      messagesActionBuilder = stubMessagesControllerComponents().messagesActionBuilder,
      actionBuilder = DefaultActionBuilder(stubBodyParser(AnyContentAsEmpty))
    )
  }

}
