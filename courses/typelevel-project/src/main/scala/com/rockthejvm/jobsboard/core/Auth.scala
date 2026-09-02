package com.rockthejvm.jobsboard.core

import cats.Applicative.ops.toAllApplicativeOps
import cats.Traverse.ops.toAllTraverseOps
import cats.TraverseFilter.ops.toAllTraverseFilterOps
import cats.data.OptionT
import cats.effect.{Async, IO, MonadCancelThrow, Ref}
import cats.implicits.{catsSyntaxApplicativeId, toFlatMapOps}
import com.rockthejvm.jobsboard.config.SecurityConfig
import com.rockthejvm.jobsboard.domain.auth.NewPasswordInfo
import com.rockthejvm.jobsboard.domain.security.{Authenticator, JwtToken}
import com.rockthejvm.jobsboard.domain.user.{NewUserInfo, Role, User}
import org.typelevel.log4cats.Logger
import tsec.authentication.{AugmentedJWT, BackingStore, IdentityStore, JWTAuthenticator}
import tsec.common.SecureRandomId
import tsec.mac.jca.HMACSHA256
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.BCrypt

import scala.concurrent.duration.DurationInt

trait Auth[F[_]] {
  def login(email: String, password: String): F[Option[JwtToken]]
  def signUp(newUserInfo: NewUserInfo): F[Option[User]]
  def changePassword(email: String, newPasswordInfo: NewPasswordInfo): F[Either[String,Option[User]]]
  // todo - password recovery via email
  def delete(email: String): F[Boolean]
  def authenticator: Authenticator[F]

}

class LiveAuth[F[_]: Async : Logger] private (users: Users[F], override val authenticator: Authenticator[F]) extends Auth[F] {
  override def login(email: String, password: String): F[Option[JwtToken]] =
    for {
      // find user in the db and return None if there's no user
      maybeUser <- users.find(email)

      // check password
      // Option[User].filterA(User => G[Boolean] => G[Option[User]]
      maybeValidatedUser <- maybeUser.filterA(user => BCrypt.checkpwBool[F](password, PasswordHash[BCrypt](user.hashedPassword)))

      // return new token if password matches
      //          Option[User].map(User => F[JWTToken]) => Option[F[JWTToken]] (Need F[Option[JWTToken]], which traverse provides)
      maybeJwtToken <- maybeValidatedUser.traverse(user => authenticator.create(user.email))
    } yield maybeJwtToken

  override def signUp(newUserInfo: NewUserInfo): F[Option[User]] =
    // find user in db. if we did => None
    users.find(newUserInfo.email).flatMap {
      case Some(_) => None.pure[F]
      case None => for {
        // hash the new password
        hashedPassword <- BCrypt.hashpw[F](newUserInfo.password)

        // create a new user in db
        user <- User(
          newUserInfo.email,
          hashedPassword,
          newUserInfo.firstName,
          newUserInfo.lastName,
          newUserInfo.company,
          Role.RECRUITER
        ).pure[F]

        id <- users.create(user)

      } yield Some(user)
    }

  override def changePassword(email: String, newPasswordInfo: NewPasswordInfo): F[Either[String, Option[User]]] = {
    // find user
//    users.find(email).flatMap {
//      case None => Right(None).pure[F]
//      case Some(user) =>
//        for {
//          // if user, check password
//          passCheck <- BCrypt.checkpwBool[F](newPasswordInfo.oldPassword, PasswordHash[BCrypt](user.hashedPassword))
//          updateResult <-
//            // if password ok, hash new password
//            if (passCheck) {
//              // update
//              for {
//                hashedPassword <- BCrypt.hashpw[F](newPasswordInfo.newPassword)
//                updatedUser <- users.update(user.copy(hashedPassword = hashedPassword))
//
//              } yield Right(updatedUser)
//            } else Left("invalid password").pure[F]
//        } yield updateResult
//    }

    // more concise implementation below

    def updateUser(user: User, newPassword: String): F[Option[User]] =
      for {
        hashedPassword <- BCrypt.hashpw[F](newPasswordInfo.newPassword)
        updatedUser <- users.update(user.copy(hashedPassword = hashedPassword))
      } yield updatedUser

    def checkAndUpdate(user: User, oldPassword: String, newPassword: String): F[Either[String, Option[User]]] =
      for {
        // if user, check password
        passCheck <- BCrypt.checkpwBool[F](newPasswordInfo.oldPassword, PasswordHash[BCrypt](user.hashedPassword))
        updateResult <-
          // if password ok, hash new password
          // update user
          if (passCheck)
            updateUser(user, newPassword).map(Right(_))
          else
            Left("invalid password").pure[F]
      } yield updateResult

    users.find(email).flatMap {
      case None => Right(None).pure[F]
      case Some(user) => checkAndUpdate(user, newPasswordInfo.oldPassword, newPasswordInfo.newPassword)
    }
  }

  override def delete(email: String): F[Boolean] =
    users.delete(email)
}

object LiveAuth {
  def apply[F[_]: Async : Logger](users: Users[F])(securityConfig: SecurityConfig): F[LiveAuth[F]] = {

    // 1. identity store: String => OptionT[F, User]
    val idStore: IdentityStore[F, String, User] = (email: String) => {
      OptionT(users.find(email))
    }

    // 2. backing store for JWT tokens: BackingStore[F, id, JwtToken]
    val tokenStoreF = Ref.of[F, Map[SecureRandomId, JwtToken]](Map.empty).map { ref =>
      new BackingStore[F, SecureRandomId, JwtToken] {
        // mutable map -> race conditions
        // use ref instead which is atomic
        override def get(id: SecureRandomId): OptionT[F, JwtToken] =
          OptionT(/*F[JwtToken]*/ ref.get.map(_.get(id)))

        override def put(elem: JwtToken): F[JwtToken] =
          ref.modify(store => (store + (elem.id -> elem), elem))

        override def update(v: JwtToken): F[JwtToken] =
          put(v)

        override def delete(id: SecureRandomId): F[Unit] =
          ref.modify(store => (store - id, ()))
      }
    }
      
    // 3. hashing key
    // TODO move to config
    val keyF = HMACSHA256.buildKey[F](securityConfig.secret.getBytes("UTF-8"))

   
    for {
      key <- keyF
      tokenStore <- tokenStoreF
      // 4. authenticator
      // jwt authenticator(key, identity store)
      authenticator = JWTAuthenticator.backed.inBearerToken(
        // expiry of tokens, max idle, idStore, key
        expiryDuration = securityConfig.jwtExpiryDuration, // expiration of tokens
        maxIdle = None, // max idle time(optional)
        identityStore = idStore, // identity store
        tokenStore = tokenStore,
        signingKey = key // hash key
      )  // 5. live auth
    } yield new LiveAuth[F](users, authenticator)
  }
}
