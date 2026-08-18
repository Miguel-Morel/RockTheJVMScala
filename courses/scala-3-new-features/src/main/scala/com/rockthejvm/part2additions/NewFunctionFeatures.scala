package com.rockthejvm.part2additions

import scala.concurrent.{ExecutionContext, Future}

object NewFunctionFeatures {

  // generics in functions
  // scala 2 only had generic methods
  def processOption[A](option: Option[A]): String = option match {
    case Some(value) => s"[$value]"
    case None => "[]"
  }

  // in scala 3 we can add generics to function values
  val processOptionFunc: [A] => Option[A] => String = // syntax for function signature
    [A] => (option: Option[A]) => option match {
      case Some(value) => s"[$value]"
      case None => "[]"
    }

  // context functions - functions with using clauses/"implicit" args
  // in scala 2, only methods can have context args
  def methodWithoutContextArg(nonContextArg: Int)(noncontextArg2: String): String = ???
  def methodWitContextArg(nonContextArg: Int)(using context: String): String = ???

  // in scala 3, function values can also include context args
  // eta-expansion
  val functionWithoutContextArg = methodWithoutContextArg
  // eta-expansion also works for methods with context args
  val functionWithContextArgs: Int => String ?=> /* <-- using clauses must be denoted with ? */ String = methodWitContextArg

  // require given instances at the call site instead of definition
//  given ec: ExecutionContext = ???
//  val incrementAsync: Int => Future[Int] = x => Future(x * 1000) // can only work if provided the given ec here, where it's being called

  val incrementAsync: ExecutionContext ?=> Int => Future[Int] = x => Future(x * 1000)

  // later, in some other part of the code
  given ec: ExecutionContext = ???
  val aList = List(1,2,3).map(incrementAsync) // I require the ec at call site, not at definition

  // parameter untupling
  val tuples = List((1,2), (2,3), (3,4))
//  tuples.map((a, b) => a + b ) // was not possible in scala 2
  tuples.map {
    case (a, b) => a + b
  }

  // scala 3 does automatic untupling
  tuples.map((a, b) => a + b)



  def main(args: Array[String]): Unit = {
    println(processOptionFunc(Some(1))) // ok in scala 3
    println(processOptionFunc(Some("scala 3"))) // ok in scala 3

  }

}
