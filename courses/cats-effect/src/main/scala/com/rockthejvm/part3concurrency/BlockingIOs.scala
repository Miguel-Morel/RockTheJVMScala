package com.rockthejvm.part3concurrency

import cats.effect.{IO, IOApp}
import com.rockthejvm.utils.DebugWrapper

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object BlockingIOs extends IOApp.Simple {

  val someSleeps = for {
    _ <- IO.sleep(1.second).debug // semantic blocking = no "actual" thread is blocked
    _ <- IO.sleep(1.second).debug
  } yield ()

  // really blocking IOs
  val aBlockingIOs = IO.blocking {
    Thread.sleep(1000)
    println(s"[${Thread.currentThread().getName}] computed a blocking code")
    42
  } // will evaluate a thread from ANOTHER thread pool specific for blocking calls

  // yielding
  val iosOnManyThreads = for {
    _ <- IO("first").debug
    _ <- IO.cede // a signal to yield control over a thread - equivalent to IO.shift (cats-effect 2)
    _ <- IO("second").debug // the rest of this effect amy run on another thread (not necessarily)
    _ <- IO.cede
    _ <- IO("third").debug
  } yield ()

  def testThousandEffectsSwitch() = {
    val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
    (1 to 1000).map(IO.pure).reduce(_.debug >> IO.cede >> _.debug).evalOn(ec)
  }

  // blocking calls & IO.sleep and yield control over the calling thread automatically

  override def run: IO[Unit] = testThousandEffectsSwitch().void

}
