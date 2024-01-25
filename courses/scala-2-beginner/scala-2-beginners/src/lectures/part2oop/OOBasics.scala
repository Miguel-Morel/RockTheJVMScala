package lectures.part2oop

object OOBasics extends App {

  val person = new Person("John", 26)
  println(person.x)
  person.greet("Daniel")
  person.greet()

  val author = new Writer("Fyodor", "Dostoevsky", 1821)
  val imposter = new Writer("Fyodor", "Dostoevsky", 1821)
  val novel = new Novel("Crime and Punishment", 1866, author)

  println(novel.authorAge)
  println(novel.isWrittenBy(imposter))

  val counter = new Counter
  counter.inc.print
  counter.inc.inc.inc.print
  counter.inc(10).print
}

//constructor
class Person(name: String, val age: Int) {
  //body
  val x = 2

  println(1 + 3)

  //method (function called inside class definition)
  def greet(name: String): Unit = println(s"${this.name} says: Hi, $name")

  //overloading (same method name, different signatures)
  def greet(): Unit = println(s"Hi, I am $name")

  //multiple constructors
  def this(name: String) = this(name, 0)
  def this() = this("John Doe")
}

//class parameters are NOT FIELDS

/*
  Novel and a Writer

  Writer: first name, last name, year
    - method fullName (concatenation of firstName and lastName)
  Novel: name, year of release, author
    - authorAge (age of author at time of release)
    - isWrittenBy (author)
    - copy (new year of release) = new instance of Novel

 */

class Writer(firstName: String, lastName: String, val yearOfBirth: Int) {
  def fullName(): String = firstName + " " + lastName
}

class Novel(name: String, yearOfRelease: Int, author: Writer) {
  def authorAge = yearOfRelease - author.yearOfBirth
  def isWrittenBy(author: Writer) = author == this.author
  def copy(newYearOfRelease: Int): Novel = new Novel(name, newYearOfRelease, author)
}

/*
  Counter class
    - receives an int value
    - method currentCount
    - method to increment/decrement counter by 1 => new Counter
    - overload inc/dec to receive an amount
 */

class Counter(val count: Int = 0) {
  def inc = {
    println("incrementing")
    new Counter(count + 1)
    } //immutability (when modifying a val, need to return a new instance instead of modifying the value (ie.java))
  def dec = {
    println("decrementing")
    new Counter(count - 1)
    }

  //overloading above methods
  def inc(n: Int): Counter = {
    if (n <= 0) this
    else {
      inc.inc(n-1)
    }
  }
  def dec(n: Int): Counter = {
    if(n <= 0) this
    else inc.inc(n-1)
  }
  def print = println(count)
}
