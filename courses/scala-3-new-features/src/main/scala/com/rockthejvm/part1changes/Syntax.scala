package com.rockthejvm.part1changes

object Syntax {

  // if
  val ifExpression = if(2 > 3) "bigger" else "smaller"
  val ifExpression_v2 = if 2 > 3 then "bigger" else "smaller" // scala 3

  // multiline ifs
  val ifExpression_v3 =
    if(2 > 3) {
      val result = "bigger"
      // code
      result
    } else {
      val result = "smaller"
      // code
      result
    }

  // scala 3: braceless
  val ifExpression_v4 =
    if 2 > 3 then
      val result = "bigger"
      // code
      result
    else
      val result = "smaller"
      // code
      result

      // ***indentation matters
      //while
  val whileExpression: Unit = while (2 > 3) {
    println("bigger")
    println("much bigger")
  }

      // scala 3: braceless
  val whileExpression_v2: Unit = while 2 > 3 do
    println("bigger")
    println("much bigger")
    // indentation matters

    // for
  val forComprehension =
    for {
      num <- List(1,2,3)
      char <- List('a', 'b')
    } yield s"$num-$char"

  // scala 3
  val forComprehension_v2 =
    for
      num <- List(1, 2, 3)
      char <- List('a', 'b')
    yield s"$num-$char"

  // match
  val meaningOfLife = 42
  val aPatternMatch = meaningOfLife.match {
    case 1 => "the one"
    case 2 => "double or nothing"
    case 3 => "something else"
  }

  val aPatternMatch_v2 = meaningOfLife match
    case 1 => "the one"
    case 2 => "double or nothing"
    case _ => "something else"

    // try-catch
  val tryCatch =
    try {
      "".charAt(2) // throws an IndexOutOfBounds expcetion
    } catch {
      case oobE: IndexOutOfBoundsException => '_'
      case e: Exception => 'z'
    }

  // scala 3 braceless
  val tryCatch_v2 =
    try
      "".charAt(2)
    catch
      case oobE: IndexOutOfBoundsException => '_'
      case e: Exception => 'z'

  // significant indentation
  def computeMeaningOfLife(arg: Int): Int = // significant indentation activated here
    val partialResult = 49


    // code(blank)


    partialResult + arg + 2 // <- part of the code block/method implementation
    // "phantom" code block" ends with the significant indentation

  def isPrime(n: Int): Boolean =
    def aux(potentialDivisor: Int): Boolean =
      if(potentialDivisor > n / 2) true
      else if (n % potentialDivisor == 0) false
      else aux(potentialDivisor + 1)

    aux(2)
  end isPrime // <- scala 3: for large code blocks


  // significant indentation region token ':', for classes, traits, objects, nums

  // indentation: number of whitespaces before a non-whitespace chars in the line

  /*
    indentation = # of whitespace chars (spaces + tabs)
    use the same type of indentation through your code
   */

  // significant indentation regions
  class Animal {
    def eat(): Unit = {
      println("I'm eating")
    }
  }

  // scala 3
  class AnimalV2: // indentatation region
    def eat(): Unit =
      println("Im eating")
    def grow(): Unit =
      println("I'm growing")
  end AnimalV2



  def main(args: Array[String]): Unit = {

  }

}
