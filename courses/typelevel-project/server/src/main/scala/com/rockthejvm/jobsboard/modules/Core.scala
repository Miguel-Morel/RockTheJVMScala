package com.rockthejvm.jobsboard.modules

import cats.effect.{MonadCancelThrow, Resource}
import com.rockthejvm.jobsboard.core.{Jobs, LiveJobs}
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import com.rockthejvm.jobsboard.core.*
import cats.effect.*
import cats.implicits.*
import com.rockthejvm.jobsboard.config.{EmailServiceConfig, SecurityConfig, TokenConfig}
import com.rockthejvm.jobsboard.domain.auth
import doobie.util.transactor.Transactor
import org.typelevel.log4cats.Logger

final class Core[F[_]] private (val jobs: Jobs[F], val users: Users[F], val auth: Auth[F])

// postgres -> jobs -> core -> httpApi -> app

object Core {
  def apply[F[_]: Async : Logger](xa: Transactor[F], tokenConfig: TokenConfig, emailServiceConfig: EmailServiceConfig): Resource[F, Core[F]] = {
    val coreF = for {
      jobs <- LiveJobs[F](xa)
      users <- LiveUsers[F](xa)
      tokens <- LiveTokens[F](users)(xa, tokenConfig)
      emails <- LiveEmails[F](emailServiceConfig)
      auth <- LiveAuth[F](users, tokens, emails)
    } yield new Core(jobs, users, auth)

    Resource.eval(coreF)
  }
}
