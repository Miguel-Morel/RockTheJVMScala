package com.rockthejvm.foundations

import cats.effect.{IO, IOApp}

object Doobie extends IOApp.Simple {

  case class Student(id: Int, name: String)



  override def run: IO[Unit] = ???

}
