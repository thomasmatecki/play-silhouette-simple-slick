package controllers

import play.api.i18n.MessagesApi
import play.api.mvc._
import utils.DefaultEnv

/**
  * A request header that can contain extra info specific to the domain.
  */
trait MyRequestHeader extends MessagesRequestHeader

/**
  * A wrapped request that contains any domain specific info you need.
  *
  * @param request original request
  * @param messagesApi injected messagesApi
  * @tparam B the body of the request
  */
class MyRequest[B](
    request: Request[B],
    messagesApi: MessagesApi,
) extends MessagesRequest[B](request, messagesApi)
    with MyRequestHeader
    with PreferredMessagesProvider

/**
  * The request header given by silhouette.  This should be provided by silhouette but it's not OOTB.
  */
trait MySecuredRequestHeader extends MyRequestHeader {
  def identity: DefaultEnv#I
  def authenticator: DefaultEnv#A
}

/**
  * An implementation of MyRequest with MySecuredRequestHeader.
  *
  * @param request original request
  * @param messagesApi injected messagesApi
  * @param identity verified silhouette identity
  * @param authenticator authenticator used by silhouette
  * @tparam B the body of the request
  */
class MySecuredRequest[B](
    request: Request[B],
    messagesApi: MessagesApi,
    val identity: DefaultEnv#I,
    val authenticator: DefaultEnv#A
) extends MyRequest(request, messagesApi)
    with MySecuredRequestHeader

/**
  * The user aware request header given by silhouette.  This should be provided by silhouette but it's not OOTB.
  */
trait MyUserAwareRequestHeader extends MyRequestHeader {
  def identity: Option[DefaultEnv#I]
  def authenticator: Option[DefaultEnv#A]
}

/**
  * An implementation of MyRequest with MyUserAwareRequestHeader.
  *
  * @param request original request
  * @param messagesApi injected messagesApi
  * @param identity verified silhouette identity
  * @param authenticator authenticator used by silhouette
  * @tparam B the body of the request
  */
class MyUserAwareRequest[B](
    request: Request[B],
    messagesApi: MessagesApi,
    val identity: Option[DefaultEnv#I],
    val authenticator: Option[DefaultEnv#A]
) extends MyRequest(request, messagesApi)
    with MyUserAwareRequestHeader
