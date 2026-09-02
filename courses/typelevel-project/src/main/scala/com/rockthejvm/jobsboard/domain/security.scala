package com.rockthejvm.jobsboard.domain

import cats.*
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxSemigroup}
import com.rockthejvm.jobsboard.domain.user.{Role, User}
import doobie.WeakAsync.doobieWeakAsyncForAsync
import org.http4s.{Response, Status}
import tsec.authentication.{AugmentedJWT, JWTAuthenticator, SecuredRequest, SecuredRequestHandler, TSecAuthService}
import tsec.authorization.*
import tsec.mac.jca.HMACSHA256

object security {
  type Crypto = HMACSHA256
  type JwtToken = AugmentedJWT[HMACSHA256, String]
  type Authenticator[F[_]] = JWTAuthenticator[F, String, User, Crypto]
  type AuthRoute[F[_]] = PartialFunction[SecuredRequest[F, User, JwtToken], F[Response[F]]]
  // type aliases for http routes
  type AuthRBAC[F[_]] = BasicRBAC[F, Role, User, JwtToken]
  type SecuredHandler[F[_]] = SecuredRequestHandler[F, String, User, JwtToken]

  // RBAC
  // BasicRBAC[F, Role, User, JwtToken]

  given authRole[F[_]: Applicative]: AuthorizationInfo[F, Role, User] with {
    override def fetchInfo(u: User): F[Role] = u.role.pure[F]
  }


  def allRoles[F[_]: MonadThrow]: AuthRBAC[F] =
    BasicRBAC.all[F, Role, User, JwtToken]

  def recruiterOnly[F[_] : MonadThrow]: AuthRBAC[F] = 
    BasicRBAC(Role.RECRUITER)

  def adminOnly[F[_]: MonadThrow]: AuthRBAC[F] =
    BasicRBAC(Role.ADMIN)

  // authorization
  case class Authorizations[F[_]](rbacRoutes: Map[AuthRBAC[F], List[AuthRoute[F]]])

  object Authorizations {
    given combiner[F[_]]: Semigroup[Authorizations[F]] = Semigroup.instance { (authA, authB) =>
      Authorizations(authA.rbacRoutes |+| authB.rbacRoutes)
    }
  }

  
  // AuthRoute -> Authorizations -> TSecAuthService -> HttpRoute
  
  // 1. AuthRoute -> Authorizations = .restrictedTo extension method
  extension [F[_]] (authRoute: AuthRoute[F])
    def restrictedTo(rbac: AuthRBAC[F]): Authorizations[F] =
      Authorizations(Map(rbac -> List(authRoute)))
      
  // 2. Authorizations -> TSecAuthService = implicit conversion
  given auth2tsec [F[_]: Monad]: Conversion[Authorizations[F], TSecAuthService[User, JwtToken, F]] =
    authz => {
      // this always responds with 401
      val unauthorizedService: TSecAuthService[User, JwtToken, F] = 
        TSecAuthService[User, JwtToken, F] { _ =>
          Response[F](Status.Unauthorized).pure[F]
      }

//      val rbac: AuthRBAC[F] = ???
//      val authRoute: AuthRoute[F] = ???
//      val tsec = TSecAuthService.withAuthorizationHandler(rbac)(authRoute, unauthorizedService.run)
      
      authz.rbacRoutes // map[RBAC, List[AuthRoutes[F]]]
        .toSeq
        .foldLeft(unauthorizedService) {
          case (acc, (rbac, routes)) => 
            // merge routes into one
            val bigRoute = routes.reduce(_.orElse(_))
            // build a new service, fall back to the acc if rabc/route fails
            TSecAuthService.withAuthorizationHandler(rbac)(bigRoute, acc.run)
        }
    }

  // 3. semigroup for Authorization
}
