package controllers

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.{ AuthenticatorResult, AuthenticatorService }
import com.mohiva.play.silhouette.api.util.{ Clock, PasswordHasherRegistry }
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import models.UserService
import play.api.Logging
import play.api.http.FileMimeTypes
import play.api.i18n.{ Langs, MessagesApi }
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.higherKinds
import scala.util.Success

abstract class SilhouetteController[E <: Env](
    override protected val controllerComponents: SilhouetteControllerComponents[E])
    extends MessagesAbstractController(controllerComponents)
    with SilhouetteComponents[E]
    with Logging {

  type SecuredEnvRequest[A]      = SecuredRequest[E, A]
  type AppSecuredEnvRequest[A]   = AppSecuredRequest[E, A]
  type UserAwareEnvRequest[A]    = UserAwareRequest[E, A]
  type AppUserAwareEnvRequest[A] = AppUserAwareRequest[E, A]

  /*
   * Abstract class to stop defining executionContext in every subclass
   *
   * @param cc controller components
   */
  protected abstract class AbstractActionTransformer[-R[_], +P[_]] extends ActionTransformer[R, P] {
    override protected def executionContext: ExecutionContext =
      controllerComponents.executionContext
  }

  /**
    * Transforms from a Request into AppRequest.
    */
  class AppActionTransformer extends AbstractActionTransformer[Request, AppRequest] {
    override protected def transform[A](request: Request[A]): Future[AppRequest[A]] =
      Future.successful(
        new AppRequest[A](
          messagesApi = controllerComponents.messagesApi,
          request = request
        ))
  }

  /**
    * Transforms from a SecuredRequest into AppSecuredRequest.
    */
  class AppSecuredActionTransformer extends AbstractActionTransformer[SecuredEnvRequest, AppSecuredEnvRequest] {
    override protected def transform[A](request: SecuredEnvRequest[A]): Future[AppSecuredEnvRequest[A]] =
      Future.successful(
        new AppSecuredRequest[E, A](
          messagesApi = controllerComponents.messagesApi,
          identity = request.identity,
          authenticator = request.authenticator,
          request = request
        ))
  }

  /**
    * Transforms from a UserAwareRequest into AppUserAwareRequest.
    */
  class AppUserAwareActionTransformer extends AbstractActionTransformer[UserAwareEnvRequest, AppUserAwareEnvRequest] {
    override protected def transform[A](request: UserAwareEnvRequest[A]): Future[AppUserAwareEnvRequest[A]] =
      Future.successful(
        new AppUserAwareRequest[E, A](
          messagesApi = controllerComponents.messagesApi,
          identity = request.identity,
          authenticator = request.authenticator,
          request = request
        ))
  }
  private val appActionTransformer          = new AppActionTransformer
  private val appSecuredActionTransformer   = new AppSecuredActionTransformer
  private val appUserAwareActionTransformer = new AppUserAwareActionTransformer

  def UnsecuredAction: ActionBuilder[AppRequest, AnyContent] = silhouette.UnsecuredAction.andThen(appActionTransformer)

  def SecuredAction: ActionBuilder[AppSecuredEnvRequest, AnyContent] =
    silhouette.SecuredAction.andThen(appSecuredActionTransformer)

  def SecuredAction(errorHandler: SecuredErrorHandler): ActionBuilder[AppSecuredEnvRequest, AnyContent] =
    silhouette.SecuredAction(errorHandler).andThen(appSecuredActionTransformer)

  def SecuredAction(authorization: Authorization[E#I, E#A]): ActionBuilder[AppSecuredEnvRequest, AnyContent] =
    silhouette.SecuredAction(authorization).andThen(appSecuredActionTransformer)

  def UserAwareAction: ActionBuilder[AppUserAwareEnvRequest, AnyContent] =
    silhouette.UserAwareAction.andThen(appUserAwareActionTransformer)

  protected def login(loginInfo: LoginInfo)(implicit request: RequestHeader): Future[AuthenticatorResult] = {
    implicit val ec: ExecutionContext = controllerComponents.executionContext
    val valueFuture = for {
      authenticator <- authenticatorService.create(loginInfo)
      v             <- authenticatorService.init(authenticator)
    } yield v

    val result = Redirect(routes.HomeController.index()).flashing("welcome" -> "Welcome!")
    valueFuture.flatMap(authenticatorService.embed(_, result)).andThen {
      case Success(_) =>
        for {
          maybeUser <- userService.retrieve(loginInfo)
          user      <- maybeUser
        } eventBus.publish(LoginEvent(user, request))
    }
  }

  def userService: UserService                       = controllerComponents.userService
  def authInfoRepository: AuthInfoRepository         = controllerComponents.authInfoRepository
  def passwordHasherRegistry: PasswordHasherRegistry = controllerComponents.passwordHasherRegistry
  def clock: Clock                                   = controllerComponents.clock
  def credentialsProvider: CredentialsProvider       = controllerComponents.credentialsProvider

  def silhouette: Silhouette[E]                       = controllerComponents.silhouette
  def authenticatorService: AuthenticatorService[E#A] = silhouette.env.authenticatorService
  def eventBus: EventBus                              = silhouette.env.eventBus
}

trait SilhouetteComponents[E <: Env] {
  def silhouette: Silhouette[E]
  def userService: UserService
  def authInfoRepository: AuthInfoRepository
  def passwordHasherRegistry: PasswordHasherRegistry
  def clock: Clock
  def credentialsProvider: CredentialsProvider
}

trait SilhouetteControllerComponents[E <: Env] extends MessagesControllerComponents with SilhouetteComponents[E]

final case class DefaultSilhouetteControllerComponents[E <: Env] @Inject()(
    silhouette: Silhouette[E],
    userService: UserService,
    authInfoRepository: AuthInfoRepository,
    passwordHasherRegistry: PasswordHasherRegistry,
    clock: Clock,
    credentialsProvider: CredentialsProvider,
    messagesActionBuilder: MessagesActionBuilder,
    actionBuilder: DefaultActionBuilder,
    parsers: PlayBodyParsers,
    messagesApi: MessagesApi,
    langs: Langs,
    fileMimeTypes: FileMimeTypes,
    executionContext: scala.concurrent.ExecutionContext
) extends SilhouetteControllerComponents[E]
