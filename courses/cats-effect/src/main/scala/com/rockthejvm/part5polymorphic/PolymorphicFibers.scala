package com.rockthejvm.part5polymorphic

import cats.effect.kernel.Outcome.{Canceled, Errored, Succeeded}
import cats.effect.{Fiber, IO, IOApp, MonadCancel, Outcome, Spawn}
import com.rockthejvm.utils.DebugWrapper

import scala.concurrent.duration.DurationInt

object PolymorphicFibers extends IOApp.Simple {

  // Spawn = create fibers for any effect
  trait MyGenSpawn[F[_], E] extends MonadCancel[F, E] {
    def start[A](fa: F[A]): F[Fiber[F, Throwable, A]] // creates a fiber
    def never[A]: F[A] // forver-suspending effect
    def cede: F[Unit] // "yield" effect
    // todo: more to come

    def racePair[A, B](fa: F[A], fb: F[B]): F[Either[ // fundamental racing
      (Outcome[F, E, A], Fiber[F, E, B]),
      (Fiber[F, E, A], Outcome[F, E, B])
    ]]
  }

  trait MySpawn[F[_]] extends MyGenSpawn[F, Throwable]

  val mol = IO(42)
  val fiber: IO[Fiber[IO, Throwable, Int]] = mol.start

  // benefits of MyGenSpawn extending MonadCancel: pure, map/flatMap, raiseError, uncancelable, start

  val spawnIO = Spawn[IO] // fetch the given/implicit Spawn[IO] inside CE

  def ioOnSomeThread[A](io: IO[A]): IO[Outcome[IO, Throwable, A]] = for {
    fib <- spawnIO.start(io) // io.start assumes the presence of a Spawn[IO] in scope
    res <- fib.join
  } yield res


  import cats.syntax.functor._ // map
  import cats.syntax.flatMap._ // flatMap

  // generalize
  import cats.effect.syntax.spawn._ // start extension methods
  def effectOnSomeThread[F[_], A](fa: F[A])(implicit spawn: Spawn[F]): F[Outcome[F, Throwable, A]] = for {
//    fib <- spawn.start(fa)
    fib <- fa.start // really powerful
    res <- fib.join
  } yield res

  val molOnFiber = ioOnSomeThread(mol)
  val molOnFiber_v2 = effectOnSomeThread(mol)

  // exercise - generalize the following code

  // original IO race
  def ioRace[A, B](ioa: IO[A], iob: IO[B]): IO[Either[A, B]] =
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

  // generalized race
  def generalRace[F[_], A, B](fa: F[A], fb: F[B])(implicit spawn: Spawn[F]): F[Either[A, B]] =
    spawn.racePair(fa, fb).flatMap {
      case Left((outA, fibB)) => outA match {
        case Succeeded(effectA) => fibB.cancel.flatMap(_ => effectA.map(a => Left(a)))
        case Errored(e) => fibB.cancel.flatMap(_ => spawn.raiseError(e))
        case Canceled() => fibB.join.flatMap {
          case Succeeded(effectB) => effectB.map(b => Right(b))
          case Errored(e) => spawn.raiseError(e)
          case Canceled() => spawn.raiseError(new RuntimeException("both computations canceled"))
        }
      }
      case Right((fibA, outB)) => outB match {
        case Succeeded(effectB) => fibA.cancel.flatMap(_ => effectB.map(b => Right(b)))
        case Errored(e) => fibA.cancel.flatMap(_ => spawn.raiseError(e))
        case Canceled() => fibA.join.flatMap {
          case Succeeded(effectA) => effectA.map(a => Left(a))
          case Errored(e) => spawn.raiseError(e)
          case Canceled() => spawn.raiseError(new RuntimeException("both computations canceled"))
        }
      }
    }

  val fast = IO.sleep(1.second) >> IO(42).debug
  val slow = IO.sleep(2.seconds) >> IO("scala").debug
  val race = ioRace(fast, slow)
  val race_v2 = generalRace(fast, slow)

  override def run: IO[Unit] = race_v2.void

}
