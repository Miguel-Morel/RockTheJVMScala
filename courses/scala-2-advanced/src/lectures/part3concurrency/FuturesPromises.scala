package lectures.part3concurrency

import scala.concurrent.Future
import scala.util.{Failure, Random, Success}

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
}
