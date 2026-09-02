package com.rockthejvm.jobsboard.http.routes

import cats.Apply.ops.toAllApplyOps
import cats.FlatMap.nonInheritedOps.toFlatMapOps
import cats.MonoidK.ops.toAllMonoidKOps
import cats.effect.Concurrent
import cats.syntax.all.catsSyntaxSemigroup
import com.rockthejvm.jobsboard.core.Auth
import com.rockthejvm.jobsboard.domain.auth.{LoginInfo, NewPasswordInfo}
import com.rockthejvm.jobsboard.domain.security.{AuthRoute, JwtToken, adminOnly, allRoles, restrictedTo}
import com.rockthejvm.jobsboard.domain.user.{NewUserInfo, User}
import com.rockthejvm.jobsboard.http.responses.FailureResponse
import com.rockthejvm.jobsboard.http.validation.syntax.HttpValidationDsl
import org.http4s.{HttpRoutes, Response, Status}
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import tsec.authentication.{SecuredRequestHandler, TSecAuthService, asAuthed}
import scala.language.implicitConversions

class AuthRoutes[F[_] : Concurrent: Logger] private (auth: Auth[F]) extends HttpValidationDsl[F] {

  private val authenticator = auth.authenticator

  private val securedHandler: SecuredRequestHandler[F, String, User, JwtToken] = SecuredRequestHandler(authenticator)

  // POST /auth/login { LoginInfo } => 200 OK with Authorization: Bearer {jwt}
  private val loginRoute: HttpRoutes[F] =  HttpRoutes.of[F] {
    case req @ POST -> Root  / "login" =>
      req.validate[LoginInfo] { loginInfo =>
        val maybeJwtToken = for {
          maybeToken <- auth.login(loginInfo.email, loginInfo.password)
          _ <- Logger[F].info(s"user logging in: ${loginInfo.email}")
        } yield maybeToken

        maybeJwtToken.map {
          case Some(token) => authenticator.embed(Response(Status.Ok), token) // Authorization: Bearer
          case None => Response(Status.Unauthorized)
        }
      }
  }

  // POST /auth/users { NewUserInfo } = 201 Created or BadRequest
  private val createUserRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root  / "users" =>
      req.validate[NewUserInfo] { newUserInfo =>
        for {
          maybeNewUser <- auth.signUp(newUserInfo)
          resp <- maybeNewUser match {
            case Some(user) => Created(user.email)
            case None => BadRequest(s"user with email ${newUserInfo.email} already exists")
          }
        } yield resp
      }
  }

  // PUT /auth/users/password { NewPasswordInfo } { Authorization: Bearer {jwt} } => 200 OK
  private val changePasswordRoute: AuthRoute[F] = {
    case req @ PUT -> Root  / "users" / "password" asAuthed user =>
      req.request.validate[NewPasswordInfo] { newPasswordInfo =>
        for {
          maybeUserOrError <- auth.changePassword(user.email, newPasswordInfo)
          resp <- maybeUserOrError match {
            case Right(Some(_)) => Ok()
            case Right(None) => NotFound(FailureResponse(s"user ${user.email} not found"))
            case Left(_) => Forbidden()
          }
        } yield resp
      }
  }

  // POST /auth/logout { Authorization: Bearer {jwt} } => 200 OK
  private val logoutRoute: AuthRoute[F] = {
    case req @ POST -> Root  / "logout" asAuthed _ =>
      val token = req.authenticator
      for {
        _ <- authenticator.discard(token)
        resp <- Ok()
      } yield resp
  }

  // DELETE /auth/users/daniel@rockthejvm.com
  private val deleteUserRoute: AuthRoute[F] = {
    case req @ DELETE-> Root  / "users" / email asAuthed user =>
      // auth - delete user
      auth.delete(email).flatMap {
        case true => Ok()
        case false => NotFound()
      }
  }

  val unauthedRoutes = loginRoute <+> createUserRoute
  val authedRoutes = securedHandler.liftService(
     changePasswordRoute.restrictedTo(allRoles) |+|
     logoutRoute.restrictedTo(allRoles) |+|
     deleteUserRoute.restrictedTo(adminOnly)
//    TSecAuthService(changePasswordRoute.orElse(logoutRoute).orElse(deleteUserRoute))
  )

  val routes = Router(
    "/auth" -> (unauthedRoutes <+> authedRoutes)
  )

}

object AuthRoutes {
  def apply[F[_] : Concurrent: Logger](auth: Auth[F]) = new AuthRoutes[F](auth)
}
