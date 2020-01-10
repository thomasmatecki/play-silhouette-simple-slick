package controllers

import play.api.mvc.RequestHeader

import com.mohiva.play.silhouette.api.{ Authenticator, Env, Identity }

// XXX should be OOTB
trait SecuredRequestHeader[E <: Env] extends RequestHeader with IdentityProvider[E#I] with AuthenticatorProvider[E#A]

// XXX should be OOTB
trait UserAwareRequestHeader[E <: Env]
    extends RequestHeader
    with IdentityAwareProvider[E#I]
    with AuthenticatorAwareProvider[E#A]

trait IdentityProvider[I <: Identity] {
  def identity: I
}

trait IdentityAwareProvider[I <: Identity] {
  def identity: Option[I]
}

trait AuthenticatorProvider[A <: Authenticator] {
  def authenticator: A
}

trait AuthenticatorAwareProvider[A <: Authenticator] {
  def authenticator: Option[A]
}
