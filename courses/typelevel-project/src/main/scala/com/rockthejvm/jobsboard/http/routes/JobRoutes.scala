package com.rockthejvm.jobsboard.http.routes

import cats.effect.Concurrent
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import cats.{Monad, MonadThrow}
import cats.syntax.all.*
import com.rockthejvm.jobsboard.core.Jobs
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

import java.util.UUID
import scala.collection.mutable
import com.rockthejvm.jobsboard.domain.job.*
import com.rockthejvm.jobsboard.http.responses.FailureResponse
import org.typelevel.log4cats.Logger
import com.rockthejvm.jobsboard.logging.syntax.*

class JobRoutes[F[_] : Concurrent: Logger] private (jobs: Jobs[F]) extends Http4sDsl[F] {

  // POST /jobs?offset=x&limit=y { filters } // todo: add query params and filters
  private val allJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root =>
      for {
        jobsList <- jobs.all()
        resp <- Ok(jobsList)
      } yield resp
  }

  // GET /jobs/uuid
  private val findJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      jobs.find(id).flatMap {
        case Some(job) => Ok(job)
        case None => NotFound(FailureResponse(s"job $id not found"))
      }
  }

  // POST /jobs/create { jobInfo }
//  private def createJob(jobInfo: JobInfo): F[Job] =
//    Job(
//      id = UUID.randomUUID(),
//      date = System.currentTimeMillis(),
//      ownerEmail = "TODO@rockthejvm.com",
//      jobInfo = jobInfo,
//      active = true
//    ).pure[F]



  private val createJobRouts: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "create" =>
      for {
        _ <- Logger[F].info("trying to add job")
        jobInfo <- req.as[JobInfo].logError(e => s"parsing payload failed: $e")
//        _ <- Logger[F].info(s"parsed job info: $jobInfo") <- verbose. add if needed for debugging
        jobId <- jobs.create("TODO@rockthejvm.com", jobInfo)
//        _ <- Logger[F].info(s"created job : $job") <- verbose. add if needed for debugging
//        _ <- database.put(job.id, job).pure[F]
        resp <- Created(jobId)
      } yield resp
  }

  // PUT /jobs/uuid { jobInfo }
  private val updateJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) =>
      for {
        jobInfo <- req.as[JobInfo]
        maybeNewJob <- jobs.update(id, jobInfo)
        resp <- maybeNewJob match {
          case Some(job) => Ok()
          case None => NotFound(FailureResponse(s"cannot update job $id: not found"))
        }
      } yield resp
  }

  // DELETE /jobs/uuid
  private val deleteJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ DELETE -> Root / UUIDVar(id)  =>
      jobs.find(id).flatMap{
        case Some(job) =>
          for {
            _ <- jobs.delete(id)
            resp <- Ok()
          } yield resp
        case None => NotFound(FailureResponse(s"cannot delete job $id: not found"))
      }
  }

  val routes = Router(
    "/jobs" -> (allJobsRoute <+> findJobRoute <+> createJobRouts <+> updateJobRoute <+> deleteJobRoute)
  )
}

object JobRoutes {
  def apply[F[_]: Concurrent: Logger](jobs: Jobs[F]) = new JobRoutes[F](jobs)
  
}
