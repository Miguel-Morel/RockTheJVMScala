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

  /*
    1. generic trait MyPredicate[-T] with a little method test(T) => Boolean
    2. generic trait MyTransformer[-A, B] with a method transform(A) => B
    3. MyList:
      -map(transformer) => MyList
      -filter(predicate) => MyList
      -flatMap(transformer from A to MyList[B] => MyList[B]

      class EvenPredicate extends MyPredicate[Int]
      class StringToIntTransformer extends MyTransformer[String, Int]

      [1,2,3].map(n * 2) = [2,4,6]
      [1,2,3,4].filter(n % 2) = [2,4]
      [1,2,3].flatMap(n => [n, n+1]) => [1,2,2,3,3,4]
   */

}
