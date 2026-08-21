package com.rockthejvm.jobsboard.http.routes

import cats.Monad
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class JobRoutes[F[_] : Monad] private extends Http4sDsl[F] {

  // POST /jobs?offset=x&limit=y { filters } // todo: add query params and filters
  private val allJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root =>
      Ok("TODO")
  }

  // GET /jobs/uuid
  private val findJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      Ok(s"TODO find job for $id")
  }

  // POST /jobs/create { jobInfo }
  private val createJobRouts: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root / "create" =>
      Ok("TODO")
  }

  // PUT /jobs/uuid { jobInfo }
  private val updateJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case PUT -> Root / UUIDVar(id) =>
      Ok(s"TODO update job at $id")
  }

  // DELETE /jobs/uuid
  private val deleteJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case DELETE -> Root / UUIDVar(id)  =>
      Ok(s"TODO delete job at $id")
  }

  val routes = Router(
    "/jobs" -> (allJobsRoute <+> findJobRoute <+> createJobRouts <+> updateJobRoute <+> deleteJobRoute)
  )
}

object JobRoutes {
  def apply[F[_]: Monad] = new JobRoutes[F]
  
}
