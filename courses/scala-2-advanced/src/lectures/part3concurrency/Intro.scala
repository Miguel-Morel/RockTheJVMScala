package lectures.part3concurrency

import java.util.concurrent.Executors

object Intro extends App {

  /*
    interface Runnable{
      public void run()
    }
   */
  // JVM threads
  // thread = instance of a class
  val runnable = new Runnable {
    override def run(): Unit = println("running in parallel")
  }
  val aThread = new Thread(runnable)

  //create a JVM thread which runs on an OS thread
  aThread.start() // gives the signal to the JVM to start the thread
  runnable.run() // doesn't do anything in parallel
  aThread.join() // blocks until a thread has finished running

  val threadHello = new Thread(() => (1 to 5).foreach(_ => println("hello")))
  val threadGoodbye = new Thread(() => (1 to 5).foreach(_ => println("goodbye")))
//  threadHello.start()
//  threadGoodbye.start()
  // different runs in multithreaded environments produce different results

  // executors
  val pool = Executors.newFixedThreadPool(10)
//  pool.execute(() => println("something in the thread pool"))

//  pool.execute(() => {
//    Thread.sleep(1000)
//    println("done after 1 second")
//  })
//
//  pool.execute(() => {
//    Thread.sleep(1000)
//    println("almost done")
//    Thread.sleep(1000)
//    println("done after 2 seconds")
//  })

  pool.shutdown()
//  pool.execute(() => println("should not appear")) // throws an exception in the calling thread

//  pool.shutdownNow() // interrupts the sleeping threads that are actually running under the pool. if they're sleeping, they will throw exceptions

  println(pool.isShutdown) // true

  def runInParallel = {
    var x = 0

    val thread1 = new Thread(() => {
      x = 1
    })

    val thread2 = new Thread(() => {
      x = 2
    })

    thread1.start()
    thread2.start()

    println(x)
  }

  // race condition: two threads are trying to set the memory zone at the same time
//  for(_ <- 1 to 10000) runInParallel

  class BankAccount(var amount: Int) {
    override def toString: String = "" + amount
  }

  def buy(account: BankAccount, thing: String, price: Int) = {
    account.amount -= price // account.amount = account.acmount - price
//    println("I've bought " + thing)
//    println("my account is now " + account)
  }

//  for (_ <- 1 to 10000) {
//    val account = new BankAccount(50000)
//    val thread1 = new Thread(() => buy(account, "shoes", 3000))
//    val thread2 = new Thread(() => buy(account, "iphone12",4000))
//
//    thread1.start()
//    thread2.start()
//    Thread.sleep(10)
//    if (account.amount != 43000) println("aha: " + account.amount)
////    println()
//  }

  /*
    thread1 (shoes): 50000
      - account = 50000 - 3000 = 47000
    thread2 (ophone12): 50000
      - account = 50000 - 4000 = 46000 overwrites the memory of account.amount
   */

  // option #1: use synchronized()
  def buySafe(account: BankAccount, thing: String, price: Int) = {
    // no two threads can evaluate this at the same time
    account.synchronized {
      account.amount -= price
      println("I've bought " + thing)
      println("my account is now " + account)
    }

  // option #2: use @volatile
    // note: @volatile only protects against concurrent single operations. the -= operator is NOT a single op: it consists of a read, a compute, and a write. to be safe: use synchronized
  }

  /*
    Exercise

    1. construct 50 "inception" threads
      thread1 -> thread2 -> thread3 -> ...
      println("hello from thread#3")
      in _reverse_ order
   */

  def inceptionThreads(maxThreads: Int, i: Int = 1): Thread = new Thread(() => {
    if (i < maxThreads) {
      val newThread = inceptionThreads(maxThreads, i + 1)
      newThread.start()
      newThread.join()
    }

    println(s"hello from thread $i")
  })

  inceptionThreads(50).start()

  /*
    2.
   */
  var x = 0
  val threads = (1 to 100).map(_ => new Thread(() => x += 1))
  threads.foreach(_.start())

  /*
    a) what is the biggest value possible for x? 100
    b) what is the smallest value possible for x? 1

    thread1: x = 0
    thread2: x = 0
      ...
    thread100: x = 0

    for all threads: x = 1 and write it back to x
   */

  threads.foreach(_.join())
  println(x)

  /*
    3. sleep fallacy
   */
  var message = ""
  val awesomeThread = new Thread(() => {
    Thread.sleep(1000)
    message = "scala is awesome"
  })

  message = "scala sucks"
  awesomeThread.start()
  Thread.sleep(1001)
  awesomeThread.join() // wait for awesomeThread to join -> makes sure awesomeThread has finished before executing println(message)
  println(message)

  /*
    what is the value for message? almost always will be "scala is awesome"
    is it guaranteed? NO
    why/why not?

    (main thread)
      message = "scala sucks"
      awesomeThread.start()
      sleep() - relieves execution
    (awesome thread)
      sleep() - relieves execution
    (OS gives the CPU to some important thread -> takes the CPU for more than 2 seconds)
    (OS is free to choose -> gives CPU back to main thread)
      println("scala sucks")
    (OS gives the CPU to awesomeThread)
      message = "scala is awesome"

    - sleeping does NOT guarantee that a thread will sleep exact number of ms. it will only yield the execution of the CPU to the OS for at least that number of ms
    - sleeping does NOT guarantee the order of evaluation of some expressions

   */

  /*
    how do we fix this?
    synchronizing does NOT work here -> only useful for concurrent modifications
    this is a sequential problem
    solve by .join()
   */

}
