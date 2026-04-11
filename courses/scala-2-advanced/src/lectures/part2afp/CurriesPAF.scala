package lectures.part2afp

object CurriesPAF extends App {

  // curried functions
  val superAdder: Int => Int => Int =
    x => y => x + y

  val add3 = superAdder(3) // Int => Int = y => 3 + y
  println(add3(5))
  println(superAdder(3)(5)) // curried function - receives multiple parameter lists

  // method
  def curriedAdder(x: Int)(y: Int): Int = x + y // curried method

  // lifting = eta-expansion. lifting = transforming a method into a function
  val add4: Int => Int = curriedAdder(4)

  // functions != methods (JVM limitation given by methods being instances of classes/ part of singleton object ie. CurriesPAF, in this case)
  def inc(x: Int): Int = x + 1
  List(1,2,3).map(inc) // eta-expansion - List(1,2,3).map(x => inc(x))

  // partial function applications
  val add5 = curriedAdder(5) _ // do an eta expansion and convert this expression to Int => Int

  /*
    Exercise
   */
  val simpleAddFunction = (x: Int, y: Int) => x + y
  def simpleAddMethod(x: Int, y: Int) = x + y
  def curriedAddMethod (x: Int, y: Int) = x + y

  // add7: Int => Int = y => 7 + y
  // as many different implementations of add7 using the above
  val add7 = (x: Int) => simpleAddFunction(7, x)
  val add7_2 = simpleAddFunction.curried(7)
  val add7_6 = simpleAddFunction(7, _: Int)
//  val add7_3 = curriedAddMethod(7) _ // paf
//  val add7_4 = curriedAddMethod(7) (_) // paf = alternative syntax
  val add7_5 = simpleAddMethod(7, _: Int) // alternative syntax for turning methods into function values
  // y => simpleAddMethod(7, y)

  // underscores are powerful
  def concatenator(a: String, b: String, c: String) = a + b + c
  val insertName = concatenator("hello, I'm ", _: String, ", how are you?") // x: String => concatenator(hello, x, howareyou)
  println(insertName("Daniel"))

  val fillInTheBlanks = concatenator("hello, ", _: String, _: String) // (x,y) => concatenator("hello, ", x, y)
  println(fillInTheBlanks("Daniel", "scala is awesome"))

  /*
    Exercise

    1. process a list of numbers and return their string representations with different formats
        use the %4.2f, %8.6f and %14.12f with a curried formatter function.
        */
  def curriedFormatter(s: String)(number: Double): String = s.format(number)
  val numbers = List(Math.PI, Math.E, 1, 9.8, 1.3e-12)

  val simpleFormat = curriedFormatter("%4.2f") _ // lift into function value
  val seriousFormat = curriedFormatter("%8.6f") _
  val preciseFormat = curriedFormatter("%14.12f") _

  println(numbers.map(preciseFormat))
  println(numbers.map(curriedFormatter("%14.12f"))) // compiler does eta expansion

  /*
    2. difference between
      - functions vs methods
      - parameters: by-name vs 0-lambda
   */
  def byName(n: => Int) = n + 1
  def byFunction(f: () => Int) = f() + 1

  def method: Int = 42
  def parenMethod(): Int = 42

  /*
    calling byName and byFunction
    - int
    - method
    - parenMethod
    - lambda
    - paf
   */

  byName(23) // ok
  byName(method) // ok
  byName(parenMethod())
  byName(parenMethod) // ok but beware ==> byName(parenMethod))
//  byName(() => 42) // not ok
  byName((() => 42)()) // ok
//  byName(parenMethod _) // not ok

//  byFunction(45) // not ok
//  byFunction(method) // not ok!!!! does not do eta expansion
  byFunction(parenMethod) // compiler does eta expansion
  byFunction(() => 46) // ok
  byFunction(parenMethod _) // also works, but warning - unnecessary


}
