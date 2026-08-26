package com.rockthejvm.jobsboard.fixtures

import com.rockthejvm.jobsboard.domain.user.*

trait UsersFixture {
  val daniel = User(
  "daniel@rockthejvm.com",
  "rockthejvm",
  Some("daniel"),
  Some("ciocirlan"),
  Some("rock the jvm"),
  Role.ADMIN
  )
  
  val ricardo = User (
  "ricardo@rockthejvm.com",
  "ricardorulez",
  Some("ricardo"),
  Some("cardin"),
  Some("rock the jvm"),
  Role.RECRUITER
  )

  val newUser = User(
    "newuser@gmail.com",
    "simplepassword",
    Some("john"),
    Some("doe"),
    Some("some company"),
    Role.RECRUITER
  )

  val updatedRicardo = User(
    "ricardo@rockthejvm.com",
    "ricardorulez",
    Some("RICARDO"),
    Some("CARDIN"),
    Some("TEMU"),
    Role.RECRUITER
  )

}
