package lectures.part2afp

object PartialFunctions extends App {

  val aFunction = (x: Int) => x + 1 // Function1[Int, Int] === Int => Int

  val aFuzzyFunction = (x: Int) =>
    if (x == 1) 42
    else if (x == 2) 56
    else if (x == 5) 999
    else throw new FunctionNotApplicableException

  class FunctionNotApplicableException extends RuntimeException

  val aNicerFuzzyFunction = (x: Int) => x match {
    case 1 => 42
    case 2 => 56
    case 3 => 999
  }
  // {1,2,5} => Int <--- partial function

  val aPartialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 56
    case 3 => 999
  } // partial function value (equivalent to above)

  println(aPartialFunction(2))
//  println(aPartialFunction(57499))

  // partial function utilities
  println(aPartialFunction.isDefinedAt(67))

  // can be lifted to total function returning Option
  val lifted = aPartialFunction.lift // Int => Option[Int]
  println(lifted(2))
  println(lifted(98))

  val pfChain = aPartialFunction.orElse[Int, Int] {
    case 45 => 67
  }

  println(pfChain(2))
  println(pfChain(45))

  // pf extend normal function
  val aTotalFunction: Int => Int = {
    case 1 => 99
  }

  // hofs accept partial functions as well
  val aMappedList = List(1,2,3).map {
    case 1 => 42
    case 2 => 78
    case 3 => 1000
//    case 5 => 1000 <--- MatchError
  }

  println(aMappedList)

  /*
    Note: pf can only have ONE parameter type
   */

  /*
    Exercises

    1. construct a pf instance (anonymous class)
    2. dumb chatbot as a pf
      scala.io.Source.stdin.getLines().foreach(line => println("you said: " + line))
   */

  // 1.
  val aManualFuzzyFunction = new PartialFunction[Int, Int] {
    override def apply(x: Int): Int = x match {
      case 1 => 42
      case 2 => 56
      case 3 => 999
    }

    override def isDefinedAt(x: Int): Boolean =
      x == 1 || x == 2 || x == 5
  }

  // 2.
  val chatbot: PartialFunction[String, String] = {
    case "hello" => "hi, my name is hal9000"
    case "goodbye" => "once you start talking to me, there is no return, human"
    case "call mom" => "unable to find your phone without your credit card"
  }

//  scala.io.Source.stdin.getLines().foreach(line => println("chatbot says: " + chatbot(line)))
  scala.io.Source.stdin.getLines().map(chatbot).foreach(println)
}
