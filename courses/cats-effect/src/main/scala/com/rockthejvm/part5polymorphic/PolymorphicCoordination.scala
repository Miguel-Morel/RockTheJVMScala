package com.rockthejvm.part5polymorphic

import cats.effect.implicits.genSpawnOps
import cats.effect.{Concurrent, Deferred, IO, IOApp, MonadCancel, Ref, Spawn}

import com.rockthejvm.utilsScala2.general.DebugWrapper

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object PolymorphicCoordination extends IOApp.Simple {

  // Concurrent - Ref + Deferred for ANY effect type
  trait MyConcurrent[F[_]] extends Spawn[F] {
    def ref[A](a: A): F[Ref[F, A]]
    def deferred[A]: F[Deferred[F, A]]
  }

  val concurrentIO = Concurrent[IO] // implicit instance of Concurrent[IO]
  val aDeferred = Deferred[IO, Int] // implicit Concurrent[IO] in scope
  val aDeferred_v2 = concurrentIO.deferred[Int]
  val aRef = concurrentIO.ref(42)

  // capabilities afforded: pure, map/flatMap, raiseError, uncancelable, start (fibers), ref + deferred

  def eggBoiler(): IO[Unit] = {
    def eggReadyNotification(signal: Deferred[IO, Unit]) = for {
      _ <- IO("egg boiling on some other fiber. waiting...").debug
      _ <- signal.get
      _ <- IO("egg ready").debug
    } yield ()

    def tickingClock(counter: Ref[IO, Int], signal: Deferred[IO, Unit]): IO[Unit] = for {
      _ <- IO.sleep(1.second)
      count <- counter.updateAndGet(_ + 1)
      _ <- IO(count).debug
      _ <- if(count >= 10) signal.complete(()) else tickingClock(counter, signal)
    } yield ()

    for {
      counter <- Ref[IO].of(0)
      signal <- Deferred[IO, Unit]
      notificationFib <- eggReadyNotification(signal).start
      clock <- tickingClock(counter, signal).start
      _ <- notificationFib.join
      _ <- clock.join
    } yield ()
  }

  import cats.syntax.flatMap._
  import cats.syntax.functor._

  // added here explicitly due to a scala 3 bug discovered during the recording
  def unsafeSleepDupe[F[_], E](duration: FiniteDuration)(implicit mc: MonadCancel[F, E]): F[Unit] =
    mc.pure(Thread.sleep(duration.toMillis))

  def polymorphicEggBoiler[F[_]](implicit concurrent: Concurrent[F]): F[Unit] = {
    def eggReadyNotification(signal: Deferred[F, Unit]) = for {
      _ <- concurrent.pure("egg boiling on some other fiber. waiting...").debug
      _ <- signal.get
      _ <- concurrent.pure("egg ready").debug
    } yield ()

    def tickingClock(counter: Ref[F, Int], signal: Deferred[F, Unit]): F[Unit] = for {
      _ <- unsafeSleepDupe[F, Throwable](1.second)
      count <- counter.updateAndGet(_ + 1)
      _ <- concurrent.pure(count).debug
      _ <- if(count >= 10) signal.complete(()) else tickingClock(counter, signal)
    } yield ()

    for {
      counter <- concurrent.ref(0)
      signal <- concurrent.deferred[Unit]
      notificationFib <- eggReadyNotification(signal).start
      clock <- tickingClock(counter, signal).start
      _ <- notificationFib.join
      _ <- clock.join
    } yield ()
  }



  override def run: IO[Unit] = polymorphicEggBoiler[IO]

}
