package com.rockthejvm.jobsboard.core

import cats.effect.MonadCancelThrow
import cats.implicits.catsSyntaxApplicativeId
import com.rockthejvm.jobsboard.domain.auth.NewPassWordInfo
import com.rockthejvm.jobsboard.domain.security.{Authenticator, JwtToken}
import com.rockthejvm.jobsboard.domain.user.{NewUserInfo, User}
import org.typelevel.log4cats.Logger
import tsec.authentication.AugmentedJWT
import tsec.mac.jca.HMACSHA256

trait Auth[F[_]] {
  def login(email: String, password: String): F[Option[JwtToken]]
  def signUp(newUserInfo: NewUserInfo): F[Option[User]]
  def changePassword(email: String, newPasswordInfo: NewPassWordInfo): F[Either[String,Option[User]]]

}

class LiveAuth[F[_]: MonadCancelThrow : Logger] private (users: Users[F], authenticator: Authenticator[F]) extends Auth[F] {
  override def login(email: String, password: String): F[Option[JwtToken]] = ???
  override def signUp(newUserInfo: NewUserInfo): F[Option[User]] = ???
  override def changePassword(email: String, newPasswordInfo: NewPassWordInfo): F[Either[String, Option[User]]] = ???
}

object LiveAuth {
  def apply[F[_]: MonadCancelThrow : Logger](users: Users[F], authenticator: Authenticator[F]): F[LiveAuth[F]] = new LiveAuth[F](users, authenticator).pure[F]
}
