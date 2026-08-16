package com.rockthejvm.part5polymorphic

import cats.effect.{Concurrent, IO, IOApp, Temporal}
import com.rockthejvm.utilsScala2.DebugWrapper

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object PolymorphicTemporalSuspension extends IOApp.Simple {

  // Temporal - time-blocking effects
  trait MyTemporal[F[_]] extends Concurrent[F] {
    def sleep(time: FiniteDuration): F[Unit] // semantically blocks this fiber for a specified time
  }

  // abilities: pure, map/flatMap, raiseError, uncancelable, start, ref/deferred, sleep
  val temporalIO = Temporal[IO] // implicit Temporal[IO] in scope
  val chainOfEffects = IO("loading...").debug *> IO.sleep(1.second) *> IO("game ready").debug
  val chainOfEffects_v2 = temporalIO.pure(IO("loading...").debug) *> temporalIO.sleep(1.second) *> temporalIO.pure("game ready").debug // same

  /*
    exercise: generalize the following
   */

  import cats.syntax.flatMap._
  def timeout[F[_], A](fa: F[A], duration: FiniteDuration)(implicit temporal: Temporal[F]): F[A] = {
    val timeoutEffect = temporal.sleep(duration)
    val result = temporal.race(fa, timeoutEffect)

    result.flatMap {
      case Left(v) => temporal.pure(v)
      case Right(_) => temporal.raiseError(new RuntimeException("computation timed out"))
    }
  }

  override def run: IO[Unit] = ???

}
