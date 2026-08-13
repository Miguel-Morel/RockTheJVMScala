package com.rockthejvm.part3concurrency

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
        IO(s"closing file at $path").debug *> IO(scanner.close())
      }

  override def run: IO[Unit] = bracketReadFile("/Users/mmorel/GitHub/RockTheJVMScala/courses/cats-effect/src/main/scala/com/rockthejvm/part3concurrency/Resources.scala")

}
