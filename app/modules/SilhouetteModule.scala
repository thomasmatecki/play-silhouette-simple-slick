package modules

import com.google.inject.name.Named
import com.google.inject.{ AbstractModule, Provides }
import com.mohiva.play.silhouette.api.actions.{ SecuredErrorHandler, UnsecuredErrorHandler }
import com.mohiva.play.silhouette.api.crypto.{ Crypter, CrypterAuthenticatorEncoder, Signer }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{ Environment, EventBus, Silhouette, SilhouetteProvider }
import com.mohiva.play.silhouette.crypto.{ JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings }
import com.mohiva.play.silhouette.impl.authenticators.{
  CookieAuthenticator,
  CookieAuthenticatorService,
  CookieAuthenticatorSettings
}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.util.{ DefaultFingerprintGenerator, PlayCacheLayer, SecureRandomIDGenerator }
import com.mohiva.play.silhouette.password.{ BCryptPasswordHasher, BCryptSha256PasswordHasher }
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import controllers._

import models.{ PasswordDAO, UserService }
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.mvc.CookieHeaderEncoding
import utils.{ CustomSecuredErrorHandler, CustomUnsecuredErrorHandler, DefaultEnv }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class SilhouetteModule extends AbstractModule with ScalaModule {

  /**
    * @see https://www.playframework.com/documentation/2.6.x/ScalaDependencyInjection#programmatic-bindings
    */
  override def configure(): Unit = {
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
    bind[UnsecuredErrorHandler].to[CustomUnsecuredErrorHandler]
    bind[SecuredErrorHandler].to[CustomSecuredErrorHandler]
    bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordDAO]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
  }

  @Provides
  def provideEnvironment(userService: UserService,
                         authenticatorService: AuthenticatorService[CookieAuthenticator],
                         eventBus: EventBus)(implicit ec: ExecutionContext): Environment[DefaultEnv] =
    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )

  @Provides
  def provideFingerprintGenerator(): FingerprintGenerator =
    new DefaultFingerprintGenerator(false)

  @Provides
  def providesCookieAuthenticatorSettings(configuration: Configuration): CookieAuthenticatorSettings =
    CookieAuthenticatorSettings(
      cookieName = configuration.get[String]("silhouette.authenticator.cookieName"),
      cookiePath = configuration.get[String]("silhouette.authenticator.cookiePath"),
      cookieDomain = None,
      secureCookie = configuration.get[Boolean]("silhouette.authenticator.secureCookie"),
      httpOnlyCookie = configuration.get[Boolean]("silhouette.authenticator.httpOnlyCookie"),
      useFingerprinting = configuration.get[Boolean]("silhouette.authenticator.useFingerprinting"),
      cookieMaxAge = None,
      authenticatorIdleTimeout =
        configuration.getOptional[FiniteDuration]("silhouette.authenticator.authenticatorIdleTimeout"),
      authenticatorExpiry = configuration.get[FiniteDuration]("silhouette.authenticator.authenticatorExpiry")
    )

  @Provides
  def provideAuthenticatorService(
      @Named("authenticator-signer") signer: Signer,
      @Named("authenticator-crypter") crypter: Crypter,
      settings: CookieAuthenticatorSettings,
      cookieHeaderEncoding: CookieHeaderEncoding,
      fingerprintGenerator: FingerprintGenerator,
      idGenerator: IDGenerator,
      configuration: Configuration,
      clock: Clock)(implicit ec: ExecutionContext): AuthenticatorService[CookieAuthenticator] = {

    val authenticatorEncoder = new CrypterAuthenticatorEncoder(crypter)

    new CookieAuthenticatorService(
      settings,
      None,
      signer,
      cookieHeaderEncoding,
      authenticatorEncoder,
      fingerprintGenerator,
      idGenerator,
      clock)
  }

  @Provides
  def provideSecureRandomGenerator()(implicit ec: ExecutionContext): IDGenerator =
    new SecureRandomIDGenerator()

  @Provides
  def provideAuthInfoRepository(passwordDAO: DelegableAuthInfoDAO[PasswordInfo])(
      implicit ec: ExecutionContext): AuthInfoRepository =
    new DelegableAuthInfoRepository(passwordDAO)

  @Provides
  def providePasswordHasherRegistry(): PasswordHasherRegistry =
    PasswordHasherRegistry(new BCryptSha256PasswordHasher(), Seq(new BCryptPasswordHasher()))

  @Provides
  def provideCredentialsProvider(
      authInfoRepository: AuthInfoRepository,
      passwordHasherRegistry: PasswordHasherRegistry)(implicit ec: ExecutionContext): CredentialsProvider =
    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)

  @Provides
  @Named("authenticator-signer")
  def provideAuthenticatorSigner(configuration: Configuration): Signer = {
    val config = JcaSignerSettings("SecretKey")

    new JcaSigner(config)
  }

  @Provides
  @Named("authenticator-crypter")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {

    val config = JcaCrypterSettings("SecretKey")

    new JcaCrypter(config)
  }

  @Provides
  def providesSilhouetteComponents(
      components: DefaultSilhouetteControllerComponents[DefaultEnv]): SilhouetteControllerComponents[DefaultEnv] =
    components
}
