package part3dataManipulation

import cats.Semigroup

import scala.annotation.tailrec

object DataValidation {

  import cats.data.Validated
  val aValidValue: Validated[String, Int] = Validated.valid(42) // "right" value
  val anInvalidValue: Validated[String, Int] = Validated.invalid("something went wrong") // "left" value
  val aTest: Validated[String, Int] = Validated.cond(42 > 39, 99, "meaning of life is too small")

  def testPrime(n: Int) = {
    @tailrec
    def tailRecPrime(d: Int): Boolean =
      if(d <= 1) true
      else n % d != 0 && tailRecPrime(d - 1)

    if(n == 0 || n == 1 || n == -1) false
    else tailRecPrime(Math.abs(n / 2))
  }

  // todo: use Either

  /*
    - n must be a prime
    - n must be non-negative
    - n <= 100
    - n must be even
   */

  def testNumber(n: Int): Either[List[String], Int] = {
    val isNotEven: List[String] = if(n % 2 == 0) List() else List("number must be even")
    val isNegative: List[String] = if(n >= 0) List() else List("number must be non-negative")
    val isTooBig: List[String] = if(n <= 100) List() else List("number must be less than or equal to 100")
    val isNotPrime: List[String] = if(testPrime(n)) List() else List("number must be a prime")

    if(n % 2 == 0 && n >= 0 && n <= 100 && testPrime(n)) Right(n)
    else Left(isNotEven ++ isNegative ++ isTooBig ++ isNotPrime)
  }

  import cats.instances.list._
  implicit val combineIntMax: Semigroup[Int] = Semigroup.instance[Int](Math.max)
  def validateNumber(n: Int): Validated[List[String], Int] =
    Validated.cond(n % 2 == 0, n, List("number must be even"))
      .combine(Validated.cond(n >= 0, n, List("number must be non-negative")))
      .combine(Validated.cond(n < 100, n, List("number must be less than or equal to 100")))
      .combine(Validated.cond(testPrime(n), n, List("number must be prime")))

  def main(args: Array[String]): Unit = {

  }

}
