package com.rockthejvm.jobsboard

import cats.Monad
import cats.effect.{IO, IOApp}
import com.rockthejvm.jobsboard.config.EmberConfig
import com.rockthejvm.jobsboard.config.Syntax.loadF
import com.rockthejvm.jobsboard.http.routes.HealthRoutes
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import pureconfig.ConfigReader.Result
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException

/*
   1 - add a plain health endpoint to our app
   2 - add minimal configuration
   3 - basic http server layout
  */

object Application extends IOApp.Simple {

  val configSource: Result[EmberConfig] = ConfigSource.default.load[EmberConfig]

  override def run: IO[Unit] = ConfigSource.default.loadF[IO, EmberConfig].flatMap { config =>
        EmberServerBuilder
          .default[IO]
          .withHost(config.host)
          .withPort(config.port)
          .withHttpApp (HealthRoutes[IO].routes.orNotFound)
          .build
          .use (_ => IO.println ("server ready") *> IO.never)
  }


}
