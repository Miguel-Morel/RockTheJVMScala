package com.rockthejvm.jobsboard.core

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.rockthejvm.jobsboard.domain.user.*
import com.rockthejvm.jobsboard.fixtures.UsersFixture
import doobie.implicits.{toConnectionIOOps, toSqlInterpolator}
import org.postgresql.util.PSQLException
import org.scalatest.Inside
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class UsersSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with Inside with DoobieSpec with UsersFixture {
  override val initScript: String = "sql/users.sql"

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]


  "Users 'algebra'" - {
    "should retrieve a user by email" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
          retrieved <- users.find("ricardo@rockthejvm.com")
        } yield retrieved

        program.asserting(_ shouldBe Some(ricardo))
      }
    }

    "should return None if the email doesn't exist" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
          retrieved <- users.find("notfound@rockthejvm.com")
        } yield retrieved

        program.asserting(_ shouldBe None)
      }
    }

    "should create a new user" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
          userId <- users.create(newUser)
          maybeUser <- sql"SELECT * FROM users WHERE email = ${newUser.email}"
            .query[User]
            .option
            .transact(xa)
        } yield (userId, maybeUser)

        program.asserting {
          case (userId, maybeUser) =>
            userId shouldBe newUser.email
            maybeUser shouldBe Some(newUser)
        }
      }
    }

    "should fail creating a new user if the email already exists" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
          userId <- users.create(daniel).attempt // IO[Either[Throwable, String]]
        } yield userId

        program.asserting { outcome =>
          inside(outcome) {
            case Left(e) => e shouldBe a[PSQLException]
            case _ => fail()
          }

        }
      }
    }

    "should return None when updating a user that doesn't exist" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
          maybeUser <- users.update(newUser)
        } yield maybeUser

        program.asserting(_ shouldBe None)
      }
    }

    "should update an existing user" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
          maybeUser <- users.update(updatedRicardo)
        } yield maybeUser

        program.asserting(_ shouldBe Some(updatedRicardo))
      }
    }

    "should delete a user" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
          result <- users.delete("daniel@rockthejvm.com")
          maybeUser <-     sql"SELECT * FROM users WHERE email = 'daniel@rockthejvm.com'"
            .query[User]
            .option
            .transact(xa)
        } yield (result, maybeUser)

        program.asserting {
          case (result, maybeUser) =>
            result shouldBe true
            maybeUser shouldBe None
        }
      }
    }

    "should not delete an user that does not exist" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
          result <- users.delete("nobody@rockthejvm.com")
        } yield result

        program.asserting(_ shouldBe false)
      }
    }
  }
}


