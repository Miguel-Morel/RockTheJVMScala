package lectures.part2oop

import scala.language.postfixOps

object MethodNotations extends App {

  class Person(val name: String, favoriteMovie: String, val age: Int = 0) {
    def likes(movie: String): Boolean = movie == favoriteMovie
    def learns(something: String): String = s"$name learns $something"
    def learnsScala = this learns("Scala")
    def +(person: Person): String = s"${this.name} is hanging out with ${person.name}"
    def +(nickname: String): Person = new Person(s"$name ($nickname)", favoriteMovie)
    def unary_! : String = s"$name, what the heck?!"
    def unary_+ : Person = new Person(name, favoriteMovie, age + 1)
    def isAlive: Boolean = true
    def apply(): String = s"Hi, my name is $name and I like $favoriteMovie"
    def apply(n: Int): String = s"$name watched Inception $n times"
  }

  val mary = new Person("Mary", "Inception")
  println(mary.likes("Inception"))
  println(mary likes "Inception") //equivalent. this is called infix notation/operator notation

  //the above is an example of "syntactic sugar": nicer ways of writing code that are equivalent to more complex ways of writing code

  //"operators" in Scala
  val tom = new Person("Tom", "Fight Club")
  println(mary + tom)
  println(mary.+(tom))

  println(1 + 2)
  println(1.+(2))

  //ALL OPERATORS ARE METHODS
  //Akka actors have ! ?

  //prefix notation
  val x = -1 //equivalent to 1.unary_-
  val y = 1.unary_-

  //unary_ prefix onl works with - + ~ !

  println(!mary)
  println(mary.unary_!)

  //postfix notation
  println(mary.isAlive)
  println(mary isAlive)

  //apply
  println(mary.apply())
  println(mary()) //equivalent

  /*
    1. overload the + operator
       ex. mary + "the rockstar" => new person "Mary (the rockstar)"

    2. add an age to the Person class (val = 0)
       add a unary + operator => new person with the age + 1 (similar to ++ in java/c++)

    3. add a "learns" method in the Person class => "Mary learns Scala"
       add a learnsScala mehtod, calls learns method with "Scala"
       use it in postfix notation

    4. overload the apply method
       ex. mary.apply(2) => "Mary watched Inception 2 times"
   */


  /*
  1. overload the + operator
         ex. mary + "the rockstar" => new person "Mary (the rockstar)"
   */

  println((mary + "the rockstar")())
  println((mary + "the rockstar").apply()) //equivalent to above

  /*
  2. add an age to the Person class (val = 0)
     add a unary + operator => new person with the age + 1 (similar to ++ in java/c++)
   */
  println((+mary).age)  //1

  /*
  3. add a "learns" method in the Person class => "Mary learns Scala"
     add a learnsScala mehtod, calls learns method with "Scala"
     use it in postfix notation
   */
  println(mary learnsScala)

  /*
  4. overload the apply method
     ex. mary.apply(2) => "Mary watched Inception 2 times"
   */
  println(mary.apply(2))
  println(mary(2)) //equivalent


}



