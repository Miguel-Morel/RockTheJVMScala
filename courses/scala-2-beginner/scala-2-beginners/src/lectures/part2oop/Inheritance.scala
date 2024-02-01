package lectures.part2oop

object Inheritance extends App {

  //single class inheritance
  sealed class Animal {
    val creatureType = "wild"
    def eat = println("nomnomnom")
  }

  class Cat extends Animal {
    def crunch = {
      eat
      println("crunch")
    }
  }

  val cat = new Cat
  cat.crunch

  //constructors
  class Person(name: String, age: Int) {
    def this(name: String) = this(name, 0)
  }
  class Adult(name: String, age: Int, idCard: String) extends Person(name)

  //overriding
  class Dog(override val creatureType: String) extends Animal {
//    override val creatureType: String = "domestic"
    override def eat =  {
      super.eat
      println("crunch, crunch")
    }
  }

  val dog = new Dog("K9")
  dog.eat
  println(dog.creatureType)

  //type substitution (broad: polymorphism)
  val unknownAnimal: Animal = new Dog("K9")
  unknownAnimal.eat

  //overriding = supplying different implementations in different classes
  //overloading = supplying multiple methods with different signatures but with the same name and the same class

  //super = used to reference fields and methods from a parent class

  //preventing overrides
  // 1 - use final on member
  // 2 - use final on class itself
  // 3 - seal the class = extend classes IN THIS FILE ONLY, prevent extension in other files



}
