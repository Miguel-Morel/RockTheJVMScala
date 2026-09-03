package com.rockthejvm.jobsboard.playground

import cats.effect.{IO, IOApp}
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.BCrypt

object PasswordHashingPlayground extends IOApp.Simple {
  override def run: IO[Unit] =
    //    BCrypt.hashpw[IO]("scalarocks").flatMap(IO.println) *>
    //    BCrypt.checkpwBool[IO]("scalarocks", PasswordHash[BCrypt]("$2a$10$eAuYqQhZ1eGIKk/MIY.N3e0wqV2mly9aSf8.LiJbU95TviVQO41uy")).flatMap(IO.println)


    BCrypt.hashpw[IO]("rockthejvm").flatMap(IO.println) *>
    BCrypt.hashpw[IO]("ricardorulez").flatMap(IO.println) *>
    BCrypt.hashpw[IO]("simplepassword").flatMap(IO.println) *>
    BCrypt.hashpw[IO]("ricardorocks").flatMap(IO.println)
}