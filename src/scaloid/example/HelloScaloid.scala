package scaloid.example

import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.widget.{EditText, Button}
import org.scaloid.common._
import android.graphics.Color

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class HelloScaloid extends SActivity {

  var tts : TextToSpeech = null
//  val startButton, stopButton

  import scaloid.example.CountingState._
  @volatile var countState : CountingState = Initializing

  onCreate {
    contentView = new SVerticalLayout {
      style {
        case b: SButton => b.textColor(Color.BLUE)
        //          .onClick(toast("Bang!"))
        case t: STextView => t textSize 10.dip
        case e: SEditText => e.backgroundColor(Color.GRAY)
      }
      STextView("Count")
      SEditText("10").id(R.id.count_box)
      STextView("Time")
      SEditText("10").id(R.id.time_box)
      //      STextView("I am 15 dip tall") textSize 15.dip // overriding
      this += new SLinearLayout {
        //        STextView("Button: ")
        SButton(R.string.start).id(R.id.count_button)
        SButton(R.string.stop).id(R.id.stop_button)
      }.wrap
      //      SEditText("Yellow input field fills the space").fill
    } padding 20.dip

    tts = new TextToSpeech(getApplicationContext(), ttsInitListener)
//    tts.setSpeechRate(1.75f)

    find[Button](R.id.count_button).onClick(count())
    find[Button](R.id.stop_button).onClick(stopCounting())

  }

  def count() = {

    val count : String =  find[EditText](R.id.count_box).getText().toString()
    val time : String =  find[EditText](R.id.time_box).getText().toString()

    val convertResult: ConvertResult = convert(count, time)
    if(convertResult.valid == true) {
      Future {
        countdown(convertResult.count.get, convertResult.time.get)
      }
    } else {

      val msg = convertResult.msgs.foldLeft("") { (s1: String, s2: String) => s1 + "\n" + s2 }
      toast(msg)
    }

  }

  def stopCounting() = {

      if(countState == Counting) {
        countState = Interrupting
      }
  }

  /** validates the count and integer entries */
  def convert(count: String, time: String) : ConvertResult = {

    var valid = true
    val msgs = mutable.HashSet[String]()

    if( ! (count forall Character.isDigit) || ! (time forall Character.isDigit) ) {
      valid = false
      msgs += "Count and time must be numbers"

      return ConvertResult(valid, msgs, None, None)
    }


    val countVal : Int = count.toInt

    if(countVal > 1000) {
      valid = false
      msgs += "count cannot be greater than 1000"
    }

    val timeVal : Int = time.toInt

    if(timeVal > 1000) {
      valid = false
      msgs += "time cannot be greater than 1000"
    }

    if(valid) {
      val delay:Float = (timeVal*1000)/countVal
      if(delay < 50) {
        valid = false
        msgs += "The count is too high for the specified time. Either reduce the count or increase the time."
        return ConvertResult(valid, msgs, None, None)
      }
    }

    ConvertResult(valid, msgs, Some(countVal), Some(timeVal))
  }


  def ttsInitListener = new OnInitListener {

    override def onInit(i: Int): Unit = {
      countState = Ready
    }
  }



  def countdown(count: Int, time: Int) : Unit = {

    if(countState == Counting) return

    while(countState == Initializing) {}

    countState = Counting

    val startTime = System.nanoTime()

    val delayInMillis = (time * 1000 )/ count
    println (s"Delay $delayInMillis")

    for(i <- 1 to count){
      tts.speak(i.toString, TextToSpeech.QUEUE_ADD, null)

      if(countState == Interrupting) {
        countState = Ready
        return
      }

      Thread.sleep(delayInMillis)
    }

    println((System.nanoTime() - startTime))

    countState = Ready
//    toast(count)
  }

}


case class ConvertResult(valid: Boolean, msgs: mutable.Set[String], count: Option[Int], time: Option[Int])

object CountingState extends Enumeration {
  type CountingState = Value
  val Initializing, Ready, Counting, Interrupting = Value
}