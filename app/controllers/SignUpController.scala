package controllers

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms._
import javax.inject.Inject
import play.api.data.Form
import utils.DefaultEnv

import scala.concurrent.{ ExecutionContext, Future }

class SignUpController @Inject()(cc: SilhouetteControllerComponents[DefaultEnv])(implicit ec: ExecutionContext)
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
            val future = userService.create(success).flatMap(login)
            future.recoverWith {
              case e =>
                logger.error("Cannot create user", e)
                Future.successful(AuthenticatorResult(Redirect(routes.LoginController.loginForm)))
            }
        }
      }
    )
  }

}
