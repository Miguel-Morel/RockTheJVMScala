package lectures.part3concurrency

import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Random, Success}
import scala.concurrent.duration._

// important for futures
import scala.concurrent.ExecutionContext.Implicits.global

object FuturesPromises extends App {

  // futures are a functional way of computing something on parallel or on another thread

  def calculateMeaningOfLife: Int = {
    Thread.sleep(2000)
    42
  }

  val aFuture = Future {
    calculateMeaningOfLife // calculates the meaning of life on another thread
  } // (global) - passed by compiler

  println(aFuture.value) // Option[Try[Int]]

  println("waiting on the future")
  aFuture.onComplete {
    case Success(meaningOfLife) => println(s"the meaning of life is $meaningOfLife")
    case Failure(e) => println(s"I failed with $e")
  } // use the value of the future when it completes. will be called by SOME thread.

  Thread.sleep(3000)



  // mini social network
  // needs to be done asynchronously

  case class Profile(id: String, name: String) {
    def poke(anotherProfile: Profile) =
      println(s"${this.name} poking ${anotherProfile.name}")
  }

  object SocialNetwork {
    // "database"
    val names = Map(
      "fb.id.1-zuck" -> "Mark",
      "fb.id.2-bill" -> "Bill",
      "fb.id.2-dummy" -> "Dummy",
    )

    val friends = Map(
      "fb.id.1-zuck" -> "fb.id.2-bill"
    )

    val random = new Random()

    // API
    def fetchProfile(id: String): Future[Profile] = Future {
      // fetching from db, etc
      Thread.sleep(random.nextInt(300))
      Profile(id, names(id))
    }

    def fetchBestFriend(profile: Profile): Future[Profile] = Future {
      Thread.sleep(random.nextInt(400))
      val bfId = friends(profile.id)
      Profile(bfId, names(bfId))
    }
  }

    // client: mark poke bill
    val mark = SocialNetwork.fetchProfile("fb.id.1-zuck")
//    mark.onComplete{
//      case Success(markProfile) =>
//        val bill = SocialNetwork.fetchBestFriend(markProfile)
//        bill.onComplete {
//          case Success(billProfile) => markProfile.poke(billProfile)
//          case Failure(e) => e.printStackTrace()
//        }
//      case Failure(ex) => ex.printStackTrace()
//
//    }



  // functional composition of futures
  // map, flatMap, filter
  val nameOnTheWall = mark.map(profile => profile.name)
  val marksBestFriend = mark.flatMap(profile => SocialNetwork.fetchBestFriend(profile))
  val zucksBestFriendRestricted = marksBestFriend.filter(profile => profile.name.startsWith("Z"))

  // for-comprehensions
  for {
    mark <- SocialNetwork.fetchProfile("fb.id.1-zuck")
    bill <- SocialNetwork.fetchBestFriend(mark)
  } mark.poke(bill)

  Thread.sleep(1000)

  // fallbacks
  val aProfileNoMatterWhat = SocialNetwork.fetchProfile("unknown id").recover {
    case e: Throwable => Profile("fb.id.0-dummy", "forever alone")
  }

  val aFetchedProfileNoMatterWhat = SocialNetwork.fetchProfile("unknown id").recoverWith {
    case e: Throwable => SocialNetwork.fetchProfile("fb.id.0-dummy")
  }
  val fallBackResult = SocialNetwork.fetchProfile("unknown id").fallbackTo(SocialNetwork.fetchProfile("fb.id.0-dummy"))

  // online banking app
  case class User(name: String)
  case class Transaction(sender: String, receiver: String, amount: Double, status: String)

  object BankingApp {
    val name = "Rock the JVM Banking"

    def fetchUser(name: String): Future[User] = Future {
      // simulate fetching from db
      Thread.sleep(500)
      User(name)
    }

    def createTransaction(user: User, merchantName: String, amount: Double): Future[Transaction] = Future {
      // simulate some processes
      Thread.sleep(1000)
      Transaction(user.name, merchantName, amount, "success")
    }

    def purchase(username: String, item: String, merchantName: String, cost: Double): String = {
      // fetch user from db
      // create a transaction
      // WAIT for transaction to finish, otherwise purchase return would fail
      val transactionStatusFuture = for {
        user <- fetchUser(username)
        transaction <- createTransaction(user, merchantName, cost)
      } yield transaction.status

      Await.result(transactionStatusFuture, 2.seconds) // implicit conversions -> pimp my library
    }
  }

  println(BankingApp.purchase("Daniel", "iphone12", "rock the jvm store", 3000))

  // promises - manual manipulation of futures
  val promise = Promise[Int]() // "controller" over a future
  val future = promise.future

  // thread1 - "consumer"
  future.onComplete {
    case Success(r) => println("[consumer] I've received " + r)
  }

  // thread2 - "producer"
  val producer = new Thread(() => {
    println("[producer] crunching numbers...")
    Thread.sleep(500)
    // "fulfilling" the promise
    promise.success(42)
    println("[producer] done")
  })

  producer.start()
  Thread.sleep(1000)
}
