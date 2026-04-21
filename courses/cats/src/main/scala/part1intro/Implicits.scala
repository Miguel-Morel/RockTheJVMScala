package part1intro

object Implicits {

  // implicit classes
  case class Person(name: String) {
    def greet: String = s"hi, my name is $name"
  }

  // implicit classes take a single argument. also known as an "extension method"
  implicit class ImpersonableString(name: String) {
    def greet: String = Person(name).greet

  }

//  val impersonableString = new ImpersonableString("Peter")
//  impersonableString.greet

  val greeting = "Peter".greet // new ImpersonableString("Peter").greet

  // importing implicit conversion in scope - "extension" example
  import scala.concurrent.duration._
  val oneSec = 1.second

  // implicit arguments and values
  def increment(x: Int)(implicit amount: Int) = x + amount
  implicit val defaultAmount = 10
  val incremented2 = increment(2) // increment(2)(10)

  def multiply(x: Int)(implicit times: Int) = x * times
  val times = multiply(2) // multiply(2)(10)

  // more complex example
  trait JSONSerializer[T] {
    def toJson(value: T): String
  }

  def listToJSON[T](list: List[T])(implicit serializer: JSONSerializer[T]): String =
    list.map(value => serializer.toJson(value)).mkString("[", ",", "]")

  implicit val personSerializer: JSONSerializer[Person] = new JSONSerializer[Person] {
    override def toJson(person: Person): String =
      s"""
        |{"name" : "${person.name}"}
        |""".stripMargin
  }

  val personsJson = listToJSON(List(Person("Alice"), Person("Bob")))

  // implicit arguments are used to PROVE THE EXISTENCE of a type

  // implicit methods
  implicit def oneArgCaseClassSerializer[T <: Product]: JSONSerializer[T] = new JSONSerializer[T] {
    override def toJson(value: T): String =
      s"""
         |{"${value.productElementName(0)}" : "${value.productElement(0)}"}
         |""".stripMargin.trim
  }

  case class Cat(catName: String)

  // in the background: val catsToJson = listToJson(List(Cat("Tom"), Cat("Garfield")))(oneArgCaseClassSerializer[Cat])
  val catsToJson = listToJSON(List(Cat("Tom"), Cat("Garfield")))

  // implicit methods are used to PROVE THE EXISTENCE of a typ
  // implicit methods can also be used for implicit conversions, but this practice is discouraged because people might call methods that are not available for their type, without being aware that their type is being converted. rather, it's better to use implicit classes for implicit conversions.

  


  def main(args: Array[String]): Unit = {
    println(oneArgCaseClassSerializer[Cat].toJson(Cat("Garfield")))
    println(oneArgCaseClassSerializer[Person].toJson(Person("David")))

  }

}
