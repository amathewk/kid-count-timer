package scaloid.example

import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.widget.{EditText, Button}
import org.scaloid.common._
import android.graphics.Color

import scala.collection.mutable

class HelloScaloid extends SActivity {

  var tts : TextToSpeech = null


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
      }.wrap
      //      SEditText("Yellow input field fills the space").fill
    } padding 20.dip

    tts = new TextToSpeech(getApplicationContext(), ttsInitListener)
    tts.setSpeechRate(0.75f)

    find[Button](R.id.count_button).onClick(count())



  }

  def count() = {

    val count : String =  find[EditText](R.id.count_box).getText().toString()
    val time : String =  find[EditText](R.id.time_box).getText().toString()

    val convertResult: ConvertResult = convert(count, time)
    if(convertResult.valid == true) {
      countdown(convertResult.count, convertResult.time)
    } else {

      val msg = convertResult.msgs.foldLeft("") { (s1: String, s2: String) => s1 + "\n" + s2 }
      toast(msg)
    }


    //    toast(count)
  }



  def convert(count: String, time: String) : ConvertResult = {

    var valid = true
    val msgs = mutable.HashSet[String]()

    if( ! (count forall Character.isDigit) && (time forall Character.isDigit) ) {
      valid = false
      msgs += "Count and time must be numbers"
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

    ConvertResult(valid, msgs, countVal, timeVal)
  }


  var ttsStarted = false

  def ttsInitListener = new OnInitListener {

    override def onInit(i: Int): Unit = {
      ttsStarted = true
    }
  }



  def countdown(count: Int, time: Int) = {

    while(!ttsStarted) {}

    val startTime = System.nanoTime()

    val delay = (time * 1000 )/ count
    println (s"Delay $delay")

    for(i <- 1 to count){
      tts.speak(i.toString, TextToSpeech.QUEUE_ADD, null)
      Thread.sleep(delay)
    }

    println((System.nanoTime() - startTime))

//    toast(count)
  }

  def speakCounts() = {

  }

}


case class ConvertResult(valid: Boolean, msgs: mutable.Set[String], count: Int, time: Int)