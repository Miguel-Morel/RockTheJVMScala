package com.rockthejvm.part3concurrency

import cats.effect.kernel.Outcome.{Canceled, Errored, Succeeded}
import cats.effect.{Fiber, IO, IOApp, Outcome}
import com.rockthejvm.utils.DebugWrapper

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object RacingIOs extends IOApp.Simple {

  def runWithSleep[A](value: A, duration: FiniteDuration): IO[A] = {
    (
      IO(s"starting computation: $value").debug >>
      IO.sleep(duration) >>
      IO(s"computation: done") >>
      IO(value)
    ).onCancel(IO(s"computation canceled for $value").debug.void)
  }

  def testRace() = {
    val meaningOfLife = runWithSleep(42, 1.second)
    val favLang = runWithSleep("scala", 2.seconds)
    val first: IO[Either[Int, String]] = IO.race(meaningOfLife, favLang)
//    val first: IO[Either[Int, String]] = unrace(meaningOfLife, favLang)

  /*
  race:
  - both IOs run on separate fibers
  - the first one to finish will complete the result
  - the loser will be canceled
 */

    first.flatMap {
      case Left(mol) => IO(s"meaning of life $mol")
      case Right(lang) => IO(s"fav language won: $lang")
    }
  }

  def testRacePair() = {
    val meaningOfLife = runWithSleep(42, 1.second)
    val favLang = runWithSleep("scala", 2.seconds)
    val raceResult: IO[Either[
      (Outcome[IO, Throwable, Int], Fiber[IO, Throwable, String]), // (winner result, loser fiber)
      (Fiber[IO, Throwable, Int], Outcome[IO, Throwable, String]) // (loser fiber, winner result)
    ]] = IO.racePair(meaningOfLife, favLang)

    raceResult.flatMap {
      case Left((outMol, fibLang)) => fibLang.cancel >> IO("mol won").debug >> IO(outMol).debug
      case Right((fibMol, outLang)) => fibMol.cancel >> IO("language won").debug >> IO(outLang).debug
    }
  }

  // exercises
  // 1 - implement a timeout pattern with race
  def timeout[A](io: IO[A], duration: FiniteDuration): IO[A] = {
    val timeoutEffect = IO.sleep(duration)
    val result = IO.race(io, timeoutEffect)

    result.flatMap {
      case Left(v) => IO(v)
      case Right(_) => IO.raiseError(new RuntimeException("computation timed out"))
    }
  }

  val importantTask = IO.sleep(2.seconds) >> IO(42).debug
  val testTimeout = timeout(importantTask, 1.seconds)

  // timeout by cats effect
  val testTimeout_v2 = importantTask.timeout(1.seconds)

  // 2 - a method to return a losing effect from a race (hint: use racePair)
  def unrace[A, B](ioa: IO[A], iob: IO[B]): IO[Either[A, B]] =
    IO.racePair(ioa, iob).flatMap {
      case Left((_, fibB)) => fibB.join.flatMap {
        case Succeeded(resultEffect) => resultEffect.map(result => Right(result))
        case Errored(e) => IO.raiseError(e)
        case Canceled() => IO.raiseError(new RuntimeException("loser canceled"))
      }
      case Right((fibA, _)) => fibA.join.flatMap {
        case Succeeded(resultEffect) => resultEffect.map(result => Left(result))
        case Errored(e) => IO.raiseError(e)
        case Canceled() => IO.raiseError(new RuntimeException("loser canceled"))
      }
    }

  // 3 - implement race in terms of racePair
  def simpleRace[A, B](ioa: IO[A], iob: IO[B]): IO[Either[A, B]] =
    IO.racePair(ioa, iob).flatMap {
      case Left((outA, fibB)) => outA match {
        case Succeeded(effectA) => fibB.cancel >> effectA.map(a => Left(a))
        case Errored(e) => fibB.cancel >> IO.raiseError(e)
        case Canceled() => fibB.join.flatMap {
          case Succeeded(effectB) => effectB.map(b => Right(b))
          case Errored(e) => IO.raiseError(e)
          case Canceled() => IO.raiseError(new RuntimeException("both computations canceled"))
        }
      }
      case Right((fibA, outB)) => outB match {
        case Succeeded(effectB) => fibA.cancel >> effectB.map(b => Right(b))
        case Errored(e) => fibA.cancel >> IO.raiseError(e)
        case Canceled() => fibA.join.flatMap {
          case Succeeded(effectA) => effectA.map(a => Left(a))
          case Errored(e) => IO.raiseError(e)
          case Canceled() => IO.raiseError(new RuntimeException("both computations canceled"))
        }
      }
    }


  override def run: IO[Unit] =
    testRace().debug.void


}
