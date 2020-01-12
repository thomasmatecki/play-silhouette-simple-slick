package controllers

import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.{AuthenticatorService, IdentityService}
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry}
import com.mohiva.play.silhouette.api.{Authorization, EventBus, Silhouette}
import com.mohiva.play.silhouette.impl.providers._
import javax.inject.Inject
import models.{User, UserService}
import play.api.Logging
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import utils.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

/**
  * An abstract class that contains and exposes all the components need by concrete controllers.
  *
  * @param controllerComponents the components needed to inject.
  */
abstract class MyAbstractController(override protected val controllerComponents: MyControllerComponents)
    extends MessagesAbstractController(controllerComponents)
    with Logging {

  type SecuredEnvRequest[A]   = SecuredRequest[DefaultEnv, A]
  type UserAwareEnvRequest[A] = UserAwareRequest[DefaultEnv, A]

  class MyActionFunction extends ActionFunction[Request, MyRequest] {
    def invokeBlock[A](request: Request[A], block: MyRequest[A] => Future[Result]): Future[Result] = {
      val myRequest = new MyRequest[A](
        messagesApi = messagesApi,
        request = request
      )
      block(myRequest)
    }

    override protected def executionContext: ExecutionContext =
      controllerComponents.executionContext
  }

  class MySecuredActionFunction extends ActionFunction[SecuredEnvRequest, MySecuredRequest] {
    def invokeBlock[A](request: SecuredEnvRequest[A], block: MySecuredRequest[A] => Future[Result]): Future[Result] = {
      val myRequest = new MySecuredRequest[A](
        messagesApi = messagesApi,
        identity = request.identity,
        authenticator = request.authenticator,
        request = request
      )
      block(myRequest)
    }

    override protected def executionContext: ExecutionContext =
      controllerComponents.executionContext
  }

  class MyUserAwareActionFunction extends ActionFunction[UserAwareEnvRequest, MyUserAwareRequest] {
    def invokeBlock[A](request: UserAwareEnvRequest[A],
                       block: MyUserAwareRequest[A] => Future[Result]): Future[Result] = {
      val myRequest = new MyUserAwareRequest[A](
        messagesApi = messagesApi,
        identity = request.identity,
        authenticator = request.authenticator,
        request = request
      )
      block(myRequest)
    }

    override protected def executionContext: ExecutionContext =
      controllerComponents.executionContext
  }

  protected def UnsecuredAction = silhouette.UnsecuredAction.andThen(new MyActionFunction)

  protected def SecuredAction = silhouette.SecuredAction.andThen(new MySecuredActionFunction)

  protected def SecuredAction(errorHandler: SecuredErrorHandler) =
    silhouette.SecuredAction(errorHandler).andThen(new MySecuredActionFunction)

  protected def SecuredAction(authorization: Authorization[DefaultEnv#I, DefaultEnv#A]) =
    silhouette.SecuredAction(authorization).andThen(new MySecuredActionFunction)

  protected def UserAwareAction = silhouette.UserAwareAction.andThen(new MyUserAwareActionFunction)

  protected def userService: UserService                   = controllerComponents.identityService.asInstanceOf[UserService]
  protected def credentialsProvider: CredentialsProvider                 = controllerComponents.credentialsProvider
  protected def silhouette: Silhouette[DefaultEnv]                       = controllerComponents.silhouette
  protected def authenticatorService: AuthenticatorService[DefaultEnv#A] = silhouette.env.authenticatorService
  protected def eventBus: EventBus                                       = silhouette.env.eventBus
}

trait SilhouetteComponents {
  def identityService: IdentityService[User]
  def authInfoRepository: AuthInfoRepository
  def passwordHasherRegistry: PasswordHasherRegistry
  def clock: Clock
  def credentialsProvider: CredentialsProvider
  def silhouette: Silhouette[DefaultEnv]
}

trait MyControllerComponents extends MessagesControllerComponents with SilhouetteComponents

final case class DefaultMyControllerComponents @Inject()(
                                                          silhouette: Silhouette[DefaultEnv],
                                                          identityService: IdentityService[User],
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
) extends MyControllerComponents
