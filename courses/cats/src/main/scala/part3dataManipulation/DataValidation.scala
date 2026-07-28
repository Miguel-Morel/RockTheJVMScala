package part3dataManipulation

import cats.Semigroup

import scala.annotation.tailrec
import scala.util.Try

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

  // todo 1: use Either

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

  // chain
  aValidValue.andThen(_ => anInvalidValue)

  // test a valid value
  aValidValue.ensure(List("something went wrong"))(_ % 2 == 0)

  // transform
  aValidValue.map(_ + 1)
  aValidValue.leftMap(_.length)
  aValidValue.bimap(_.length, _ + 1)

  // interoperate with std lib
  val eitherToValidated: Validated[List[String], Int] = Validated.fromEither(Right(42))
  val optionToValidated: Validated[List[String], Int] = Validated.fromOption(None, List("nothing present here"))
  val tryToValidated: Validated[Throwable, Int] = Validated.fromTry(Try("something".toInt))

  // backwards
  aValidValue.toOption
  aValidValue.toEither

  // todo 2: form validation
  object FormValidation {
    import cats.instances.string._
    type FormValidation[T] = Validated[List[String], T]

    def getValue(form: Map[String, String], fieldName: String): FormValidation[String] =
      Validated.fromOption(form.get(fieldName), List(s"the field $fieldName must be specified"))

    def nonBlank(value: String, fieldName: String): FormValidation[String] =
      Validated.cond(value.length > 0, value, List(s"the field $fieldName must not be blank"))

    def emailProperForm(email: String): FormValidation[String] =
      Validated.cond(email.contains("@"), email, List("email is invalid"))

    def passwordCheck(password: String): FormValidation[String] =
      Validated.cond(password.length >= 10, password, List("passwords must be at least 10 characters long"))
    /*
      fields:
      - name
      - email
      - password

      rules:
      - above MUST be specified
      - name must not be blank
      - email must have "@"
      - password must have >= 10 characters
     */

    def validateForm(form: Map[String, String]): FormValidation[String] =
      getValue(form, "name").andThen(name => nonBlank(name, "name"))
        .combine(getValue(form, "email").andThen(emailProperForm))
        .combine(getValue(form, "password").andThen(passwordCheck))
        .map(_ => "user registration complete")
  }

  import cats.syntax.validated._
  val aValidMeaningOfLife: Validated[List[String], Int] = 42.valid[List[String]]
  val anError: Validated[String, Int] = "something went wrong".invalid[Int]

  def main(args: Array[String]): Unit = {

    val form = Map(
      "name" -> "daniel",
      "email" -> "daniel@rockthejvm.com",
      "password" -> "rockthejvm1!"
    )

    println(FormValidation.validateForm(form))
  }

}
