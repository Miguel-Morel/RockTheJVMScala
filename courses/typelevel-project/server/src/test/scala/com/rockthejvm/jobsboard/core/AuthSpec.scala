package com.rockthejvm.jobsboard.core

import cats.data.OptionT
import cats.effect.{IO, Ref}
import cats.effect.testing.scalatest.AsyncIOSpec
import com.rockthejvm.jobsboard.config.SecurityConfig
import com.rockthejvm.jobsboard.domain.auth.NewPasswordInfo
import com.rockthejvm.jobsboard.domain.security.Authenticator
import com.rockthejvm.jobsboard.domain.user
import com.rockthejvm.jobsboard.domain.user.{NewUserInfo, Role, User}
import com.rockthejvm.jobsboard.fixtures.UserFixture
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import tsec.authentication.{IdentityStore, JWTAuthenticator}
import tsec.mac.jca.HMACSHA256
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.BCrypt

import scala.concurrent.duration.DurationInt

class AuthSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with UserFixture{

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]


  
  val mockedConfig = SecurityConfig("secret", 1.day)
  
  val mockedTokens: Tokens[IO] = new Tokens[IO] {
    override def getToken(email: String): IO[Option[String]] =
      if(email == danielEmail) IO.pure(Some("abc123"))
      else IO.pure(None)

    override def checkToken(email: String, token: String): IO[Boolean] =
      IO.pure(token == "abc123")
  }
  
  val mockedEmails: Emails[IO] = new Emails[IO] {
    override def sendEmail(to: String, subject: String, content: String): IO[Unit] =
      IO.unit

    override def sendPasswordRecoveryEmail(to: String, token: String): IO[Unit] =
      IO.unit
  }

  def probedEmails(users: Ref[IO, Set[String]]): Emails[IO] = new Emails[IO] {
    override def sendEmail(to: String, subject: String, content: String): IO[Unit] =
      users.modify(set => (set + to, ()))

    override def sendPasswordRecoveryEmail(to: String, token: String): IO[Unit] =
      sendEmail(to, "your token", "token")
  }

  "Auth 'algebra" - {
    "login should return None if the user doesn't exist" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedTokens, mockedEmails)
        maybeToken <- auth.login("user@rockthejvm.com", "password")
      } yield maybeToken

      program.asserting(_ shouldBe None)
    }

    "login should return None if the user exists but the password is wrong" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedTokens, mockedEmails)
        maybeToken <- auth.login(danielEmail, "wrongpassword")
      } yield maybeToken

      program.asserting(_ shouldBe None)
    }

    "login should return a token if the user exists and the password is correct" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedTokens, mockedEmails)
        maybeToken <- auth.login(danielEmail, "rockthejvm")
      } yield maybeToken

      program.asserting(_ shouldBe defined)
    }

    "signing up should not create an user with an existing email" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedTokens, mockedEmails)
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
        auth <- LiveAuth[IO](mockedUsers, mockedTokens, mockedEmails)
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
        auth <- LiveAuth[IO](mockedUsers, mockedTokens, mockedEmails)
        result <- auth.changePassword("alice@rockthejvm.com", NewPasswordInfo("oldpw", "newpw"))
      } yield result

      program.asserting(_ shouldBe Right(None))
    }

    "changePassword should return a Left(error) if the user exists and the password is incorrect" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedTokens, mockedEmails)
        result <- auth.changePassword(danielEmail, NewPasswordInfo("oldpw", "newpw"))
      } yield result

      program.asserting(_ shouldBe Left("invalid password"))
    }

    "changePassword should update password if all details are correct" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedTokens, mockedEmails)
        result <- auth.changePassword(danielEmail, NewPasswordInfo("rockthejvm", "scalarocks"))
        isNicePassword <- result match {
          case Right(Some(user)) =>
              BCrypt.checkpwBool[IO]("scalarocks", PasswordHash[BCrypt](user.hashedPassword))
          case _ =>
            IO.pure(false)
        }
      } yield isNicePassword

      program.asserting(_ shouldBe true)
    }

    "recoverPassword should fail for a user that does not exist, even if the token is correct" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedTokens, mockedEmails)
        result1 <- auth.recoverPasswordFromToken("someone@gmail.com", "abc123", "gotcha")
        result2 <- auth.recoverPasswordFromToken("someone@gmail.com", "wrongtoken", "gotcha")
      } yield (result1, result2)

      program.asserting(_ shouldBe (false, false))
    }

    "recoverPassword should fail for a user that does exist but has an invalid token" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedTokens, mockedEmails)
        result <- auth.recoverPasswordFromToken(danielEmail, "wrongtoken", "h4ck3d")
      } yield result

      program.asserting(_ shouldBe false)
    }

    "recoverPassword should succeed for a valid user + token" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedTokens, mockedEmails)
        result <- auth.recoverPasswordFromToken(danielEmail, "abc123", "rockstar")
      } yield result

      program.asserting(_ shouldBe true)
    }

    "sending recovery passwords should fail for a user that doesn't exist" in {
      val program = for {
        set <- Ref.of[IO, Set[String]](Set())
        emails <- IO(probedEmails(set))
        auth <- LiveAuth[IO](mockedUsers, mockedTokens, emails)
        result <- auth.sendPasswordRecoveryToken("someone@whatever.com")
        usersBeingSentEmails <- set.get
      } yield usersBeingSentEmails

      program.asserting(_ shouldBe empty)
    }

    "sending recovery passwords should succeed for a user that exists" in {
      val program = for {
        set <- Ref.of[IO, Set[String]](Set())
        emails <- IO(probedEmails(set))
        auth <- LiveAuth[IO](mockedUsers, mockedTokens, emails)
        result <- auth.sendPasswordRecoveryToken(danielEmail)
        usersBeingSentEmails <- set.get
      } yield usersBeingSentEmails

      program.asserting(_ should contain(danielEmail))
    }



  }


}
