package com.rockthejvm.jobsboard.fixtures

import cats.effect.IO
import com.rockthejvm.jobsboard.core.Users
import com.rockthejvm.jobsboard.domain.user.*

/*
  rockthejvm => $2a$10$32S4CnVVXV8oANEWcxby0.KRCrmaIwfkzVLMlS8dyD/Y7VFkQuHzG
  ricardorulez => $2a$10$W88WykuhIfAuA3JBXhG.KOf0DR8yqUUOWmPnd7pdQWic/7AK6f8lW
  simplepassword => $2a$10$SvF.lMjwjaAEwpv6xZHlauSZPoszYYrBocqGzHB8/WsdeGRRU91iu
  ricardorocks => $2a$10$GmOA69MKb7NDzZ514eDYoulpuqMnKWXAdRxFzr2WXp/mXwvVmCMky
 */

trait UserFixture {

  val mockedUsers: Users[IO] = new Users[IO] {
    override def find(email: String): IO[Option[User]] =
      if (email == danielEmail) IO.pure(Some(daniel))
      else IO.pure(None)

    override def create(user: User): IO[String] =
      IO.pure(user.email)

    override def update(user: User): IO[Option[User]] =
      IO.pure(Some(user))

    override def delete(email: String): IO[Boolean] =
      IO.pure(true)
  }
  
  val daniel = User(
  "daniel@rockthejvm.com",
  "$2a$10$32S4CnVVXV8oANEWcxby0.KRCrmaIwfkzVLMlS8dyD/Y7VFkQuHzG",
  Some("daniel"),
  Some("ciocirlan"),
  Some("rock the jvm"),
  Role.ADMIN
  )

  val danielEmail = daniel.email
  val danielPassword = "rockthejvm"
  
  val ricardo = User (
  "ricardo@rockthejvm.com",
  "$2a$10$W88WykuhIfAuA3JBXhG.KOf0DR8yqUUOWmPnd7pdQWic/7AK6f8lW",
  Some("ricardo"),
  Some("cardin"),
  Some("rock the jvm"),
  Role.RECRUITER
  )

  val ricardoEmail = ricardo.email
  val ricardoPassword = "ricardorulez"

  val newUser = User(
    "newuser@gmail.com",
    "$2a$10$SvF.lMjwjaAEwpv6xZHlauSZPoszYYrBocqGzHB8/WsdeGRRU91iu",
    Some("john"),
    Some("doe"),
    Some("some company"),
    Role.RECRUITER
  )

  val updatedRicardo = User(
    "ricardo@rockthejvm.com",
    "$2a$10$GmOA69MKb7NDzZ514eDYoulpuqMnKWXAdRxFzr2WXp/mXwvVmCMky",
    Some("RICARDO"),
    Some("CARDIN"),
    Some("TEMU"),
    Role.RECRUITER
  )

  val newUserDaniel = NewUserInfo(
    danielEmail,
    danielPassword,
    Some("daniel"),
    Some("ciocirlan"),
    Some("rock the jvm"),
  )

  val newUserRicardo = NewUserInfo(
    ricardoEmail,
    ricardoPassword,
    Some("ricardo"),
    Some("cardin"),
    Some("rock the jvm"),
  )

}
