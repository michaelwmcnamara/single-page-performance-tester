package app.apiutils

import com.gu.contentapi.client.model.v1.CapiDateTime
import org.joda.time.DateTime

import scala.xml.Elem


/**
 * Created by mmcnamara on 10/02/16.
 */
class PerformanceResultsObject(url:String, testType: String, urlforTestResults: String, tTFB: Int, tFP:Int, render:Int, tDC: Int, bDC: Int, tFL: Int, bFL: Int, sI: Int, visualComp: Int, status: String, failedNeedsRetest: Boolean) {
// todo - add publication date; and setters and getters for same ; also add to csv string
  var timeOfTest: String = DateTime.now().toString
  val testUrl: String = url
  val typeOfTest: String = testType
  lazy val typeOfTestName = if(typeOfTest.contains("Desktop")){
    "Desktop"
  } else {
    "Mobile"
  }
  val friendlyResultUrl: String = urlforTestResults
  val timeToFirstByte: Int = tTFB
  val timeFirstPaintInMs: Int = tFP
  val timeFirstPaintInSec: Double = roundAt(3)(timeFirstPaintInMs.toDouble/1000)
  val startRenderInMs: Int = render
  val timeDocCompleteInMs: Int = tDC
  val timeDocCompleteInSec: Double = roundAt(3)(timeDocCompleteInMs.toDouble/1000)
  val bytesInDocComplete: Int = bDC
  val kBInDocComplete: Int = roundAt(0)(bytesInDocComplete.toDouble/1024).toInt
  val mBInDocComplete: Double = roundAt(3)(bytesInDocComplete.toDouble/1048576)
  val timeFullyLoadedInMs: Int = tFL
  val timeFullyLoadedInSec: Int = roundAt(0)(timeFullyLoadedInMs.toDouble/1000).toInt
  val bytesInFullyLoaded: Int = bFL
  val kBInFullyLoaded: Double = roundAt(3)(bytesInFullyLoaded.toDouble/1024)
  val mBInFullyLoaded: Double = roundAt(3)(bytesInFullyLoaded.toDouble/1048576)
  val estUSPrePaidCost: Double = roundAt(3)((bytesInFullyLoaded.toDouble/1048576)*0.10)
  val estUSPostPaidCost: Double = roundAt(3)((bytesInFullyLoaded.toDouble/1048576)*0.06)
  val speedIndex: Int = sI
  val visualComplete: Int = visualComp
  val aboveTheFoldCompleteInSec: Double = roundAt(3)(speedIndex.toDouble/1000)
  val resultStatus:String = status
  var pageWeightAlertDescription: String = "No alerts have been set for this page"
  var pageSpeedAlertDescription: String = "No alerts have been set for this page"
  val brokenTest: Boolean = failedNeedsRetest

  def setTimeOfTest(time: String): Unit = {
  if(DateTime.parse(time).toString.contains(time)) {
    println("setting timeOfTest to " + time)
    timeOfTest = time}
  }


  def toCSVString(): String = {
    timeOfTest + "," + testUrl.toString + "," + typeOfTest + "," + friendlyResultUrl + "," + timeToFirstByte.toString + "," + timeFirstPaintInMs.toString + "," + startRenderInMs + "," + timeDocCompleteInMs + "," + bytesInDocComplete + "," + timeFullyLoadedInMs + "," + bytesInFullyLoaded + "," + speedIndex + "," + visualComplete + "," + cleanString(resultStatus) + "," + brokenTest + "\n"
  }

/*  def toFullHTMLTableCells(): String = {
    "<td>" + "<a href=" + testUrl + ">" + headline.getOrElse(testUrl) + "</a>" + " </td>" + "<td>" + getPageType + "</td>" +  "<td>" + timeFirstPaintInMs.toString + "ms </td><td>" +  timeDocCompleteInSec.toString + "s </td><td>" + mBInDocComplete + "MB </td><td>" + timeFullyLoadedInSec.toString + "s </td><td>" + mBInFullyLoaded + "MB </td><td> $(US)" + estUSPrePaidCost + "</td><td> $(US)" + estUSPostPaidCost + "</td><td>" + speedIndex.toString + " </td><td> " + genTestResultString() + "</td>"
  }*/

  def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) / s }

  def stringtoCAPITime(time: String): Option[CapiDateTime] = {
    if(time.nonEmpty && !time.equals("0")) {
      val longTime = time.toLong
      val capiTime = new CapiDateTime {
        override def dateTime: Long = longTime
      }
      Option(capiTime)
    } else
    {
     None
    }
  }

   def capiTimeToString(time: Option[CapiDateTime]): String = {
     if(time.nonEmpty){
       val capiDT:CapiDateTime = time.get
       val timeLong = capiDT.dateTime
       val moreUseableTime = new DateTime(timeLong)
       moreUseableTime.toDate.toString
     }
     else{
       "Unknown"
     }
   }

  def newCapiDateTime(time: Long): CapiDateTime = {
    new CapiDateTime {
      override def dateTime: Long = time
    }
  }

  def cleanString(inputString: String): String = {
    var clean= inputString.replace(",", " ")
    clean = clean.replace("\r", "")
    clean = clean.replace("\n", "")
    clean = clean.replace("\\", "")
    clean = clean.replace("<", "")
    clean = clean.replace(">", "")
    clean = clean.replace("/", "")
    clean
  }
}
