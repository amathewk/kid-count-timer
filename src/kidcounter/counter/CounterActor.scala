package kidcounter.counter

import akka.actor.{ActorLogging, Actor}
import scala.concurrent.duration._
//import system.dispatcher



/**
 * Created by amathew on 5/17/15.
 */

object CounterActor {

  case object Start
  case object Pause
  case object Resume
  case object Stop
  case class NextCount(val count:Int, val maxCount:Int)


}

class CounterActor(var count: Int, var time:Int) extends Actor with ActorLogging {

  import CounterActor._
  import context.{system, become}
//  import context.system.dispatcher


  var pausedCount:Option[NextCount]

  def stopped : Receive = {
    case Start =>
      system.scheduler.scheduleOnce(1 second, self, NextCount(1, count))
      become(counting)

    case _ =>

  }

  def counting : Receive = {
    case NextCount(count, maxCount) =>
      sayCount(count)
      system.scheduler.scheduleOnce(1 second, self, NextCount(count + 1, maxCount))
    case Pause => become(paused)
    case _ =>
  }


  def paused : Receive = {
    case NextCount(count, maxCount) =>
      pausedCount = Some(NextCount)

    case Resume =>
      sayCount(pausedCount.get.count)
      system.scheduler.scheduleOnce(1 second, self, NextCount(pausedCount.get.count + 1, pausedCount.get.maxCount))
      pausedCount = None
      become(counting)

    case Stop =>
      pausedCount = None

    case _ =>
  }


  def receive = stopped

  private def sayCount(count:Int) = { count ->
    println (s"count = $count")
  }

}

