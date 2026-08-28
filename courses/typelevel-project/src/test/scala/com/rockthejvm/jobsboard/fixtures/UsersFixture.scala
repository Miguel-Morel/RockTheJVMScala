package com.rockthejvm.jobsboard.fixtures

import com.rockthejvm.jobsboard.domain.user.*

/*
  rockthejvm => $2a$10$32S4CnVVXV8oANEWcxby0.KRCrmaIwfkzVLMlS8dyD/Y7VFkQuHzG
  ricardorulez => $2a$10$W88WykuhIfAuA3JBXhG.KOf0DR8yqUUOWmPnd7pdQWic/7AK6f8lW
  simplepassword => $2a$10$SvF.lMjwjaAEwpv6xZHlauSZPoszYYrBocqGzHB8/WsdeGRRU91iu
  ricardorocks => $2a$10$GmOA69MKb7NDzZ514eDYoulpuqMnKWXAdRxFzr2WXp/mXwvVmCMky
 */

trait UsersFixture {
  val daniel = User(
  "daniel@rockthejvm.com",
  "$2a$10$32S4CnVVXV8oANEWcxby0.KRCrmaIwfkzVLMlS8dyD/Y7VFkQuHzG",
  Some("daniel"),
  Some("ciocirlan"),
  Some("rock the jvm"),
  Role.ADMIN
  )

  val danielEmail = daniel.email
  
  val ricardo = User (
  "ricardo@rockthejvm.com",
  "$2a$10$W88WykuhIfAuA3JBXhG.KOf0DR8yqUUOWmPnd7pdQWic/7AK6f8lW",
  Some("ricardo"),
  Some("cardin"),
  Some("rock the jvm"),
  Role.RECRUITER
  )

  val ricardoEmail = ricardo.email

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

}
