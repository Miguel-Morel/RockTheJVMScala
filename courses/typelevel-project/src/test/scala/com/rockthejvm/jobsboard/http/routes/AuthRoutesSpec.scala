package com.rockthejvm.jobsboard.http.routes

import cats.data.OptionT
import cats.effect.{IO, Ref}
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits.toTraverseOps
import com.rockthejvm.jobsboard.core.Auth
import com.rockthejvm.jobsboard.domain.auth
import com.rockthejvm.jobsboard.domain.auth.{ForgotPasswordInfo, LoginInfo, NewPasswordInfo, RecoverPasswordInfo}
import com.rockthejvm.jobsboard.domain.security.{Authenticator, JwtToken}
import com.rockthejvm.jobsboard.domain.user.{NewUserInfo, User}
import com.rockthejvm.jobsboard.fixtures.{SecuredRouteFixture, UserFixture}
import org.http4s.{AuthScheme, Credentials, HttpRoutes, Method, Request, Status}
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.uri
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.headers.Authorization
import org.typelevel.ci.CIStringSyntax
import tsec.authentication.{IdentityStore, JWTAuthenticator}
import tsec.jws.mac.JWTMac
import tsec.mac.jca.HMACSHA256

import scala.concurrent.duration.DurationInt

class AuthRoutesSpec
  extends AsyncFreeSpec
  with AsyncIOSpec
  with Matchers
  with Http4sDsl[IO]
  with UserFixture
  with SecuredRouteFixture {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // prep
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  val mockedAuth: Auth[IO] = probedAuth(None)

  def probedAuth(userMap: Option[Ref[IO, Map[String, String]]]): Auth[IO] = new Auth[IO] {

    def login(email: String, password: String): IO[Option[User]] =
      if(email == danielEmail && password == danielPassword)
        IO(Some(daniel))
      else
        IO.pure(None)

    def signUp(newUserInfo: NewUserInfo): IO[Option[User]] =
      if(newUserInfo.email == ricardoEmail)
        IO.pure(Some(ricardo))
      else
        IO.pure(None)

    def changePassword(email: String, newPasswordInfo: NewPasswordInfo): IO[Either[String, Option[User]]] =
      if(email == danielEmail)
        if(newPasswordInfo.oldPassword == danielPassword)
          IO.pure(Right(Some(daniel)))
        else
          IO.pure(Left("invalid password"))
      else
      IO.pure(Right(None))

    override def delete(email: String): IO[Boolean] = IO.pure(true)

    // allow password recovery
    override def sendPasswordRecoveryToken(email: String): IO[Unit] =
      userMap.traverse { userMapRef =>
        userMapRef.modify { userMap =>
          (userMap + (email -> "abc123"), ())
        }
      }.map(_ => ())

    override def recoverPasswordFromToken(email: String, token: String, newPassword: String): IO[Boolean] =
      userMap.traverse { userMapRef =>
        userMapRef.get.map { userMap =>
          userMap.get(email).filter(_ == token) // Option[String]
        } // IO[Option[String]]
          .map(_.nonEmpty) // IO[Boolean]
      } // IO[Option[Boolean]]
        .map(_.getOrElse(false))


  }

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val authRoutes: HttpRoutes[IO] = AuthRoutes[IO](mockedAuth, mockedAuthenticator).routes


  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // tests
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  "AuthRoutes" - {
    "should return a 401 - Unauthorized if login fails" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/login")
        .withEntity(LoginInfo(danielEmail, "wrongpassword"))
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Unauthorized
      }
    }

    "should return a 200 - OK + a JWT if login is successful" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/login")
            .withEntity(LoginInfo(danielEmail, danielPassword))
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Ok
        response.headers.get(ci"Authorization") shouldBe defined
      }
    }

    "should return a 400 - BadRequest if the user being created already exists" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/users")
            .withEntity(newUserDaniel)
        )
      } yield {
        // assertions here
        response.status shouldBe Status.BadRequest
      }
    }

    "should return a 201 - Created if the user creation succeeds" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/users")
            .withEntity(newUserRicardo)
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Created
      }
    }

    "should return a 200 - OK if logging out with a valid JWT token" in {
      for {
        jwtToken <- mockedAuthenticator.create(danielEmail)
        response <- authRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/logout")
            .withBearerToken(jwtToken)
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Ok
      }
    }

    "should return a 401 - Unauthorized if logging out without a valid JWT token" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/logout")
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Unauthorized
      }
    }


    "should return a 404 - Not Found if changing password for user that doesn't exist" in {
      for {
        jwtToken <- mockedAuthenticator.create(ricardoEmail)
        response <- authRoutes.orNotFound.run(
          Request(method = Method.PUT, uri = uri"/auth/users/password")
            .withBearerToken(jwtToken)
            .withEntity(NewPasswordInfo(ricardoPassword, "newpassword"))
        )
      } yield {
        // assertions here
        response.status shouldBe Status.NotFound
      }
    }

    "should return a 403 - Forbidden if old password is incorrect" in {
      for {
        jwtToken <- mockedAuthenticator.create(danielEmail)
        response <- authRoutes.orNotFound.run(
          Request(method = Method.PUT, uri = uri"/auth/users/password")
            .withBearerToken(jwtToken)
            .withEntity(NewPasswordInfo("wrongpassword", "newpassword"))
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Forbidden
      }
    }

    "should return a 401 - Unauthorized if changing password without a JWT" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request(method = Method.PUT, uri = uri"/auth/users/password")
            .withEntity(danielPassword, "newpassword")
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Unauthorized
      }
    }

    "should return a 200 - Ok if changing password for a user with a valid JWT and password" in {
      for {
        jwtToken <- mockedAuthenticator.create(danielEmail)
        response <- authRoutes.orNotFound.run(
          Request(method = Method.PUT, uri = uri"/auth/users/password")
            .withBearerToken(jwtToken)
            .withEntity(NewPasswordInfo(danielPassword, "newpassword"))
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Ok
      }
    }

    "should return a 401 - Unauthorized if a non-admin tries to delete an user" in {
      for {
        jwtToken <- mockedAuthenticator.create(ricardoEmail)
        response <- authRoutes.orNotFound.run(
          Request(method = Method.DELETE, uri = uri"/auth/users/daniel@rockthejvm.com")
            .withBearerToken(jwtToken)
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Unauthorized
      }
    }

    "should return a 200 - Ok if an admin tries to delete an user" in {
      for {
        jwtToken <- mockedAuthenticator.create(danielEmail)
        response <- authRoutes.orNotFound.run(
          Request(method = Method.PUT, uri = uri"/auth/users/password")
            .withBearerToken(jwtToken)
            .withEntity(NewPasswordInfo(danielPassword, "newpassword"))
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Ok
      }
    }

    "should return a 200 - Ok when resetting a password and an email should be triggered" in {
      for {
        userMapRef <- Ref.of[IO, Map[String, String]](Map())
        auth <- IO(probedAuth(Some(userMapRef)))
        routes <- IO(AuthRoutes(auth, mockedAuthenticator).routes)
        response <- routes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/reset")
            .withEntity(ForgotPasswordInfo(danielEmail))
        )

        userMap <- userMapRef.get
      } yield {
        // assertions here
        response.status shouldBe Status.Ok
        userMap should contain key(danielEmail)
      }
    }

    "should return a 200 - Ok when recovering a password for a valid user + token" in {
      for {
        userMapRef <- Ref.of[IO, Map[String, String]](Map(danielEmail -> "abc123"))
        auth <- IO(probedAuth(Some(userMapRef)))
        routes <- IO(AuthRoutes(auth, mockedAuthenticator).routes)
        response <- routes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/recover")
            .withEntity(RecoverPasswordInfo(danielEmail, "abc123", "rockthejvm"))
        )

        userMap <- userMapRef.get
      } yield {
        // assertions here
        response.status shouldBe Status.Ok
      }
    }

    "should return a 403 - Forbidden when recovering a password for an invalid user + token" in {
      for {
        userMapRef <- Ref.of[IO, Map[String, String]](Map(danielEmail -> "abc123"))
        auth <- IO(probedAuth(Some(userMapRef)))
        routes <- IO(AuthRoutes(auth, mockedAuthenticator).routes)
        response <- routes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/recover")
            .withEntity(RecoverPasswordInfo(danielEmail, "wrongoken", "rockthejvm"))
        )

        userMap <- userMapRef.get
      } yield {
        // assertions here
        response.status shouldBe Status.Forbidden
      }
    }

  }
}
