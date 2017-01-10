package app.apiutils

import org.joda.time.DateTime


class PageAverageObject(dtfp: Int, dtdc: Int, dsdc: Int, dtfl: Int, dsfl: Int, dcflprepaid: Double, dcflpostpaid: Double, dsi: Int, dsc: Int, mtfp: Int, mtdc: Int, msdc: Int, mtfl: Int, msfl: Int, mcflprepaid: Double, mcflpostpaid: Double, msi: Int, msc: Int, resultString: String) {

  val desktopTimeFirstPaintInMs: Int = dtfp
  lazy val desktopTimeFirstPaintInSeconds: Double = roundAt(3)(desktopTimeFirstPaintInMs.toDouble/1000)
  val desktopTimeDocCompleteInMs: Int = dtdc
  lazy val desktopTimeDocCompleteInSeconds: Double = roundAt(3)(desktopTimeDocCompleteInMs.toDouble/1000)
  val desktopKBInDocComplete: Double = dsdc
  lazy val desktopMBInDocComplete: Double = roundAt(3)(desktopKBInDocComplete.toDouble/1024)
  val desktopTimeFullyLoadedInMs: Int = dtfl
  lazy val desktopTimeFullyLoadedInSeconds: Double = roundAt(3)(desktopTimeFullyLoadedInMs.toDouble/1000)
  val desktopKBInFullyLoaded: Double = dsfl
  lazy val desktopMBInFullyLoaded: Double = roundAt(3)(desktopKBInFullyLoaded.toDouble/1024)
  val desktopEstUSPrePaidCost: Double = dcflprepaid
  val desktopEstUSPostPaidCost: Double = dcflpostpaid
  val desktopSpeedIndex: Int = dsi
  lazy val desktopAboveTheFoldCompleteInSec: Double = roundAt(3)(desktopSpeedIndex.toDouble/1000)
  val desktopSuccessCount = dsc

  val mobileTimeFirstPaintInMs: Int = mtfp
  lazy val mobileTimeFirstPaintInSeconds: Double = roundAt(3)(mobileTimeFirstPaintInMs.toDouble/1000)
  val mobileTimeDocCompleteInMs: Int = mtdc
  lazy val mobileTimeDocCompleteInSeconds: Double = roundAt(3)(mobileTimeDocCompleteInMs.toDouble/1000)
  val mobileKBInDocComplete: Double = msdc
  lazy val mobileMBInDocComplete: Double = roundAt(3)(mobileKBInDocComplete.toDouble/1024)
  val mobileTimeFullyLoadedInMs: Int = mtfl
  lazy val mobileTimeFullyLoadedInSeconds: Double = roundAt(3)(mobileTimeFullyLoadedInMs.toDouble/1000)
  val mobileKBInFullyLoaded: Double = msfl
  lazy val mobileMBInFullyLoaded: Double = roundAt(3)(mobileKBInFullyLoaded.toDouble/1024)
  val mobileEstUSPrePaidCost: Double = mcflprepaid
  val mobileEstUSPostPaidCost: Double = mcflpostpaid
  val mobileSpeedIndex: Int = msi
  lazy val mobileAboveTheFoldCompleteInSec: Double = roundAt(3)(desktopSpeedIndex.toDouble/1000)
  val mobileSuccessCount = msc

  val formattedHTMLResultString: String = resultString
  lazy val (desktopPart,mobilePart): (String,String) = formattedHTMLResultString.splitAt(formattedHTMLResultString.indexOf("<tr>",4))
  lazy val desktopHTMLResultString: String = desktopPart
  lazy val mobileHTMLResultString: String = mobilePart

  lazy val desktopTimeFirstPaintInMs80thPercentile: Double = (desktopTimeFirstPaintInMs * 80).toDouble/ 100
  lazy val desktopTimeDocCompleteInMs80thPercentile: Double = (desktopTimeDocCompleteInMs * 80).toDouble/ 100
  lazy val desktopKBInDocComplete80thPercentile: Double = (desktopKBInDocComplete * 80)/ 100
  lazy val desktopTimeFullyLoadedInMs80thPercentile: Double = (desktopTimeFullyLoadedInMs * 80).toDouble/ 100
  lazy val desktopKBInFullyLoaded80thPercentile:Double = (desktopKBInFullyLoaded * 80)/ 100
  lazy val desktopEstUSPrePaidCost80thPercentile: Double = (desktopEstUSPrePaidCost * 80)/ 100
  lazy val desktopEstUSPostPaidCost80thPercentile: Double = (desktopEstUSPostPaidCost * 80)/ 100
  lazy val desktopSpeedIndex80thPercentile: Double = (desktopSpeedIndex * 80).toDouble/ 100
  lazy val mobileTimeFirstPaintInMs80thPercentile: Double = (mobileTimeFirstPaintInMs * 80).toDouble/ 100
  lazy val mobileTimeDocCompleteInMs80thPercentile: Double = (mobileTimeDocCompleteInMs * 80).toDouble/ 100
  lazy val mobileKBInDocComplete80thPercentile: Double = (mobileKBInDocComplete * 80)/ 100
  lazy val mobileTimeFullyLoadedInMs80thPercentile: Double = (mobileTimeFullyLoadedInMs * 80).toDouble/ 100
  lazy val mobileKBInFullyLoaded80thPercentile: Double = (mobileKBInFullyLoaded * 80)/ 100
  lazy val mobileEstUSPrePaidCost80thPercentile: Double = (mobileEstUSPrePaidCost * 80)/ 100
  lazy val mobileEstUSPostPaidCost80thPercentile: Double = (mobileEstUSPostPaidCost * 80)/ 100
  lazy val mobileSpeedIndex80thPercentile: Double = (mobileSpeedIndex * 80).toDouble/100


  def this() {
    this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "")
  }

  def toHTMLString: String = formattedHTMLResultString

  def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) .toDouble/ s}
}


class ArticleDefaultAverages(averageColor: String) extends PageAverageObject {
  override val desktopTimeFirstPaintInMs: Int = (2 * 1000).toInt
  override val desktopTimeDocCompleteInMs: Int = 15 * 1000
  override val desktopKBInDocComplete: Double = 4 * 1024
  override val desktopTimeFullyLoadedInMs: Int = 20 * 1000
  override val desktopKBInFullyLoaded: Double = 4096
  override val desktopEstUSPrePaidCost: Double = 0.60
  override val desktopEstUSPostPaidCost: Double = 0.50
  override val desktopSpeedIndex: Int = 1482
  override val desktopSuccessCount = 1

  override val mobileTimeFirstPaintInMs: Int = 3 * 1000
  override val mobileTimeDocCompleteInMs: Int = 15 * 1000
  override val mobileKBInDocComplete: Double = 3072
  override val mobileTimeFullyLoadedInMs: Int = 20 * 1000
  override val mobileKBInFullyLoaded: Double = 3584
  override val mobileEstUSPrePaidCost: Double = 0.40
  override val mobileEstUSPostPaidCost: Double = 0.30
  override val mobileSpeedIndex: Int = 5000
  override val mobileSuccessCount = 1

  override val formattedHTMLResultString: String = "<tr style=\"background-color:" + averageColor + ";\">" +
    "<td>" + DateTime.now + "</td>" +
    "<td>Desktop</td>" +
    "<td> Alerting thresholds for desktop browser tests</td>" +
    "<td>article</td>" +
    "<td>" + desktopTimeFirstPaintInSeconds + "s</td>" +
    "<td>" + desktopAboveTheFoldCompleteInSec + "s</td>" +
    "<td>" + desktopMBInDocComplete + "MB</td>" +
    "<td>$(US)" + desktopEstUSPrePaidCost + "</td>" +
    "<td>$(US)" + desktopEstUSPostPaidCost + "</td>" +
    "<td>Predefined standards</td>"+
    "<td> </td>"+ "</tr>" +
    "<tr style=\"background-color:" + averageColor + ";\">" +
    "<td>" + DateTime.now + "</td>" +
    "<td>Mobile</td>" +
    "<td> Alerting thresholds for mobile 3G browser tests </td>" +
    "<td>" + mobileTimeFirstPaintInSeconds + "s</td>" +
    "<td>" + mobileAboveTheFoldCompleteInSec + "</td>" +
    "<td>" + mobileMBInDocComplete + "MB</td>" +
    "<td>$(US)" + mobileEstUSPrePaidCost + "</td>" +
    "<td>S(US)" + mobileEstUSPostPaidCost + "</td>" +
    "<td>Predefined standards</td>"+
    "<td> </td></tr>"
}

class LiveBlogDefaultAverages(averageColor: String) extends PageAverageObject {
  override val desktopTimeFirstPaintInMs: Int = (2 * 1000).toInt
  override val desktopTimeDocCompleteInMs: Int = 15 * 1000
  override val desktopKBInDocComplete: Double = 3200
  override val desktopTimeFullyLoadedInMs: Int = 20 * 1000
  override val desktopKBInFullyLoaded: Double = 5120
  override val desktopEstUSPrePaidCost: Double = 0.60
  override val desktopEstUSPostPaidCost: Double = 0.50
  override val desktopSpeedIndex: Int = 2074
  override val desktopSuccessCount = 1

  override val mobileTimeFirstPaintInMs: Int = 3 * 1000
  override val mobileTimeDocCompleteInMs: Int = 15 * 1000
  override val mobileKBInDocComplete: Double = 3000
  override val mobileTimeFullyLoadedInMs: Int = 20 * 1000
  override val mobileKBInFullyLoaded: Double = 4096
  override val mobileEstUSPrePaidCost: Double = 0.40
  override val mobileEstUSPostPaidCost: Double = 0.30
  override val mobileSpeedIndex: Int = 5000
  override val mobileSuccessCount = 1

  override val formattedHTMLResultString: String = "<tr style=\"background-color:" + averageColor + ";\">" +
    "<td>" + DateTime.now + "</td>" +
    "<td>Desktop</td>" +
    "<td> Alerting thresholds for desktop browser tests</td>" +
    "<td>liveblog</td>" +
    "<td>" + desktopTimeFirstPaintInSeconds + "s</td>" +
    "<td>" + desktopAboveTheFoldCompleteInSec + "s</td>" +
    "<td>" + desktopMBInDocComplete + "MB</td>" +
    "<td>$(US)" + desktopEstUSPrePaidCost + "</td>" +
    "<td>$(US)" + desktopEstUSPostPaidCost + "</td>" +
    "<td>Predefined standards</td>"+
    "<td> </td>"+ "</tr>" +
    "<tr style=\"background-color:" + averageColor + ";\">" +
    "<td>" + DateTime.now + "</td>" +
    "<td>Mobile</td>" +
    "<td> Alerting thresholds for mobile 3G browser tests </td>" +
    "<td>" + mobileTimeFirstPaintInSeconds + "s</td>" +
    "<td>" + mobileAboveTheFoldCompleteInSec + "</td>" +
    "<td>" + mobileMBInDocComplete + "MB</td>" +
    "<td>$(US)" + mobileEstUSPrePaidCost + "</td>" +
    "<td>S(US)" + mobileEstUSPostPaidCost + "</td>" +
    "<td>Predefined standards</td>"+
    "<td> </td></tr>"
}

class InteractiveDefaultAverages(averageColor: String) extends PageAverageObject {
  override val desktopTimeFirstPaintInMs: Int = (2.5 * 1000).toInt
  override val desktopTimeDocCompleteInMs: Int = 15 * 1000
  override val desktopKBInDocComplete: Double = 2900
  override val desktopTimeFullyLoadedInMs: Int = 20 * 1000
  override val desktopKBInFullyLoaded: Double = 5120//2906
  override val desktopEstUSPrePaidCost: Double = 0.60
  override val desktopEstUSPostPaidCost: Double = 0.50
  override val desktopSpeedIndex: Int = 4000
  override val desktopSuccessCount = 1

  override val mobileTimeFirstPaintInMs: Int = 3 * 1000
  override val mobileTimeDocCompleteInMs: Int = 15 * 1000
  override val mobileKBInDocComplete: Double = 2318
  override val mobileTimeFullyLoadedInMs: Int = 20 * 1000
  override val mobileKBInFullyLoaded: Double = 3072
  override val mobileEstUSPrePaidCost: Double = 0.40
  override val mobileEstUSPostPaidCost: Double = 0.30
  override val mobileSpeedIndex: Int = 7000
  override val mobileSuccessCount = 1

  override val formattedHTMLResultString: String = "<tr style=\"background-color:" + averageColor + ";\">" +
    "<td>" + DateTime.now + "</td>" +
    "<td>Desktop</td>" +
    "<td> Alerting thresholds for desktop browser tests</td>" +
    "<td>interactive</td>" +
    "<td>" + desktopTimeFirstPaintInSeconds + "s</td>" +
    "<td>" + desktopAboveTheFoldCompleteInSec + "s</td>" +
    "<td>" + desktopMBInDocComplete + "MB</td>" +
    "<td>$(US)" + desktopEstUSPrePaidCost + "</td>" +
    "<td>$(US)" + desktopEstUSPostPaidCost + "</td>" +
    "<td>Predefined standards</td>"+
    "<td> </td>"+ "</tr>" +
    "<tr style=\"background-color:" + averageColor + ";\">" +
    "<td>" + DateTime.now + "</td>" +
    "<td>Mobile</td>" +
    "<td> Alerting thresholds for mobile 3G browser tests </td>" +
    "<td>interactive</td>" +
    "<td>" + mobileTimeFirstPaintInSeconds + "s</td>" +
    "<td>" + mobileAboveTheFoldCompleteInSec + "</td>" +
    "<td>" + mobileMBInDocComplete + "MB</td>" +
    "<td>$(US)" + mobileEstUSPrePaidCost + "</td>" +
    "<td>S(US)" + mobileEstUSPostPaidCost + "</td>" +
    "<td>Predefined standards</td>"+
    "<td> </td></tr>"
}



class FrontsDefaultAverages(averageColor: String) extends PageAverageObject() {
  override val desktopTimeFirstPaintInMs: Int = (2 * 1000).toInt
  override val desktopTimeDocCompleteInMs: Int = 15 * 1000
  override val desktopKBInDocComplete: Double = 3 * 1024
  override val desktopTimeFullyLoadedInMs: Int = 20 * 1000
  override val desktopKBInFullyLoaded: Double = 4 * 1024
  override val desktopEstUSPrePaidCost: Double = 0.60
  override val desktopEstUSPostPaidCost: Double = 0.50
  override val desktopSpeedIndex: Int = 1717
  override val desktopSuccessCount = 1

  override val mobileTimeFirstPaintInMs: Int = 2 * 1000
  override val mobileTimeDocCompleteInMs: Int = 15 * 1000
  override val mobileKBInDocComplete: Double = 3 * 1024
  override val mobileTimeFullyLoadedInMs: Int = 20 * 1000
  override val mobileKBInFullyLoaded: Double = 4 * 1024
  override val mobileEstUSPrePaidCost: Double = 0.40
  override val mobileEstUSPostPaidCost: Double = 0.30
  override val mobileSpeedIndex: Int = 4694
  override val mobileSuccessCount = 1

  override val formattedHTMLResultString: String = "<tr style=\"background-color:" + averageColor + ";\">" +
    "<td>" + DateTime.now + "</td>" +
    "<td>Desktop</td>" +
    "<td> Alerting thresholds for desktop browser tests</td>" +
  "<td> Front </td>"
    "<td>" + desktopTimeFirstPaintInSeconds + "s</td>" +
    "<td>" + desktopAboveTheFoldCompleteInSec + "s</td>" +
    "<td>" + desktopMBInDocComplete + "MB</td>" +
    "<td>$(US)" + desktopEstUSPrePaidCost + "</td>" +
    "<td>$(US)" + desktopEstUSPostPaidCost + "</td>" +
    "<td>Predefined standards</td>" +
    "<td> </td></tr>" +
    "<tr style=\"background-color:" + averageColor + ";\">" +
    "<td>" + DateTime.now + "</td>" +
    "<td>Mobile</td>" +
    "<td> Alerting thresholds for mobile 3G browser tests </td>" +
    "<td>" + mobileTimeFirstPaintInSeconds + "s</td>" +
    "<td>" + mobileAboveTheFoldCompleteInSec + "</td>" +
    "<td>" + mobileMBInDocComplete + "MB</td>" +
    "<td>$(US)" + mobileEstUSPrePaidCost + "</td>" +
    "<td>S(US)" + mobileEstUSPostPaidCost + "</td>" +
    "<td>Predefined standards</td>" +
    "<td> </td></tr>"
}

class GeneratedPageAverages(resultsList: List[Array[PerformanceResultsObject]], averageColor: String, pageType: String) extends PageAverageObject{

  var accumulatorDesktopTimeFirstPaint: Int = 0
  var accumulatorDesktopTimeDocComplete: Int = 0
  var accumulatorDesktopKBInDocComplete: Double = 0
  var accumulatorDesktopTimeFullyLoaded: Int = 0
  var accumulatorDesktopKBInFullyLoaded: Double = 0
  var accumulatorDesktopEstUSPrePaidCost: Double = 0
  var accumulatorDesktopEstUSPostPaidCost: Double = 0
  var accumulatorDesktopSpeedIndex: Int = 0
  var accumulatorDesktopSuccessCount = 0

  var accumulatorMobileTimeFirstPaint: Int = 0
  var accumulatorMobileTimeDocComplete: Int = 0
  var accumulatorMobileKBInDoccomplete: Double = 0
  var accumulatorMobileTimeFullyLoaded: Int = 0
  var accumulatorMobileKBInFullyLoaded: Double = 0
  var accumulatorMobileEstUSPrePaidCost: Double = 0
  var accumulatorMobileEstUSPostPaidCost: Double = 0
  var accumulatorMobileSpeedIndex: Int = 0
  var accumulatorMobileSuccessCount = 0

  var accumulatorString: String = ""

  val typeOfPage:String = pageType
  val multipleLiveBlogs:String = "liveblogs that were migrated due to size"
  val singleLiveBlog: String = "Example of a liveblog migrated due to size"
  val noLiveBlogs: String = "All tests of migrated liveblogs failed"

  val multipleInteractives:String = "interactives with known size or performance issues"
  val singleInteractive: String = "Example of an interactive with known size or performance issues"
  val noInteractives: String = "All tests of interactives with size or performance issues have failed"

  //process result objects
  resultsList.foreach(result => {
  if (result(0).resultStatus == "Test Success") {
    accumulatorDesktopTimeFirstPaint += result(0).timeFirstPaintInMs
    accumulatorDesktopTimeDocComplete += result(0).timeDocCompleteInMs
    accumulatorDesktopKBInDocComplete += result(0).kBInDocComplete
    accumulatorDesktopTimeFullyLoaded += result(0).timeFullyLoadedInMs
    accumulatorDesktopKBInFullyLoaded += result(0).kBInFullyLoaded
    accumulatorDesktopEstUSPrePaidCost += result(0).estUSPrePaidCost
    accumulatorDesktopEstUSPostPaidCost += result(0).estUSPostPaidCost
    accumulatorDesktopSpeedIndex += result(0).speedIndex
    accumulatorDesktopSuccessCount += 1
  }
  if (result(1).resultStatus == "Test Success") {
    accumulatorMobileTimeFirstPaint += result(1).timeFirstPaintInMs
    accumulatorMobileTimeDocComplete += result(1).timeDocCompleteInMs
    accumulatorMobileKBInDoccomplete += result(1).kBInDocComplete
    accumulatorMobileTimeFullyLoaded += result(1).timeFullyLoadedInMs
    accumulatorMobileKBInFullyLoaded += result(1).kBInFullyLoaded
    accumulatorMobileEstUSPrePaidCost += result(1).estUSPrePaidCost
    accumulatorMobileEstUSPostPaidCost += result(1).estUSPostPaidCost
    accumulatorMobileSpeedIndex += result(1).speedIndex
    accumulatorMobileSuccessCount += 1
  }
  })

  override val desktopTimeFirstPaintInMs: Int = if (accumulatorDesktopSuccessCount > 0) {roundAt(0)(accumulatorDesktopTimeFirstPaint.toDouble/accumulatorDesktopSuccessCount).toInt} else {2 * 1000}
  override val desktopTimeDocCompleteInMs: Int = if (accumulatorDesktopSuccessCount > 0) {roundAt(0)(accumulatorDesktopTimeDocComplete.toDouble/accumulatorDesktopSuccessCount).toInt} else {15 * 1000}
  override val desktopKBInDocComplete: Double = if (accumulatorDesktopSuccessCount > 0) {roundAt(3)(accumulatorDesktopKBInDocComplete/accumulatorDesktopSuccessCount)} else {10 * 1024}
  override val desktopTimeFullyLoadedInMs: Int = if (accumulatorDesktopSuccessCount > 0) {roundAt(0)(accumulatorDesktopTimeFullyLoaded.toDouble/accumulatorDesktopSuccessCount).toInt} else {20 * 1000}
  override val desktopKBInFullyLoaded: Double = if (accumulatorDesktopSuccessCount > 0) {roundAt(3)(accumulatorDesktopKBInFullyLoaded/accumulatorDesktopSuccessCount)} else {15 * 1024}
  override val desktopEstUSPrePaidCost: Double = if (accumulatorDesktopSuccessCount > 0) {roundAt(2)(accumulatorDesktopEstUSPrePaidCost/accumulatorDesktopSuccessCount)} else {60.00}
  override val desktopEstUSPostPaidCost: Double = if (accumulatorDesktopSuccessCount > 0) {roundAt(2)(accumulatorDesktopEstUSPostPaidCost/accumulatorDesktopSuccessCount)} else {50.00}
  override val desktopSpeedIndex: Int = if (accumulatorDesktopSuccessCount > 0) {roundAt(0)(accumulatorDesktopSpeedIndex.toDouble/accumulatorDesktopSuccessCount).toInt} else {5000}
  override val desktopSuccessCount: Int = accumulatorDesktopSuccessCount

  override val mobileTimeFirstPaintInMs: Int = if (accumulatorMobileSuccessCount > 0) {roundAt(0)(accumulatorMobileTimeFirstPaint.toDouble/accumulatorMobileSuccessCount).toInt} else {2 * 1000}
  override val mobileTimeDocCompleteInMs: Int = if (accumulatorMobileSuccessCount > 0) {roundAt(0)(accumulatorMobileTimeDocComplete.toDouble/accumulatorMobileSuccessCount).toInt} else {15 * 1000}
  override val mobileKBInDocComplete: Double = if (accumulatorMobileSuccessCount > 0) {roundAt(3)(accumulatorMobileKBInDoccomplete/accumulatorMobileSuccessCount)} else {6 * 1024}
  override val mobileTimeFullyLoadedInMs: Int = if (accumulatorMobileSuccessCount > 0) {roundAt(0)(accumulatorMobileTimeFullyLoaded.toDouble/accumulatorMobileSuccessCount).toInt} else {20 * 1000}
  override val mobileKBInFullyLoaded: Double = if (accumulatorMobileSuccessCount > 0) {roundAt(3)(accumulatorMobileKBInFullyLoaded/accumulatorMobileSuccessCount)} else {6 * 1024}
  override val mobileEstUSPrePaidCost: Double = if (accumulatorMobileSuccessCount > 0) {roundAt(2)(accumulatorMobileEstUSPrePaidCost/accumulatorMobileSuccessCount)} else {0.40}
  override val mobileEstUSPostPaidCost: Double = if (accumulatorMobileSuccessCount > 0) {roundAt(2)(accumulatorMobileEstUSPostPaidCost/accumulatorMobileSuccessCount)} else {0.30}
  override val mobileSpeedIndex: Int = if (accumulatorMobileSuccessCount > 0) {roundAt(0)(accumulatorMobileSpeedIndex.toDouble/accumulatorMobileSuccessCount).toInt} else {5000}
  override val mobileSuccessCount: Int = accumulatorMobileSuccessCount

  accumulatorString = accumulatorString.concat("<tr style=\"background-color:" + averageColor + ";\"><td>" + DateTime.now + "</td><td>Desktop</td>")

  //add desktop averaages to return string
  if(accumulatorDesktopSuccessCount > 1){
    accumulatorString = accumulatorString.concat("<td>" + "Average of " + accumulatorDesktopSuccessCount + "pages  with recognised size issues</td>" +
      "<td>" + typeOfPage + "</td>" +
      "<td>" + desktopTimeFirstPaintInSeconds + "s</td>" +
      "<td>" + desktopAboveTheFoldCompleteInSec + "s</td>" +
      "<td>" + desktopMBInDocComplete + "MB</td>" +
      "<td>$(US)" + desktopEstUSPrePaidCost + "</td>" +
      "<td>$(US)" + desktopEstUSPostPaidCost + "</td>" +
      "<td>" + desktopSuccessCount + " urls Tested Successfully</td>" +
      "<td> </td></tr>"
    )}
  else{
    if (accumulatorDesktopSuccessCount == 1) {
      accumulatorString = accumulatorString.concat("<td> Results from 1 page with recognised size issues</td>" +
        "<td>" + typeOfPage + "</td>" +
        "<td>" + desktopTimeFirstPaintInSeconds + "s</td>" +
        "<td>" + desktopAboveTheFoldCompleteInSec + "s</td>" +
        "<td>" + desktopMBInDocComplete + "MB</td>" +
        "<td>$(US)" + desktopEstUSPrePaidCost + "</td>" +
        "<td>$(US)" + desktopEstUSPostPaidCost + "</td>" +
        "<td>" + desktopSuccessCount + " urls Tested Successfully</td>" +
        "<td> </td></tr>"
      )
    }
    else {
      accumulatorString = accumulatorString.concat("<td> Standard values to be used for judging page size</td>" +
        "<td>" + typeOfPage + "</td>" +
        "<td>" + desktopTimeFirstPaintInSeconds + "s</td>" +
        "<td>" + desktopAboveTheFoldCompleteInSec + "s</td>" +
        "<td>" + desktopMBInDocComplete + "MB</td>" +
        "<td>$(US)" + desktopEstUSPrePaidCost + "</td>" +
        "<td>$(US)" + desktopEstUSPostPaidCost + "</td>" +
        "<td>" + desktopSuccessCount + " urls Tested Successfully</td>" +
        "<td> </td></tr>"

      )
    }
  }
  
  //add mobile averages to return string
  accumulatorString = accumulatorString.concat("<tr style=\"background-color:" + averageColor + ";\"><td>" + DateTime.now + "</td><td>Android/3G</td>")
  if(accumulatorMobileSuccessCount > 1){
    accumulatorString = accumulatorString.concat("<td>" + "Average of " + accumulatorDesktopSuccessCount + "pages  with recognised size issues</td>" +
      "<td>" + typeOfPage + "</td>" +
      "<td>" + mobileTimeFirstPaintInSeconds + "s</td>" +
      "<td>" + mobileAboveTheFoldCompleteInSec + "</td>" +
      "<td>" + mobileMBInDocComplete + "MB</td>" +
      "<td>$(US)" + mobileEstUSPrePaidCost + "</td>" +
      "<td>S(US)" + mobileEstUSPostPaidCost + "</td>" +
      "<td>" + mobileSuccessCount + " urls Tested Successfully</td>" +
      "<td> </td></tr>"
    )}
  else{
    if (accumulatorMobileSuccessCount == 1) {
      accumulatorString = accumulatorString.concat("<td> Results from 1 page with recognised size issues</td>" +
        "<td>" + typeOfPage + "</td>" +
        "<td>" + mobileTimeFirstPaintInSeconds + "s</td>" +
        "<td>" + mobileAboveTheFoldCompleteInSec + "</td>" +
        "<td>" + mobileMBInDocComplete + "MB</td>" +
        "<td>$(US)" + mobileEstUSPrePaidCost + "</td>" +
        "<td>S(US)" + mobileEstUSPostPaidCost + "</td>" +
        "<td>" + mobileSuccessCount + " urls Tested Successfully</td>" +
        "<td> </td></tr>"
      )
    }
    else {
      accumulatorString = accumulatorString.concat("<td> Standard values to be used for judging page size</td>" +
        "<td>" + typeOfPage + "</td>" +
        "<td>" + mobileTimeFirstPaintInSeconds + "s</td>" +
        "<td>" + mobileAboveTheFoldCompleteInSec + "</td>" +
        "<td>" + mobileMBInDocComplete + "MB</td>" +
        "<td>$(US)" + mobileEstUSPrePaidCost + "</td>" +
        "<td>S(US)" + mobileEstUSPostPaidCost + "</td>" +
        "<td>" + mobileSuccessCount + " urls Tested Successfully</td>" +
        "<td> </td></tr>"
      )
    }
  }

  override val formattedHTMLResultString: String = accumulatorString
}


class GeneratedInteractiveAverages(resultsList: List[Array[PerformanceResultsObject]], averageColor: String) extends PageAverageObject {
  var accumulatorDesktopTimeFirstPaint: Int = 0
  var accumulatorDesktopTimeDocComplete: Int = 0
  var accumulatorDesktopKBInDocComplete: Double = 0
  var accumulatorDesktopTimeFullyLoaded: Int = 0
  var accumulatorDesktopKBInFullyLoaded: Double = 0
  var accumulatorDesktopEstUSPrePaidCost: Double = 0
  var accumulatorDesktopEstUSPostPaidCost: Double = 0
  var accumulatorDesktopSpeedIndex: Int = 0
  var accumulatorDesktopSuccessCount = 0

  var accumulatorMobileTimeFirstPaint: Int = 0
  var accumulatorMobileTimeDocComplete: Int = 0
  var accumulatorMobileKBInDoccomplete: Double = 0
  var accumulatorMobileTimeFullyLoaded: Int = 0
  var accumulatorMobileKBInFullyLoaded: Double = 0
  var accumulatorMobileEstUSPrePaidCost: Double = 0
  var accumulatorMobileEstUSPostPaidCost: Double = 0
  var accumulatorMobileSpeedIndex: Int = 0
  var accumulatorMobileSuccessCount = 0

  var accumulatorString: String = ""

  val multipleLiveBlogs:String = "liveblogs that were migrated due to size"
  val singleLiveBlog: String = "Example of a liveblog migrated due to size"
  val noLiveBlogs: String = "All tests of migrated liveblogs failed"

  val multipleInteractives:String = "interactives with known size or performance issues"
  val singleInteractive: String = "Example of an interactive with known size or performance issues"
  val noInteractives: String = "All tests of interactives with size or performance issues have failed"

  //process result objects
  resultsList.foreach(result => {
    if (result(0).resultStatus == "Test Success") {
      accumulatorDesktopTimeFirstPaint += result(0).timeFirstPaintInMs
      accumulatorDesktopTimeDocComplete += result(0).timeDocCompleteInMs
      accumulatorDesktopKBInDocComplete += result(0).kBInDocComplete
      accumulatorDesktopTimeFullyLoaded += result(0).timeFullyLoadedInMs
      accumulatorDesktopKBInFullyLoaded += result(0).kBInFullyLoaded
      accumulatorDesktopEstUSPrePaidCost += result(0).estUSPrePaidCost
      accumulatorDesktopEstUSPostPaidCost += result(0).estUSPostPaidCost
      accumulatorDesktopSpeedIndex += result(0).speedIndex
      accumulatorDesktopSuccessCount += 1
    }
    if (result(1).resultStatus == "Test Success") {
      accumulatorMobileTimeFirstPaint += result(1).timeFirstPaintInMs
      accumulatorMobileTimeDocComplete += result(1).timeDocCompleteInMs
      accumulatorMobileKBInDoccomplete += result(1).kBInDocComplete
      accumulatorMobileTimeFullyLoaded += result(1).timeFullyLoadedInMs
      accumulatorMobileKBInFullyLoaded += result(1).kBInFullyLoaded
      accumulatorMobileEstUSPrePaidCost += result(1).estUSPrePaidCost
      accumulatorMobileEstUSPostPaidCost += result(1).estUSPostPaidCost
      accumulatorMobileSpeedIndex += result(1).speedIndex
      accumulatorMobileSuccessCount += 1
    }
  })

  override val desktopTimeFirstPaintInMs: Int = if (accumulatorDesktopSuccessCount > 0) {roundAt(0)(accumulatorDesktopTimeFirstPaint.toDouble/accumulatorDesktopSuccessCount).toInt} else {2 * 1000}
  override val desktopTimeDocCompleteInMs: Int = if (accumulatorDesktopSuccessCount > 0) {roundAt(0)(accumulatorDesktopTimeDocComplete.toDouble/accumulatorDesktopSuccessCount).toInt} else {15 * 1000}
  override val desktopKBInDocComplete: Double = if (accumulatorDesktopSuccessCount > 0) {roundAt(3)(accumulatorDesktopKBInDocComplete/accumulatorDesktopSuccessCount)} else {10 * 1024}
  override val desktopTimeFullyLoadedInMs: Int = if (accumulatorDesktopSuccessCount > 0) {roundAt(0)(accumulatorDesktopTimeFullyLoaded.toDouble/accumulatorDesktopSuccessCount).toInt} else {20 * 1000}
  override val desktopKBInFullyLoaded: Double = if (accumulatorDesktopSuccessCount > 0) {roundAt(3)(accumulatorDesktopKBInFullyLoaded/accumulatorDesktopSuccessCount)} else {15 * 1024}
  override val desktopEstUSPrePaidCost: Double = if (accumulatorDesktopSuccessCount > 0) {roundAt(2)(accumulatorDesktopEstUSPrePaidCost/accumulatorDesktopSuccessCount)} else {60.00}
  override val desktopEstUSPostPaidCost: Double = if (accumulatorDesktopSuccessCount > 0) {roundAt(2)(accumulatorDesktopEstUSPostPaidCost/accumulatorDesktopSuccessCount)} else {50.00}
  override val desktopSpeedIndex: Int = if (accumulatorDesktopSuccessCount > 0) {roundAt(0)(accumulatorDesktopSpeedIndex.toDouble/accumulatorDesktopSuccessCount).toInt} else {5000}
  override val desktopSuccessCount: Int = accumulatorDesktopSuccessCount

  override val mobileTimeFirstPaintInMs: Int = if (accumulatorMobileSuccessCount > 0) {roundAt(0)(accumulatorMobileTimeFirstPaint.toDouble/accumulatorMobileSuccessCount).toInt} else {2 * 1000}
  override val mobileTimeDocCompleteInMs: Int = if (accumulatorMobileSuccessCount > 0) {roundAt(0)(accumulatorMobileTimeDocComplete.toDouble/accumulatorMobileSuccessCount).toInt} else {15 * 1000}
  override val mobileKBInDocComplete: Double = if (accumulatorMobileSuccessCount > 0) {roundAt(3)(accumulatorMobileKBInDoccomplete/accumulatorMobileSuccessCount)} else {6 * 1024}
  override val mobileTimeFullyLoadedInMs: Int = if (accumulatorMobileSuccessCount > 0) {roundAt(0)(accumulatorMobileTimeFullyLoaded.toDouble/accumulatorMobileSuccessCount).toInt} else {20 * 1000}
  override val mobileKBInFullyLoaded: Double = if (accumulatorMobileSuccessCount > 0) {roundAt(3)(accumulatorMobileKBInFullyLoaded/accumulatorMobileSuccessCount)} else {6 * 1024}
  override val mobileEstUSPrePaidCost: Double = if (accumulatorMobileSuccessCount > 0) {roundAt(2)(accumulatorMobileEstUSPrePaidCost/accumulatorMobileSuccessCount)} else {0.40}
  override val mobileEstUSPostPaidCost: Double = if (accumulatorMobileSuccessCount > 0) {roundAt(2)(accumulatorMobileEstUSPostPaidCost/accumulatorMobileSuccessCount)} else {0.30}
  override val mobileSpeedIndex: Int = if (accumulatorMobileSuccessCount > 0) {roundAt(0)(accumulatorMobileSpeedIndex.toDouble/accumulatorMobileSuccessCount).toInt} else {5000}
  override val mobileSuccessCount: Int = accumulatorMobileSuccessCount

  accumulatorString = accumulatorString.concat("<tr style=\"background-color:" + averageColor + ";\"><td>" + DateTime.now + "</td><td>Desktop</td>")

  //add desktop averaages to return string
  if(accumulatorDesktopSuccessCount > 1){
    accumulatorString = accumulatorString.concat("<td>" + "Average of " + accumulatorDesktopSuccessCount + "pages  with recognised size issues</td>" +
      "<td>" + "Interactive" + "</td>" +
      "<td>" + desktopTimeFirstPaintInSeconds + "s</td>" +
      "<td>" + desktopAboveTheFoldCompleteInSec + "s</td>" +
      "<td>" + desktopMBInDocComplete + "MB</td>" +
      "<td>" + desktopSuccessCount + " urls Tested Successfully</td>" +
      "<td> </td></tr>"
    )}
  else{
    if (accumulatorDesktopSuccessCount == 1) {
      accumulatorString = accumulatorString.concat("<td> Results from 1 page with recognised size issues</td>" +
        "<td>" + "Interactive" + "</td>" +
        "<td>" + desktopTimeFirstPaintInSeconds + "s</td>" +
        "<td>" + desktopAboveTheFoldCompleteInSec + "s</td>" +
        "<td>" + desktopMBInDocComplete + "MB</td>" +
        "<td>" + desktopSuccessCount + " urls Tested Successfully</td>" +
        "<td> </td></tr>"
      )
    }
    else {
      accumulatorString = accumulatorString.concat("<td> Standard values to be used for judging page size</td>" +
        "<td>" + "Interactive" + "</td>" +
        "<td>" + desktopTimeFirstPaintInSeconds + "s</td>" +
        "<td>" + desktopAboveTheFoldCompleteInSec + "s</td>" +
        "<td>" + desktopMBInDocComplete + "MB</td>" +
        "<td>" + desktopSuccessCount + " urls Tested Successfully</td>" +
        "<td> </td></tr>"
      )
    }
  }

  //add mobile averages to return string
  accumulatorString = accumulatorString.concat("<tr style=\"background-color:" + averageColor + ";\"><td>" + DateTime.now + "</td><td>Android/3G</td>")
  if(accumulatorMobileSuccessCount > 1){
    accumulatorString = accumulatorString.concat("<td>" + "Average of " + accumulatorDesktopSuccessCount + "pages  with recognised size issues</td>" +
      "<td>" + "Interactive" + "</td>" +
      "<td>" + mobileTimeFirstPaintInSeconds + "s</td>" +
      "<td>" + mobileAboveTheFoldCompleteInSec + "</td>" +
      "<td>" + mobileMBInDocComplete + "MB</td>" +
      "<td>" + mobileSuccessCount + " urls Tested Successfully</td>" +
      "<td> </td></tr>"
    )}
  else{
    if (accumulatorMobileSuccessCount == 1) {
      accumulatorString = accumulatorString.concat("<td> Results from 1 page with recognised size issues</td>" +
        "<td>" + "Interactive" + "</td>" +
        "<td>" + mobileTimeFirstPaintInSeconds + "s</td>" +
        "<td>" + mobileAboveTheFoldCompleteInSec + "</td>" +
        "<td>" + mobileMBInDocComplete + "MB</td>" +
        "<td>" + mobileSuccessCount + " urls Tested Successfully</td>" +
        "<td> </td></tr>"
      )
    }
    else {
      accumulatorString = accumulatorString.concat("<td> Standard values to be used for judging page size</td>" +
        "<td>" + "Interactive" + "</td>" +
        "<td>" + mobileTimeFirstPaintInSeconds + "s</td>" +
        "<td>" + mobileAboveTheFoldCompleteInSec + "</td>" +
        "<td>" + mobileMBInDocComplete + "MB</td>" +
        "<td>" + mobileSuccessCount + " urls Tested Successfully</td>" +
        "<td> </td></tr>"
      )
    }
  }

  override val formattedHTMLResultString: String = accumulatorString
}

