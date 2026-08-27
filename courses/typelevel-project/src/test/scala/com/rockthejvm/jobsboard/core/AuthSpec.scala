package com.rockthejvm.jobsboard.core

import cats.data.OptionT
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.rockthejvm.jobsboard.domain.auth.NewPassWordInfo
import com.rockthejvm.jobsboard.domain.security.Authenticator
import com.rockthejvm.jobsboard.domain.user
import com.rockthejvm.jobsboard.domain.user.{NewUserInfo, Role, User}
import com.rockthejvm.jobsboard.fixtures.UsersFixture
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import tsec.authentication.{IdentityStore, JWTAuthenticator}
import tsec.mac.jca.HMACSHA256
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.BCrypt

import scala.concurrent.duration.DurationInt

class AuthSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with UsersFixture{

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private val mockedUsers: Users[IO] = new Users[IO] {
    override def find(email: String): IO[Option[User]] =
      if(email == danielEmail) IO.pure(Some(daniel))
      else IO.pure(None)

    override def create(user: User): IO[String] =
      IO.pure(user.email)

    override def update(user: User): IO[Option[User]] =
      IO.pure(Some(user))

    override def delete(email: String): IO[Boolean] =
      IO.pure(true)
  }

  val mockedAuthenticator: Authenticator[IO] = {
    // key for hashing
    val key = HMACSHA256.unsafeGenerateKey
    // identity store to retrieve users
    val idStore: IdentityStore[IO, String, User] = (email: String) =>
      if(email == danielEmail) OptionT.pure(daniel)
      else if(email == ricardoEmail) OptionT.pure(ricardo)
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

  "Auth 'algebra" - {
    "login should return None if the user doesn't exist" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeToken <- auth.login("user@rockthejvm.com", "password")
      } yield maybeToken

      program.asserting(_ shouldBe None)
    }

    "login should return None if the user exists but the password is wrong" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeToken <- auth.login(danielEmail, "wrongpassword")
      } yield maybeToken

      program.asserting(_ shouldBe None)
    }

    "login should return a token if the user exists and the password is correct" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeToken <- auth.login(danielEmail, "rockthejvm")
      } yield maybeToken

      program.asserting(_ shouldBe defined)
    }

    "signing up should not create an user with an existing email" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeUser <- auth.signUp(
          NewUserInfo(
            danielEmail,
            "password",
            Some("daniel"),
            Some("ciocirlan"),
            Some("other company")
          )
        )
      } yield maybeUser

      program.asserting(_ shouldBe None)
    }

    "signing up should create a new user" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeUser <- auth.signUp(
          NewUserInfo(
            "bob@rockthejvm.com",
            "anotherpassword",
            Some("john"),
            Some("doe"),
            Some("another company")
          )
        )
      } yield maybeUser

      program.asserting {
        case Some(user) =>
          user.email shouldBe "bob@rockthejvm.com"
          user.firstName shouldBe Some("john")
          user.lastName shouldBe Some("doe")
          user.company shouldBe Some("another company")
          user.role shouldBe Role.RECRUITER
        case _ =>
          fail()
      }
    }

    "changePassword should return a Right(None) if the user doesn't exist" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        result <- auth.changePassword("alice@rockthejvm.com", NewPassWordInfo("oldpw", "newpw"))
      } yield result

      program.asserting(_ shouldBe Right(None))
    }

    "changePassword should return a Left(error) if the user exists and the password is incorrect" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        result <- auth.changePassword(danielEmail, NewPassWordInfo("oldpw", "newpw"))
      } yield result

      program.asserting(_ shouldBe Left("invalid password"))
    }

    "changePassword should update password if all details are correct" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        result <- auth.changePassword(danielEmail, NewPassWordInfo("rockthejvm", "scalarocks"))
        isNicePassword <- result match {
          case Right(Some(user)) =>
              BCrypt.checkpwBool[IO]("scalarocks", PasswordHash[BCrypt](user.hashedPassword))
          case _ =>
            IO.pure(false)
        }
      } yield isNicePassword

      program.asserting(_ shouldBe true)
    }

  }


}
