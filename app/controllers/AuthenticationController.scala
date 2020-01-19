package controllers

import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.{ LoginEvent, LoginInfo, LogoutEvent }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms._
import javax.inject.Inject
import play.api.data.Form
import play.api.mvc.RequestHeader
import utils.DefaultEnv

import scala.concurrent.{ ExecutionContext, Future }

class AuthenticationController @Inject()(cc: SilhouetteControllerComponents[DefaultEnv])(implicit ec: ExecutionContext)
    extends SilhouetteController(cc) {

  def signUpForm = UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.signup(SignUpForm.form)))
  }

  def signUpSubmit = UnsecuredAction.async { implicit request =>
    SignUpForm.form.bindFromRequest.fold(
      (hasErrors: Form[SignUpForm.Data]) => Future.successful(BadRequest(views.html.signup(hasErrors))),
      (success: SignUpForm.Data) => {
        val loginUserInfo = LoginInfo(CredentialsProvider.ID, success.email)
        userService.retrieve(loginUserInfo).flatMap {
          case Some(user) =>
            val conflictForm = SignUpForm.form
              .fill(success.copy(password = ""))
              .withGlobalError(s"Email conflict exists for email address!")
            Future.successful(BadRequest(views.html.signup(conflictForm)))
          case None =>
            val future = userService.create(success).flatMap(authenticateLoginInfo)
            future.recoverWith {
              case e =>
                logger.error("Cannot create user", e)
                Future.successful(AuthenticatorResult(Redirect(routes.AuthenticationController.signInForm)))
            }
        }
      }
    )
  }

  def signInForm = UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.signin(SignInForm.form)))
  }

  def signInSubmit = UnsecuredAction.async { implicit request =>
    SignInForm.form.bindFromRequest.fold(
      hasErrors => {
        Future.successful(BadRequest(views.html.signin(hasErrors)))
      },
      success => {
        val credentials = Credentials(success.email, success.password)
        credentialsProvider
          .authenticate(credentials)
          .flatMap(authenticateLoginInfo)
          .recover {
            case e: IdentityNotFoundException =>
              Redirect(routes.AuthenticationController.signInForm()).flashing("login-error" -> "Invalid user/password")

            case e: Exception =>
              logger.error("Cannot login from unexpected exception!", e)
              Redirect(routes.AuthenticationController.signInForm()).flashing("login-error" -> e.getMessage)
          }
      }
    )
  }

  def signOut = SecuredAction.async { implicit request =>
    val result = Redirect(routes.AuthenticationController.signInForm())
    eventBus.publish(LogoutEvent(request.identity, request))
    authenticatorService.discard(request.authenticator, result)
  }

  protected def authenticateLoginInfo(loginInfo: LoginInfo)(
      implicit request: RequestHeader): Future[AuthenticatorResult] = {
    val result = Redirect(routes.HomeController.index()).flashing("welcome" -> "Welcome!")
    authenticatorService
      .create(loginInfo)
      .flatMap { authenticator =>
        authenticatorService.init(authenticator).flatMap { v =>
          for {
            maybeUser <- userService.retrieve(loginInfo)
            user      <- maybeUser
          } eventBus.publish(LoginEvent(user, request))
          authenticatorService.embed(v, result)
        }
      }
  }

}
