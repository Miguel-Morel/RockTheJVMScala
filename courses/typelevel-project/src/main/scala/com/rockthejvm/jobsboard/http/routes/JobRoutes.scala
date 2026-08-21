package com.rockthejvm.jobsboard.http.routes

import cats.effect.Concurrent
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import cats.{Monad, MonadThrow}
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

import java.util.UUID
import scala.collection.mutable
import com.rockthejvm.jobsboard.domain.job.*
import com.rockthejvm.jobsboard.http.responses.FailureResponse
import org.typelevel.log4cats.Logger

class JobRoutes[F[_] : Concurrent: Logger] private extends Http4sDsl[F] {

  // "database"
  private val database = mutable.Map[UUID, Job]()

  // POST /jobs?offset=x&limit=y { filters } // todo: add query params and filters
  private val allJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root =>
      Ok(database.values)
  }

  // GET /jobs/uuid
  private val findJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      database.get(id) match {
        case Some(job) => Ok(job)
        case None => NotFound(FailureResponse(s"job $id not found"))
      }
  }

  // POST /jobs/create { jobInfo }
  private def createJob(jobInfo: JobInfo): F[Job] =
    Job(
      id = UUID.randomUUID(),
      date = System.currentTimeMillis(),
      ownerEmail = "TODO@rockthejvm.com",
      jobInfo = jobInfo,
      active = true
    ).pure[F]

  import com.rockthejvm.jobsboard.logging.syntax.*

  private val createJobRouts: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "create" =>
      for {
        _ <- Logger[F].info("trying to add job")
        jobInfo <- req.as[JobInfo].logError(e => s"parsing payload failed: $e")
//        _ <- Logger[F].info(s"parsed job info: $jobInfo") <- verbose. add if needed for debugging
        job <- createJob(jobInfo)
//        _ <- Logger[F].info(s"created job : $job") <- verbose. add if needed for debugging
        _ <- database.put(job.id, job).pure[F]
        resp <- Created(job.id)
      } yield resp
  }

  // PUT /jobs/uuid { jobInfo }
  private val updateJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) =>
      database.get(id) match {
        case Some(job) =>
          for {
            jobInfo <- req.as[JobInfo]
            _ <- database.put(id, job.copy(jobInfo = jobInfo)).pure[F]
            resp <- Ok()
          } yield resp
        case None => NotFound(FailureResponse(s"cannot update job $id: not found"))
      }
  }

  // DELETE /jobs/uuid
  private val deleteJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ DELETE -> Root / UUIDVar(id)  =>
      database.get(id) match {
        case Some(job) =>
          for {
            _ <- database.remove(id).pure[F]
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
  def apply[F[_]: Concurrent: Logger] = new JobRoutes[F]
  
}
