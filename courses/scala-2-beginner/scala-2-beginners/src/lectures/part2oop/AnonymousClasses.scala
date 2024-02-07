package lectures.part2oop

object AnonymousClasses extends App {

  abstract class Animal {
    def eat: Unit
  }

  //anonymous class
  val funnyAnimal: Animal = new Animal {
    override def eat: Unit = println("hahaha")
  }

  /*
  equivalent to (what the compiler does behind the scenes):

  class AnonymousClasses$$anon$1 extends Animal {
    override def eat: Unit = println("hahaha")
  }
  val funnyAnimal: Animal = new AnonymousClasses$$

   */

  println(funnyAnimal.getClass)

  class Person(name: String) {
    def sayHi: Unit = println(s"hi, my name is $name. what can I do for you?")
  }

  val jim = new Person("Jim") {
    override def sayHi: Unit = println(s"hi, my name is Jim. how can I be of service?")
  }

  println(jim.sayHi)
}
