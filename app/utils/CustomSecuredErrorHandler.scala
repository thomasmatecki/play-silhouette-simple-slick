package utils

import com.mohiva.play.silhouette.api.actions.SecuredErrorHandler
import javax.inject.Inject
import play.api.Logging
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.mvc.RequestHeader
import play.api.mvc.Results._

import scala.concurrent.Future

/**
  * Custom secured error handler.
  */
class CustomSecuredErrorHandler extends SecuredErrorHandler with Logging {

  /**
    * Called when a user is not authenticated.
    *
    * As defined by RFC 2616, the status code of the response should be 401 Unauthorized.
    *
    * @param request The request header.
    * @return The result to send to the client.
    */
  override def onNotAuthenticated(implicit request: RequestHeader) = {
    logger.warn(s"onNotAuthenticated: request = ${request}")
    Future.successful(Redirect(controllers.routes.LoginController.loginForm()))
  }

  /**
    * Called when a user is authenticated but not authorized.
    *
    * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
    *
    * @param request The request header.
    * @return The result to send to the client.
    */
  override def onNotAuthorized(implicit request: RequestHeader) = {
    logger.warn(s"onNotAuthorized: request = ${request}")
    Future.successful(
      Redirect(controllers.routes.LoginController.loginForm())
        .flashing("login-error" -> "Access Denied"))
  }
}
