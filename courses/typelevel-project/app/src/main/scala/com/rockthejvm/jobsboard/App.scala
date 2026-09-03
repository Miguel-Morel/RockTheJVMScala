package com.rockthejvm.jobsboard

import cats.effect.IO
import org.scalajs.dom.{console, document}
import tyrian.*
import tyrian.Html.*
import tyrian.cmds.Logger

import scala.concurrent.duration.DurationInt
import scala.scalajs.js.annotation.*

object App {
  sealed trait Msg
  case class Increment(amount: Int) extends Msg

  case class Model(count: Int)

}

@JSExportTopLevel("rockthejvmapp")
class App extends TyrianApp[    App.Msg, App.Model] {
  import App.*
  // there is a launch function |   ^message   ^model = "state"

  /*
    can send messages by
    - trigger a command
    - create a subscription
    - listening for an event
   */
  override def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model(0), Cmd.None)

  // potentially endless stream of messages
  override def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.every[IO](1.second).map(_ => Increment(1))

  // model can change by receiving messages
  // model => message => (new model, new command)
  // update triggered whenever we get a new message
  override def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Increment(amount) =>
//      console.log("changing count by" + amount)
      (model.copy(count = model.count + amount), Logger.consoleLog[IO]("changing count by " + amount))

  // view triggered whenever the model changes
  override def view(model: Model): Html[Msg] =
    div(
      button(onClick(Increment(1)))("increase"),
      button(onClick(Increment(-1)))("decrease"),
      div(s"Tyrian running: ${model.count}")
    )
}
