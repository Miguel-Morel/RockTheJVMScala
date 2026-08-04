package com.rockthejvm.part2effects

import scala.concurrent.Future

object Effects {

  // pure functional programming
  // rely on substitution
  def combine(a: Int, b: Int): Int = a + b
  val five = combine(2,3)
  val five_v2 = 2 + 3
  val five_v3 = 5

  // referential transparency: can replace an expression with its value as many times as we want without changing behavior

  // example: print to the console
  val printSomething: Unit = println("cats effect")
  val printSomething_v2: Unit = () // not the same

  // example: change a variable
  var anInt = 0
  val changingVar: Unit = anInt += 1
  val changingVar_v2: Unit = () // not the same

  // side effects are inevitable for useful programs

  // effect

  /*
    effect types
    properties:
    - type signature describes the kind of calculation that will be performed
    - type signature describes the value that will be calculated
    - when side effects are needed, effect construction is separate from effect execution
   */

  /*
    example: Option is an effect type
    - describes a possibly absent value
    - computes a value of type A, if it exists
    - side effects are not needed
   */
  val anOption: Option[Int] = Option(42)

  /*
    example: Future -> is NOT an effect type
    - describes an asynchronous computation
    - computes a value of type A, if it's successful
    - side effect is required (allocating/scheduling a thread). execution is NOT separate from construction
   */
  import scala.concurrent.ExecutionContext.Implicits.global
  val aFuture: Future[Int] = Future(42)

  /*
   example: MyIO data type from the Monads Scala 3 lesson - it IS an effect type
   - describes any computation that might produce side effects
   - calculates a value of type A, if it's successful
   - side effects are required for the evaluation of () => A. yes, the creation of MyIO does NOT produce side effects on construction
   */
  case class MyIO[A](unsafeRun: () => A) {
    def map[B](f: A => B): MyIO[B] =
      MyIO(() => f(unsafeRun()))

    def flatMap[B](f: A => MyIO[B]): MyIO[B] =
      MyIO(() => f(unsafeRun()).unsafeRun())
  }

  val anIO: MyIO[Int] = MyIO(() => {
    println("I'm writing something...")
    42
  })




  def main(args: Array[String]): Unit = {
    anIO.unsafeRun()
  }

}
