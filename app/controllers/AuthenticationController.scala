package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.{SignInForm, SignUpForm}
import models.UserService
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import utils.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

/**
  * @see https://www.playframework.com/documentation/2.6.x/ScalaForms#Passing-MessagesProvider-to-Form-Helpers
  * @param cc
  * @param userService
  * @param silhouette
  */
class AuthenticationController @Inject()(cc: ControllerComponents,
                                         userService: UserService,
                                         silhouette: Silhouette[DefaultEnv],
                                         credentialsProvider: CredentialsProvider)
                                        (implicit ec: ExecutionContext)
  extends AbstractController(cc)
    with I18nSupport {

  val authService = silhouette.env.authenticatorService


  def signUpForm = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.signup(SignUpForm.form)))
  }

  def signUpSubmit = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>

    SignUpForm.form.bindFromRequest.fold(
      (hasErrors: Form[SignUpForm.Data]) => Future.successful(BadRequest(views.html.signup(hasErrors))),

      (success: SignUpForm.Data) => {

        userService.retrieve(LoginInfo(CredentialsProvider.ID, success.email))
          .flatMap((uo: Option[models.User]) =>
            uo.fold({
              userService.create(success).flatMap(authService.create(_))
                .flatMap(authService.init(_))
                .flatMap(authService.embed(_, Redirect(routes.HomeController.index())))

            })({ _ =>
              Future.successful(AuthenticatorResult(Redirect(routes.AuthenticationController.signInForm)))
            }))
      })
  }

  def signInForm = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.signin(SignInForm.form)))
  }

  def signInSubmit = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>

    SignInForm.form.bindFromRequest.fold(

      hasErrors => {
        Future.successful(BadRequest(views.html.signin(hasErrors)))
      },

      success => {

        Logger.debug(s"Attempting to login ${success.email}, ${success.password}")

        val authRes: Future[LoginInfo] = credentialsProvider.authenticate(Credentials(success.email, success.password))

        authRes.flatMap(u => {

          authService.create(u)
            .flatMap(authService.init(_))
            .flatMap(authService.embed(_, Redirect(routes.HomeController.index())))

        })
      }.recover {
        case e: ProviderException => {
          Logger.debug(s"Provider Exception: ${e.getMessage}")

          Redirect(routes.HomeController.index())
        }
      }
    )
  }
}
