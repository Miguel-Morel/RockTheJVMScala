package com.rockthejvm.part3concurrency

import cats.effect.kernel.Outcome.{Canceled, Errored, Succeeded}
import cats.effect.kernel.Resource
import cats.effect.{IO, IOApp}
import com.rockthejvm.utils.DebugWrapper

import java.io.{File, FileReader}
import java.util.Scanner
import scala.concurrent.duration.DurationInt

object Resources extends IOApp.Simple {

  // use-case: manage a connection lifecycle
  class Connection(url: String) {
    def open: IO[String] = IO(s"opening connection to $url").debug
    def close: IO[String] = IO(s"closing connection to $url").debug
  }

  // problem: leaking resources
  val asyncFetchUrl = for {
    fib <- (new Connection("rockthejvm.com").open *> IO.sleep(Int.MaxValue.seconds)).start
    _ <- IO.sleep(1.second) *> fib.cancel
  } yield ()

  // proper way
  val correctAsyncFetchUrl = for {
    conn <- IO(new Connection("rockthejvm.com"))
    fib <- (conn.open *> IO.sleep(Int.MaxValue.seconds)).onCancel(conn.close.void).start
    _ <- IO.sleep(1.second) *> fib.cancel
  } yield ()

  /*
    bracket pattern: someIO.bracket(useResourceCallback)(releaseResourceCallback)
    bracket is equivalent to try-catches (pure fp)
   */

  // optimal way: bracket pattern - way to write code that compels the writing of cancellations
  val bracketFetchUrl = IO(new Connection("rockthejvm.com"))
    .bracket(conn => conn.open *> IO.sleep(Int.MaxValue.seconds))(conn => conn.close.void)

  val bracketProgram = for {
    fib <- bracketFetchUrl.start
    _ <- IO.sleep(1.second) *> fib.cancel
  } yield ()

  /*
    exercise: read the file with the bracket pattern
    - open a scanner
    - read the file line by line, every 100 millis
    - close the scanner
    - if cancelled/throws error, close the scanner
   */

  def openFileScanner(path: String): IO[Scanner] =
    IO(new Scanner(new FileReader(new File(path))))

  def readLineByLine(scanner: Scanner): IO[Unit] =
    if(scanner.hasNextLine) IO(scanner.nextLine()).debug >> IO.sleep(100.millis) >> readLineByLine(scanner)
    else IO.unit

  def bracketReadFile(path: String): IO[Unit] =
    IO(s"opening file at $path") >>
      openFileScanner(path).bracket { scanner =>
        readLineByLine(scanner)
      } { scanner =>
        IO(s"closing file at $path").debug >> IO(scanner.close())
      }

  /*
    resources
   */
   def connFromConfig(path: String): IO[Unit] =
     openFileScanner(path)
       .bracket { scanner =>
         // acquire a connection based on the file
         IO(new Connection(scanner.nextLine())).bracket { conn =>
           conn.open >> IO.never
         }(conn => conn.close.void)
       }(scanner => IO("closing file").debug >> IO(scanner.close()))
   // nesting resources is tedious...


  // ...enter Resources
  val connectionResource = Resource.make(IO(new Connection("rockthejvm.com")))(conn => conn.close.void)

  // ... at a later part of your code
  val resourceFetchUrl = for {
    fib <- connectionResource.use(conn => conn.open >> IO.never).start
    _ <- IO.sleep(1.second) >> fib.cancel
  } yield ()

  // resources are equivalent to brackets
  val simpleResource = IO("some resource")
  val usingResource:String => IO[String] = string => IO(s"using the string: $string").debug
  val releaseResource: String => IO[Unit] = string => IO(s"finalizing the string: $string").debug.void

  val usingResourceWithBracket = simpleResource.bracket(usingResource)(releaseResource) // equivalent to below
  val usingResourceWithResource = Resource.make(simpleResource)(releaseResource).use(usingResource) // equivalent to above

  /*
    exercise: read a text file with one line every 100 millis using Resource
    (refactor bracket exercise to use Resource)
   */
  def getResourceFromFile(path: String) = Resource.make(openFileScanner(path)) { scanner =>
    IO(s"closing file at $path").debug >> IO(scanner.close())
  }

  def resourceReadFile(path: String) =
    IO(s"opening file at $path") >>
    getResourceFromFile(path).use { scanner =>
      readLineByLine(scanner)
    }

  def cancelReadFile(path: String) = for {
    fib <- resourceReadFile(path).start
    _ <- IO.sleep(2.seconds) >> fib.cancel
  } yield ()

  // nested resources
  def connFromConfigResource(path: String) = {
    Resource.make(IO("opening file").debug >> openFileScanner(path))(scanner => IO("closing file").debug >> IO(scanner.close()))
      .flatMap(scanner => Resource.make(IO(new Connection(scanner.nextLine())))(conn => conn.close.void))
  }

  // equivalent
  def connFromConfigResourceClean(path: String) = for {
    scanner <- Resource.make(IO("opening file").debug >> openFileScanner(path))(scanner => IO("closing file").debug >> IO(scanner.close()))
    conn <- Resource.make(IO(new Connection(scanner.nextLine())))(conn => conn.close.void)
  } yield conn

  val openConnection = connFromConfigResourceClean("/Users/mmorel/GitHub/RockTheJVMScala/courses/cats-effect/src/main/resources/connection.txt").use(conn => conn.open >> IO.never)
  val canceledConnection = for {
    fib <- openConnection.start
    _ <- IO.sleep(1.second) >> IO("cancelling").debug >> fib.cancel
  } yield ()
  // connection + file will close automatically

  // it's possible to add finalizers to regular IOs
  val ioWithFinalizer = IO("some resource").debug.guarantee(IO("freeing resource").debug.void)
  val ioWithFinalizer_v2 = IO("some resource").debug.guaranteeCase {
    case Succeeded(fa) => fa.flatMap(result => IO(s"releasing resource: $result").debug).void
    case Errored(_) => IO("nothing to release").debug.void
    case Canceled() => IO("resource got canceled. releasing what's left").debug.void
  }

  override def run: IO[Unit] =
//    bracketReadFile("/Users/mmorel/GitHub/RockTheJVMScala/courses/cats-effect/src/main/scala/com/rockthejvm/part3concurrency/Resources.scala")
//  resourceFetchUrl.void
//  resourceReadFile("/Users/mmorel/GitHub/RockTheJVMScala/courses/cats-effect/src/main/scala/com/rockthejvm/part3concurrency/Resources.scala")
//    cancelReadFile("/Users/mmorel/GitHub/RockTheJVMScala/courses/cats-effect/src/main/scala/com/rockthejvm/part3concurrency/Resources.scala").void
//  openConnection.void
//  canceledConnection
  ioWithFinalizer_v2.void

}
