package controllers

import com.mohiva.play.silhouette.api.LogoutEvent
import javax.inject._
import play.api.mvc.AnyContent
import utils.DefaultEnv

import scala.concurrent.Future

@Singleton
class HomeController @Inject()(cc: SilhouetteControllerComponents[DefaultEnv]) extends SilhouetteController(cc) {

  def index() = UserAwareAction.async { implicit request: AppUserAwareEnvRequest[AnyContent] =>
    logger.debug(s"home: index = ${request.identity}")
    Future.successful(Ok(views.html.index()))
  }

  def logout = SecuredAction.async { implicit request =>
    val result = Redirect(routes.LoginController.loginForm())
    eventBus.publish(LogoutEvent(request.identity, request))
    authenticatorService.discard(request.authenticator, result)
  }
}
