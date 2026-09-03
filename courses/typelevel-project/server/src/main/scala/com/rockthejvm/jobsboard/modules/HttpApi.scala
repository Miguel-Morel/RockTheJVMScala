package com.rockthejvm.jobsboard.modules

import cats.data.OptionT
import cats.effect.{Async, Concurrent, Ref, Resource, Sync}
import cats.syntax.all.*
import cats.{Monad, MonadThrow}
import com.rockthejvm.jobsboard.config.SecurityConfig
import com.rockthejvm.jobsboard.core.{LiveAuth, Users}
import com.rockthejvm.jobsboard.domain.security.{Authenticator, JwtToken, SecuredHandler}
import com.rockthejvm.jobsboard.domain.user.User
import com.rockthejvm.jobsboard.http.routes.*
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import tsec.authentication.{BackingStore, IdentityStore, JWTAuthenticator, SecuredRequestHandler}
import tsec.common.SecureRandomId
import tsec.mac.jca.HMACSHA256

class HttpApi[F[_]: Concurrent: Logger] private (core: Core[F], authenticator: Authenticator[F]){
  given securedHandler: SecuredHandler[F] = SecuredRequestHandler(authenticator)
  private val healthRoutes = HealthRoutes[F].routes
  private val jobRoutes = JobRoutes[F](core.jobs).routes
  private val authRoutes = AuthRoutes[F](core.auth, authenticator).routes
  
  val endpoints = Router(
    "/api" -> (healthRoutes <+> jobRoutes <+> authRoutes)
  )

}

object HttpApi {


  def createAuthenticator[F[_]: Sync](users: Users[F], securityConfig: SecurityConfig): F[Authenticator[F]] = {
      // 1. identity store: String => OptionT[F, User]
      val idStore: IdentityStore[F, String, User] = (email: String) => {
        OptionT(users.find(email))
      }

      // 2. backing store for JWT tokens: BackingStore[F, id, JwtToken]
      val tokenStoreF = Ref.of[F, Map[SecureRandomId, JwtToken]](Map.empty).map { ref =>
        new BackingStore[F, SecureRandomId, JwtToken] {
          // mutable map -> race conditions
          // use ref instead which is atomic
          override def get(id: SecureRandomId): OptionT[F, JwtToken] =
            OptionT(/*F[JwtToken]*/ ref.get.map(_.get(id)))

          override def put(elem: JwtToken): F[JwtToken] =
            ref.modify(store => (store + (elem.id -> elem), elem))

          override def update(v: JwtToken): F[JwtToken] =
            put(v)

          override def delete(id: SecureRandomId): F[Unit] =
            ref.modify(store => (store - id, ()))
        }
      }

      // 3. hashing key
      // TODO move to config
      val keyF = HMACSHA256.buildKey[F](securityConfig.secret.getBytes("UTF-8"))


      for {
        key <- keyF
        tokenStore <- tokenStoreF
        // 4. authenticator
        // jwt authenticator(key, identity store)
      } yield JWTAuthenticator.backed.inBearerToken(
          // expiry of tokens, max idle, idStore, key
          expiryDuration = securityConfig.jwtExpiryDuration, // expiration of tokens
          maxIdle = None, // max idle time(optional)
          identityStore = idStore, // identity store
          tokenStore = tokenStore,
          signingKey = key // hash key
        )
  }


  def apply[F[_]: Async: Logger](core: Core[F], securityConfig: SecurityConfig): Resource[F, HttpApi[F]]= {
    Resource
      .eval(createAuthenticator(core.users, securityConfig))
      .map(authenticator => new HttpApi[F](core, authenticator))
  }
}
