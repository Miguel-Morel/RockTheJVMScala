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
  threadHello.start()
  threadGoodbye.start()
  // different runs in multithreaded environments produce different results

  // executors
  val pool = Executors.newFixedThreadPool(10)
  pool.execute(() => println("something in the thread pool"))

  pool.execute(() => {
    Thread.sleep(1000)
    println("done after 1 second")
  })

  pool.execute(() => {
    Thread.sleep(1000)
    println("almost done")
    Thread.sleep(1000)
    println("done after 2 seconds")
  })

  pool.shutdown()
//  pool.execute(() => println("should not appear")) // throws an exception in the calling thread

//  pool.shutdownNow() // interrupts the sleeping threads that are actually running under the pool. if they're sleeping, they will throw exceptions

  println(pool.isShutdown) // true

}
