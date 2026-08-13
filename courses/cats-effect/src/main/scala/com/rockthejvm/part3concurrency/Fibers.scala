package com.rockthejvm.part3concurrency

import cats.effect.kernel.Outcome.{Canceled, Errored, Succeeded}
import cats.effect.{Fiber, IO, IOApp, Outcome}
import com.rockthejvm.utils._

import scala.concurrent.duration.DurationInt

object Fibers extends IOApp.Simple {

  val meaningOfLife = IO.pure(42)
  val favLang = IO.pure("scala")

  def sameThreadIOs() = for {
    mol <- meaningOfLife.debug
    lang <- favLang.debug
  } yield ()

  // introduce the Fiber
  def createFiber: Fiber[IO, Throwable, String] = ??? // almost impossible to create fibers manually

  // the fiber is not actually started but its allocation is wrapped in another effect
  val aFiber: IO[Fiber[IO, Throwable, Int]] = meaningOfLife.debug.start

  def differentThreadIOs() = for {
    _ <- aFiber
    _ <- favLang.debug
  } yield()

  // joining a fiber
  def runOnSomeOtherThread[A](io: IO[A]): IO[Outcome[IO, Throwable, A]] = for {
    fib <- io.start
    result <- fib.join // an effect which waits for the fiber to terminate
  } yield result

  /*
    possible outcomes:
    - success with an IO
    - failure with an exception
    - cancelled
   */

  val someIOOnAnotherThread = runOnSomeOtherThread(meaningOfLife)
  val someResultFromAnotherThread = someIOOnAnotherThread.flatMap {
    case Succeeded(effect) => effect
    case Errored(_) => IO(0)
    case Canceled() => IO(0)
  }

  def throwOnAnotherThread() = for {
    fib <- IO.raiseError[Int](new RuntimeException("no number for you")).start
    result <- fib.join
  } yield result

  def testCancel() = {
    val task = IO("starting").debug >> IO.sleep(1.second) >> IO("done").debug // <-- IO(Thread.sleep(1000))

    // onCancel is a "finalizer", allowing you to free up resources in case you get canceled
    val taskWithCancellationHandler = task.onCancel(IO("I'm being cancelled").debug.void)

    for {
      fib <- taskWithCancellationHandler.start // on a separate thread
      _ <- IO.sleep(500.millis) >> IO("cancelling").debug // running on the calling thread
      _ <- fib.cancel // stop the execution of this fiber
      result <- fib.join // wait for fiber to join/terminate
    } yield result
  }


  override def run: IO[Unit] =
//    runOnSomeOtherThread(meaningOfLife) // IO(Succeeded(IO(42)))
//      .debug.void
//    throwOnAnotherThread().debug.void
      testCancel().debug.void


}
