package controllers

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

}
