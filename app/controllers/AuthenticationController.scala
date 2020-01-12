package controllers

import com.mohiva.play.silhouette.api.{LoginInfo, LogoutEvent}
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.{SignInForm, SignUpForm}
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Cookie, RequestHeader}

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationController @Inject()(cc: MyControllerComponents)(implicit ec: ExecutionContext)
    extends MyAbstractController(cc)
    with I18nSupport {

  def signUpForm = UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.signup(SignUpForm.form)))
  }

  def signUpSubmit = UnsecuredAction.async { implicit request =>
    SignUpForm.form.bindFromRequest.fold(
      (hasErrors: Form[SignUpForm.Data]) => Future.successful(BadRequest(views.html.signup(hasErrors))),
      (success: SignUpForm.Data) => {
        userService.retrieve(LoginInfo(CredentialsProvider.ID, success.email)).flatMap {
          case Some(user) =>
            val conflictForm = SignUpForm.form.fill(success.copy(password = ""))
              .withGlobalError(s"Email conflict exists for email address!")
            Future.successful(BadRequest(views.html.signup(conflictForm)))
          case None =>
            userService.create(success).flatMap { loginInfo =>
              loginInfoToCookie(loginInfo).flatMap { cookie =>
                val result = Redirect(routes.HomeController.index())
                authenticatorService.embed(cookie, result)
              }
            }.recoverWith { case e =>
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
        credentialsProvider
          .authenticate(credentials = Credentials(success.email, success.password))
          .flatMap { loginInfo =>
            loginInfoToCookie(loginInfo).flatMap { cookie =>
              val result = Redirect(routes.HomeController.index()).flashing("welcome" -> "Welcome!")
              authenticatorService.embed(cookie, result)
            }
          }.recover {
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

  private def loginInfoToCookie(loginInfo: LoginInfo)(implicit rh: RequestHeader): Future[Cookie] = {
    authenticatorService.create(loginInfo).flatMap(authenticatorService.init)
  }
}
