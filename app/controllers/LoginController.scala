package controllers

import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import forms.LoginForm
import javax.inject.Inject
import utils.DefaultEnv

import scala.concurrent.{ ExecutionContext, Future }

class LoginController @Inject()(cc: SilhouetteControllerComponents[DefaultEnv])(implicit ec: ExecutionContext)
    extends SilhouetteController(cc) {

  def loginForm = UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.login(LoginForm.form)))
  }

  def loginSubmit = UnsecuredAction.async { implicit request =>
    LoginForm.form.bindFromRequest.fold(
      hasErrors => {
        Future.successful(BadRequest(views.html.login(hasErrors)))
      },
      success => {
        val credentials = Credentials(success.email, success.password)
        credentialsProvider
          .authenticate(credentials)
          .flatMap(login)
          .recover {
            case e: IdentityNotFoundException =>
              Redirect(routes.LoginController.loginForm()).flashing("login-error" -> "Invalid user/password")

            case e: Exception =>
              logger.error("Cannot login from unexpected exception!", e)
              Redirect(routes.LoginController.loginForm()).flashing("login-error" -> e.getMessage)
          }
      }
    )
  }

}
