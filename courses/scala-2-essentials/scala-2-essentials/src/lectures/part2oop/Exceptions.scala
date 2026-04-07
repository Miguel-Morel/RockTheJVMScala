package lectures.part2oop

object Exceptions extends App {

  val x: String = null
//  println(x.length)
  // this will carsh with a Null Pointer Exception (NPE)

  // 1. throwing exceptions

//  val aWeirdValue: String = throw new NullPointerException

  // throwable classes extend the Throwable class
  // Exception and Error are the major Throwable subtypes

  // 2. catching exceptions
  def getInt(withExceptions: Boolean): Int = {
    if (withExceptions) throw new RuntimeException("No int for you!")
    else 42
  }

  val potentialFail = try{
    // code that might fail
    getInt(false)
  } catch  {
    case e: RuntimeException => 43
  } finally {
    //code that will get executed NO MATTER WHAT
    // finally block is optional and does not influence the return type of this expression
    // use finally only for side effects (ie. writing something to a file in the context of distributed applications)
    println("finally")
  }

  println(potentialFail)

  // 3. how to define your own exceptions
  class MyException extends Exception
  val exception = new MyException

//  throw exception

  /*
 1. crash your program with an OutOfMemoryError
 2. crash with stack overflow error
 3. PocketCalculator
     -add(x,y)
     -subtract(x,y)
     -multiply(x,y)
     -divide(x,y)

     Throw
       -OverflowException if add(x,y) exceeds Int.MAX_VALUE
       -UnderflowException if subtract(x,y) exceeds Int.MIN_VALUE
       -MathCalculationException for division by 0
 */

  //1.
//  val array = Array.ofDim(Int.MaxValue)
// OOM

  //2.
//  def infinite: Int = 1 + infinite
//  val noLimit = infinite
// SO

  //3.
  class OverflowException extends RuntimeException
  class UnderflowException extends RuntimeException
  class MathCalculationException extends RuntimeException("Division by 0")

  object PocketCalculator {
    def add(x: Int, y: Int)= {
      val result = x + y
      if (x > 0 && y > 0 && result < 0) throw new OverflowException
      else if (x < 0 && y < 0 && result > 0) throw new UnderflowException
      else result
    }

    def subtract(x: Int, y: Int)= {
      val result = x - y
      if (x > 0 && y < 0 && result < 0) throw new OverflowException
      else if (x < 0 && y > 0 && result > 0) throw new UnderflowException
      else result
    }

    def multiply(x: Int, y: Int) = {
      val result = x * y
      if (x > 0 && y > 0 && result < 0) throw new OverflowException
      else if (x < 0 && y < 0 && result < 0) throw new OverflowException
      else if (x > 0 && y < 0 && result > 0) throw new UnderflowException
      else if (x < 0 && y > 0 && result > 0) throw new UnderflowException
      else result
    }

    def divide (x: Int, y: Int) = {
      val result = x / y
      if (y == 0) throw new MathCalculationException
      else result
    }


  }

//  println(PocketCalculator.add(Int.MaxValue, 10))
println(PocketCalculator.divide(2, 0))

}


