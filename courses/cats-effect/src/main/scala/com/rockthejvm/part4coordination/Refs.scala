package com.rockthejvm.part4coordination

import cats.effect.{IO, IOApp, Ref}
import cats.implicits.{catsSyntaxParallelSequence1, catsSyntaxTuple2Parallel}
import com.rockthejvm.utils.DebugWrapper

import scala.concurrent.duration.DurationInt

object Refs extends IOApp.Simple {

  // ref = purely functional atomic reference
  val atomicMol: IO[Ref[IO, Int]] = Ref[IO].of(42)
  val atomicMol_v2: IO[Ref[IO, Int]] = IO.ref(42)

  // modifying is an effect
//  val increasedMol: IO[IO[Unit]] = atomicMol.map { ref =>
  val increasedMol: IO[Unit] = atomicMol.flatMap { ref =>
    ref.set(43) // thread-safe
  }

  // obtain a value
  val mol = atomicMol.flatMap { ref =>
    ref.get // thread-safe
  }

  val getSetMol: IO[Int] = atomicMol.flatMap { ref =>
    ref.getAndSet(43) // gets the old value, sets the new one
  }

  // updating with a function
  val fMol: IO[Unit] = atomicMol.flatMap { ref =>
    ref.update(value => value * 10)
  }

  val updatedMol: IO[Int] = atomicMol.flatMap { ref =>
    ref.updateAndGet(value => value * 10) // get the new value
    // can also use getAndUpdate to get the OLD value
  }

  // modifying with a function returning a different type
  val modifiedMol: IO[String] = atomicMol.flatMap { ref =>
    ref.modify(value => (value * 10, s"my current value is $value"))
  }

  // why do we need this? concurrent + thread-safe reads/writes over shared values, in a purely functional way
  def demoConcurrentWorkImpure(): IO[Unit] = {
    var count = 0

    def task(workload: String): IO[Unit] = {
      val wordCount = workload.split(" ").length
      for {
        _ <- IO(s"counting words for '$workload': '$wordCount'").debug
        newCount <- IO(count + wordCount)
        _ <- IO(s"new total: $newCount").debug
        _ <- IO(count += wordCount)
      } yield ()
    }

    List("I love cats effect", "this ref thing is useless", "daniel writes a lot of code")
      .map(task)
      .parSequence
      .void
  }

  /*
    drawbacks:
    - hard to read/debug
    - mix pure/impure code
    - NOT THREAD-SAFE
   */

  // ref equivalent
  def demoConcurrentWorkPure(): IO[Unit] = {
    def task(workload: String, total: Ref[IO, Int]): IO[Unit] = {
      val wordCount = workload.split(" ").length

      for {
        _ <- IO(s"counting words for '$workload': '$wordCount'").debug
        newCount <- total.updateAndGet(currentCount => currentCount + wordCount)
        _ <- IO(s"new total: $newCount").debug
      } yield ()
    }

    for {
      initialCount <- Ref[IO].of(0)
      _ <-  List("I love cats effect", "this ref thing is useless", "daniel writes a lot of code")
        .map(string => task(string, initialCount))
        .parSequence
    } yield ()
  }

  // exercise
  // does not work
  def tickingClockImpure(): IO[Unit] = {
    var ticks: Long = 0L
    def tickingClock: IO[Unit] = for {
      _ <- IO.sleep(1.second)
      _ <- IO(System.currentTimeMillis()).debug
      _ <- IO(ticks += 1) // not thread-safe
      _ <- tickingClock
    } yield ()

    def printTicks: IO[Unit] = for {
      _ <- IO.sleep(5.seconds)
      _ <- IO(s"ticks: $ticks").debug
      _ <- printTicks
    } yield ()

    for {
      _ <- (tickingClock, printTicks).parTupled
    } yield ()
  }


  // works, thread-safe, best-practice
  def tickingClockPure(): IO[Unit] = {
    def tickingClock(ticks: Ref[IO, Int]): IO[Unit] = for {
      _ <- IO.sleep(1.second)
      _ <- IO(System.currentTimeMillis()).debug
      _ <- ticks.update(_ + 1) // thread-safe effect
      _ <- tickingClock(ticks)
    } yield ()

    def printTicks(ticks: Ref[IO, Int]): IO[Unit] = for {
      _ <- IO.sleep(5.seconds)
      t <- ticks.get
      _ <- IO(s"ticks: $t").debug
      _ <- printTicks(ticks)
    } yield ()

    for {
      tickRef <- Ref[IO].of(0)
      _ <- (tickingClock(tickRef), printTicks(tickRef)).parTupled
    } yield ()
  }

  // does not work
  def tickingClockWeird(): IO[Unit] = {
    val ticks = Ref[IO].of(0) // IO[Ref]

    def tickingClock: IO[Unit] = for {
      t <- ticks // ticks will give you a NEW Ref
      _ <- IO.sleep(1.second)
      _ <- IO(System.currentTimeMillis()).debug
      _ <- t.update(_ + 1) // thread-safe effect
      _ <- tickingClock
    } yield ()

    def printTicks: IO[Unit] = for {
      t <- ticks // ticks will give you a NEW Ref
      _ <- IO.sleep(5.seconds)
      currentTicks <- t.get
      _ <- IO(s"ticks: $currentTicks").debug
      _ <- printTicks
    } yield ()

    for {
      _ <- (tickingClock, printTicks).parTupled
    } yield ()
  }


  override def run: IO[Unit] = tickingClockWeird()

}
