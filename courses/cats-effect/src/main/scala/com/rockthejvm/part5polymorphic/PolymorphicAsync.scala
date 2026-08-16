package com.rockthejvm.part5polymorphic

import cats.effect.kernel.Concurrent
import cats.effect.{Async, IO, IOApp, Sync, Temporal}
import com.rockthejvm.utilsScala2.DebugWrapper

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object PolymorphicAsync extends IOApp.Simple {

  // Async - asynchronous computations, "suspended" in F
  trait MyAsync[F[_]] extends Sync[F] with Temporal[F] {
    // fundamental description of async computations
    def executionContext: F[ExecutionContext]
    def async[A](cb: (Either[Throwable, A] => Unit) => F[Option[F[Unit]]]): F[A]
    def evalOn[A](fa: F[A], ec: ExecutionContext): F[A]

    def async_[A](cb: (Either[Throwable, A] => Unit) => Unit): F[A] =
      async(kb => map(pure(cb(kb)))(_ => None))
    def never[A]: F[A] = async_(_ => ())
  }

  val asyncIO = Async[IO] // implicit Async[IO]

  // abilities: pure, map/flatMap, raiseError, uncancelable, start, ref/deferred, sleep, delay/defer/blocking

  val ec = asyncIO.executionContext

  // power: async_ + async: FFI (foreign-function interface)
  val threadPool = Executors.newFixedThreadPool(10)
  type Callback[A] = Either[Throwable, A] => Unit
  val asyncMeaningOfLife: IO[Int] = IO.async_ { cb: Callback[Int] =>
    // start computation on some other thread pool
    threadPool.execute { () =>
      println(s"[${Thread.currentThread().getName}] computing an async mol")
      cb(Right(42))
    }
  }

  val asyncMeaningOfLife_v2: IO[Int] = asyncIO.async_ { cb: Callback[Int] =>
    // start computation on some other thread pool
    threadPool.execute { () =>
      println(s"[${Thread.currentThread().getName}] computing an async mol")
      cb(Right(42))
    }
  } // same

  val asyncMeaningOfLifeCompolex: IO[Int] = IO.async {(cb: Callback[Int]) =>
    IO {
      threadPool.execute { () =>
        println(s"[${Thread.currentThread().getName}] computing an async mol")
        cb(Right(42))
      }
    }.as(Some(IO("cancelled").debug.void)) // finalizer in case the computation gets cancelled
  }

  val asyncMeaningOfLifeCompolex_v2: IO[Int] = asyncIO.async {(cb: Callback[Int]) =>
    IO {
      threadPool.execute { () =>
        println(s"[${Thread.currentThread().getName}] computing an async mol")
        cb(Right(42))
      }
    }.as(Some(IO("cancelled").debug.void)) // finalizer in case the computation gets cancelled
  } // same

  val myExecutionContext = ExecutionContext.fromExecutorService(threadPool)
  val asyncMeaningOfLife_v3 = asyncIO.evalOn(IO(42).debug, myExecutionContext).guarantee(IO(threadPool.shutdown()))

  // never
  val neverIO = asyncIO.never

  /*
    exercises
    1 - implement never and async_ in terms of the big async
    2 - tuple two effects with different requirements
   */
  // 2
  import cats.syntax.functor._ // map
  import cats.syntax.flatMap._ // flatMap

  def firstEffect[F[_], A](a: A)(implicit concurrent: Concurrent[F]): F[A] = Concurrent[F].pure(a)
  def secondEffect[F[_], A](a: A)(implicit sync: Sync[F]): F[A] = Sync[F].pure(a)

  def tupledEffect[F[_], A](a: A)(implicit async: Async[F]): F[(A, A)] = for {
    first <- firstEffect(a)
    second <- secondEffect(a)
  } yield (first, second)

  override def run: IO[Unit] = tupledEffect[IO, Int](42).void

}
