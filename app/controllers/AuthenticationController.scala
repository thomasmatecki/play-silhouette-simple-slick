package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import forms.SignUpForm
import models.UserService
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
                                         silhouette: Silhouette[DefaultEnv])
                                        (implicit ec: ExecutionContext)
  extends AbstractController(cc)
    with I18nSupport {

  def signUpForm = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.signup(SignUpForm.form)))
  }

  def signUpSubmit = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>

    SignUpForm.form.bindFromRequest.fold(
      hasErrors => Future.successful(BadRequest(views.html.signup(hasErrors))),
      success => userService.create(success).map(_ => Ok(views.html.index()))
    )
  }
}
