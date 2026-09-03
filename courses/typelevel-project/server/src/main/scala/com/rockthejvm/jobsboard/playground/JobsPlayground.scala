package com.rockthejvm.jobsboard.playground

import cats.effect.{IO, IOApp, Resource}
import com.rockthejvm.jobsboard.core.LiveJobs
import com.rockthejvm.jobsboard.domain.job.JobInfo
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.io.StdIn

object JobsPlayground extends IOApp.Simple {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val postgresResource: Resource[IO, HikariTransactor[IO]] = for {
    ec <- ExecutionContexts.fixedThreadPool[IO](32)
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql:board",
      "docker",
      "docker",
      ec
    )
  } yield xa
  
  val jobInfo = JobInfo.minimal(
    company = "rock the jvm",
    title = "software engineer",
    description = "best job ever",
    externalUrl = "rockthejvm.com",
    remote = false,
    location = "anywhere"
  )


  override def run: IO[Unit] = postgresResource.use { xa =>
    for {
      jobs <- LiveJobs[IO](xa)
      _ <- IO(println("ready.next...")) *> IO(StdIn.readLine())
      id <- jobs.create("daniel@rockthejvm.com", jobInfo)
      _ <- IO(println("next...")) *> IO(StdIn.readLine())
      list <- jobs.all()
      _ <- IO(println(s"all jobs: $list. next...")) *> IO(StdIn.readLine())
      _ <- jobs.update(id, jobInfo.copy(title = "software aficionado"))
      newJob <- jobs.find(id)
      _ <- IO(println(s"new job: $newJob.next...")) *> IO(StdIn.readLine())
      _ <- jobs.delete(id)
      listAfter <- jobs.all()
      _ <- IO(println(s"deleted job. list now: $listAfter. next...")) *> IO(StdIn.readLine())
    } yield ()
  }

}
