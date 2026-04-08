package lectures.part3fp

object AnonymousFunctions extends App {

//  val doubler = new Function1[Int, Int] {
//    override def apply(x: Int): Int = x * 2
//  }

  // anonymous function (LAMBDA)
  val doubler = (x: Int) => x * 2

  // multiple params in a lambda
  val adder: (Int, Int) => Int = (a: Int, b: Int) => a + b

  // no params
  val justDoSomething: () => Int = () => 3

  // careful: lambdas MUST be called with parentheses
  println(justDoSomething) // function itself
  println(justDoSomething()) // function call

  // curly braces with lambdas
  val stringToInt = { (str: String) =>
    str.toInt
  }

  // MOAR syntactic sugar
  val niceIncrementer: Int => Int = (x: Int) => x + 1
  val niceIncrementerAlt: Int => Int =  _ + 1 // equivalent to x => x + 1

  val niceAdder: (Int, Int) => Int = (a, b) => a + b
  val niceAdderAlt: (Int, Int) => Int = _ + _ // equivalent to (a,b) => a + b

  /*
    1. MyList: replace all FunctionX calls with lambdas
    2. rewrite the "special" adder as an anonymous function
   */

}
