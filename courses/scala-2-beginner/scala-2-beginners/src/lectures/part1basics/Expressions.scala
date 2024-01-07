package lectures.part1basics

object Expressions extends App {

  val x = 1 + 2 //expression
  println(x)

  println(2 + 3 * 4) // + - * / & | ^ << >> >>> (right shift with zero extension)

  println(1 == x) // == != > >= < <=

  println(!(1 == x)) // ! && ||

  var aVariable = 2
  aVariable += 3 //also works with -= *= /= ....... side effects (change variables)
  println(aVariable)

  //instructions (do) vs expressions (value)

  // if expression
  val aCondition = true
  val aConditionedValue = if(aCondition) 5 else 3 // if expression
  println(aConditionedValue)
  println(if(aCondition) 5 else 3)

  var i = 0
  val aWhile = while (i < 10) {
    println(i)
    i += 1
  }

  //NEVER WRITE THIS ^^^ AGAIN.

  //EVERYTHING in Scala is an expression

  val aWeirdValue = (aVariable = 3) //Unit === void
  println(aWeirdValue)

  //side effects: println(), whiles, reassigning

  //code blocks
  val aCodeBlock = {
    val y = 2
    val z = y + 1

    if (z > 2) "hello" else "goodbye"
  }

  //exercises

  //1. difference between "hello world" vs println("hello world")?
  // former is a String and latter is Unit

  //2. what's the value of the following expressions?
  val someValue = {
    2 < 3
  }
  //true, because it evaluates if 2 is less than 3, which is a boolean operation that evaluates to true.

  val someOtherValue = {
    if(someValue) 239 else 986
    42
  }
  //42

}
