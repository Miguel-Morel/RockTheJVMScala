package com.rockthejvm.jobsboard.modules

import cats.effect.{MonadCancelThrow, Resource}
import com.rockthejvm.jobsboard.core.{Jobs, LiveJobs}
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import com.rockthejvm.jobsboard.core.*
import cats.effect.*
import cats.implicits.*
import doobie.util.transactor.Transactor

final class Core[F[_]] private (val jobs: Jobs[F])

// postgres -> jobs -> core -> httpApi -> app

object Core {
  def postgresResource[F[_]: Async]: Resource[F, HikariTransactor[F]] = for {
    ec <- ExecutionContexts.fixedThreadPool(32)
    xa <- HikariTransactor.newHikariTransactor[F](
      "org.postgresql.Driver",
      "jdbc:postgresql:board", // todo: move to config
      "docker",
      "docker",
      ec
    )
  } yield xa

  def apply[F[_]: Async](xa: Transactor[F]): Resource[F, Core[F]] = {
    Resource
      .eval(LiveJobs[F](xa)
      .map(jobs => new Core(jobs)))
  }
}
