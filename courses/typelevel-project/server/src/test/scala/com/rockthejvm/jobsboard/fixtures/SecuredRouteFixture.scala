package com.rockthejvm.jobsboard.fixtures

import cats.data.OptionT
import cats.effect.IO
import com.rockthejvm.jobsboard.domain.security.{Authenticator, JwtToken, SecuredHandler}
import com.rockthejvm.jobsboard.domain.user.User
import org.http4s.{AuthScheme, Credentials, Request}
import org.http4s.headers.Authorization
import tsec.authentication.{IdentityStore, JWTAuthenticator, SecuredRequestHandler}
import tsec.jws.mac.JWTMac
import tsec.mac.jca.HMACSHA256

import scala.concurrent.duration.DurationInt

trait SecuredRouteFixture extends UserFixture {
  val mockedAuthenticator: Authenticator[IO] = {
    // key for hashing
    val key = HMACSHA256.unsafeGenerateKey
    // identity store to retrieve users
    val idStore: IdentityStore[IO, String, User] = (email: String) =>
      if (email == danielEmail) OptionT.pure(daniel)
      else if (email == ricardoEmail) OptionT.pure(ricardo)
      else OptionT.none[IO, User]
    // jwt authenticator(key, identity store)
    JWTAuthenticator.unbacked.inBearerToken(
      // expiry of tokens, max idle, idStore, key
      1.day, // expiration of tokens
      None, // max idle time(optional)
      idStore, // identity store
      key // hash key
    )
  }

  extension (r: Request[IO])
    def withBearerToken(a: JwtToken): Request[IO] =
      r.putHeaders {
        val jwtString = JWTMac.toEncodedString[IO, HMACSHA256](a.jwt)
        // Authorization: Bearer {jwt}
        Authorization(Credentials.Token(AuthScheme.Bearer, jwtString))
      }
      
  given securedHandler: SecuredHandler[IO] = SecuredRequestHandler(mockedAuthenticator)

}
