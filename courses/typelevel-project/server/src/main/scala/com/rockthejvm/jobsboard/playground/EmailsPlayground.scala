package com.rockthejvm.jobsboard.playground

import cats.effect.{IO, IOApp}
import com.rockthejvm.jobsboard.config.EmailServiceConfig
import com.rockthejvm.jobsboard.core.LiveEmails

import java.util.Properties
import javax.mail.internet.MimeMessage
import javax.mail.{Authenticator, Message, PasswordAuthentication, Session, Transport}

object EmailsPlayground {
  def main(args: Array[String]): Unit = {
    // configs
    // user, pass, host, port
    val host = "smtp.ethereal.email"
    val port = 587
    val user = "wilmer.rau3@ethereal.email"
    val pass = "x2KY1VcFrkMe8Zyw8H"
    val frontendUrl = "https://google.com"

    val token = "ABCD1234"

    // properties file
    val prop = new Properties
    prop.put("mail.smtp.auth", true)
    prop.put("mail.smtp.starttls.enable", true)
    prop.put("mail.smtp.host", host)
    prop.put("mail.smtp.port", port)
    prop.put("mail.smtp.ssl", host)

    // authentication
    val auth = new Authenticator {
      override protected def getPasswordAuthentication: PasswordAuthentication =
        new PasswordAuthentication(user, pass)
    }

    // session
    val session = Session.getInstance(prop, auth)

    // email itself
    val subject = "email from rock the jvm"
    val content =
      s"""
        |<div style="
        |border: 1px solid black;
        |padding: 20px;
        |font-family: sans-serif;
        |line-height: 2;
        |font-size: 20px;
        |">
        |<h1>rock the jvm: password recovery</h1>
        |<p> your password recovery token is: $token</p>
        |<p>click <a href="$frontendUrl/login">here</a> to get back to the application</p>
        |<p> from rock the jvm</p>
        |</div>
        |""".stripMargin


    // message = MIME message
    val message = new MimeMessage(session)
    message.setFrom("daniel@rockthejvm.com")
    message.setRecipients(Message.RecipientType.TO, "the.user@gmail.com")
    message.setSubject(subject)
    message.setContent(content, "text/html; charset=utf-8")

    // send
    Transport.send(message)
  }

}

object EmailsEffectsPlayground extends IOApp.Simple {
  override def run: IO[Unit] = for {
    emails <- LiveEmails[IO](
      EmailServiceConfig(
        host = "smtp.ethereal.email",
        port = 587,
        user = "wilmer.rau3@ethereal.email",
        pass = "x2KY1VcFrkMe8Zyw8H",
        frontendUrl = "https://google.com"
      )
    )
      _ <- emails.sendPasswordRecoveryEmail("someone@rockthejvm.com", "rockthejvm")
  } yield ()
}
