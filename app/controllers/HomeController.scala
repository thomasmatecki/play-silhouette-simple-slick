package controllers

import javax.inject._

import com.mohiva.play.silhouette.api.Silhouette
import play.api.i18n.I18nSupport
import play.api.mvc._
import utils.DefaultEnv

import scala.concurrent.Future

@Singleton
class HomeController @Inject()(cc: ControllerComponents, silhouette: Silhouette[DefaultEnv])
  extends AbstractController(cc)
    with I18nSupport {

  def index() = silhouette.UserAwareAction.async { implicit request =>
    Future.successful(Ok(views.html.index(request.identity)))
  }

}
