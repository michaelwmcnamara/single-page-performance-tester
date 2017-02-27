package app.apiutils

//import app.api.PerformanceResultsObject
import com.squareup.okhttp._
import org.joda.time.DateTime

import scala.xml.Elem


/**
 * Created by mmcnamara on 14/12/15.
 */
class WebPageTest(baseUrl: String, passedKey: String, urlFragments: List[String]) {

  val apiBaseUrl:String = baseUrl
  val apihost: String = baseUrl.split("http://")(1)
  val apiKey:String = passedKey
  val fragments: String = urlFragments.map(x => "#" + x).mkString

  val wptResponseFormat:String = "xml"
  implicit val httpClient = new OkHttpClient()

  val msmaxTime: Int = 120000
  val msTimeBetweenPings: Int = 10000
  val maxCount: Int = roundAt(0)(msmaxTime.toDouble / msTimeBetweenPings).toInt

  val msmaxTimeForMultipleTests: Int = 600000
  val msTimeBetweenPingsForMultipleTests: Int = 20000
  val maxCountForMultipleTests: Int = roundAt(0)(msmaxTimeForMultipleTests.toDouble / msTimeBetweenPingsForMultipleTests).toInt


  def desktopChromeCableTest(gnmPageUrl:String, highPriority: Boolean = false): PerformanceResultsObject = {
    println("Sending desktop webpagetest request to WPT API")
    if (highPriority) {
      val resultPage: String = sendHighPriorityPage(gnmPageUrl)
      println("Accessing results at: " + resultPage)
      val testResults: PerformanceResultsObject = getResults(gnmPageUrl, resultPage)
      println("Results returned")
      testResults
    }else {
      val resultPage: String = sendPage(gnmPageUrl)
      println("Accessing results at: " + resultPage)
      val testResults: PerformanceResultsObject = getResults(gnmPageUrl, resultPage)
      println("Results returned")
      testResults
    }
  }

  def mobileChrome3GTest(gnmPageUrl:String, wptLocation: String, highPriority: Boolean = false): PerformanceResultsObject = {
    println("Sending mobile webpagetest request to WPT API")
    if(highPriority){
      val resultPage: String = sendHighPriorityMobile3GPage(gnmPageUrl, wptLocation)
      println("Accessing results at: " + resultPage)
      val testResults: PerformanceResultsObject = getResults(gnmPageUrl, resultPage)
      testResults
    }else {
      val resultPage: String = sendMobile3GPage(gnmPageUrl, wptLocation)
      println("Accessing results at: " + resultPage)
      val testResults: PerformanceResultsObject = getResults(gnmPageUrl, resultPage)
      testResults
    }
  }

  def sendPage(gnmPageUrl:String): String = {
    println("Forming desktop webpage test query")
    val getUrl: HttpUrl = new HttpUrl.Builder()
    .scheme("http")
    .host(apihost)
    .addPathSegment("/runtest.php")
    .addQueryParameter("f", wptResponseFormat)
    .addQueryParameter("k", apiKey)
    .addQueryParameter("fvonly", "1")
    .addQueryParameter("priority", "3")
    .addQueryParameter("url", gnmPageUrl + fragments)
    .build()

    val request: Request = new Request.Builder()
      .url(getUrl)
      .get()
      .build()

    println("sending request: " + request.toString)
    val response: Response = httpClient.newCall(request).execute()
    println("Response: \n" + response )
    val responseXML: Elem = scala.xml.XML.loadString(response.body.string)
    println("response received: \n" + responseXML.text)
    val resultPage: String =  (responseXML \\ "xmlUrl").text
    println(resultPage)
    resultPage
  }

  def sendHighPriorityPage(gnmPageUrl:String): String = {
    println("Forming high prioirty desktop webpage test query")
    val getUrl: HttpUrl = new HttpUrl.Builder()
    .scheme("http")
    .host(apihost)
    .addPathSegment("/runtest.php")
    .addQueryParameter("f", wptResponseFormat)
    .addQueryParameter("k", apiKey)
    .addQueryParameter("fvonly", "1")
    .addQueryParameter("priority", "1")
    .addQueryParameter("url", gnmPageUrl + fragments)
    .build()

    val request: Request = new Request.Builder()
      .url(getUrl)
      .get()
      .build()

    println("sending request: " + request.toString)
    val response: Response = httpClient.newCall(request).execute()
    val responseXML: Elem = scala.xml.XML.loadString(response.body.string)
    println("response received: \n" + responseXML.text)
    val resultPage: String =  (responseXML \\ "xmlUrl").text
    println(resultPage)
    resultPage
  }


  def sendMobile3GPage(gnmPageUrl:String, wptLocation: String): String = {
    println("Forming high-priority mobile 3G webpage test query")
//    val getUrl: String = apiBaseUrl + "/runtest.php?" + "&f=" + wptResponseFormat + "&k=" + apiKey + "&mobile=1&mobileDevice=Nexus5&location=" + wptLocation + ":Chrome.3G" + "&url=" + gnmPageUrl + "noads"
    val getUrl: HttpUrl = new HttpUrl.Builder()
    .scheme("http")
    .host(apihost)
    .addPathSegment("/runtest.php")
    .addQueryParameter("f", wptResponseFormat)
    .addQueryParameter("k", apiKey)
    .addQueryParameter("fvonly", "1")
    .addQueryParameter("priority", "3")
    .addQueryParameter("mobile", "1")
    .addQueryParameter("mobileDevice", "Nexus5")
    .addQueryParameter("location", wptLocation + ":Chrome.3G")
    .addQueryParameter("url", gnmPageUrl + fragments)
    .build()

    val request: Request = new Request.Builder()
      .url(getUrl)
      .get()
      .build()

    println("sending request: " + request.toString)
    val response: Response = httpClient.newCall(request).execute()
    val responseXML: Elem = scala.xml.XML.loadString(response.body.string)
    println("response received: \n" + responseXML.text)
    val resultPage: String =  (responseXML \\ "xmlUrl").text
    resultPage
  }

  def sendHighPriorityMobile3GPage(gnmPageUrl:String, wptLocation: String): String = {
    println("Forming mobile 3G webpage test query")
    val getUrl: HttpUrl = new HttpUrl.Builder()
      .scheme("http")
      .host(apihost)
      .addPathSegment("/runtest.php")
      .addQueryParameter("f", wptResponseFormat)
      .addQueryParameter("k", apiKey)
      .addQueryParameter("fvonly", "1")
      .addQueryParameter("mobile", "1")
      .addQueryParameter("mobileDevice", "Nexus5")
      .addQueryParameter("location", wptLocation + ":Chrome.3G")
      .addQueryParameter("priority", "1")
      .addQueryParameter("url", gnmPageUrl + fragments)
      .build()

    val request: Request = new Request.Builder()
      .url(getUrl)
      .get()
      .build()

    println("sending request: " + request.toString)
    val response: Response = httpClient.newCall(request).execute()
    val responseXML: Elem = scala.xml.XML.loadString(response.body.string)
    println("response received: \n" + responseXML.text)
    val resultPage: String =  (responseXML \\ "xmlUrl").text
    resultPage
  }


  def getResults(pageUrl: String, resultUrl: String):PerformanceResultsObject = {
    println("Requesting result url:" + resultUrl)
    val request: Request = new Request.Builder()
      .url(resultUrl)
      .get()
      .build()
    println("request is:" + request)
    var response: Response = httpClient.newCall(request).execute()
    println("Processing response and checking if results are ready")
    var testResults: Elem = scala.xml.XML.loadString(response.body.string)
    var iterator: Int = 0
    while (((testResults \\ "statusCode").text.toInt != 200) && ((testResults \\ "statusCode").text.toInt != 404) && (iterator < maxCount)) {
      println(DateTime.now + " " + (testResults \\ "statusCode").text + " statusCode response - test not ready. " + iterator + " of " + maxCount + " attempts\n")
      Thread.sleep(msTimeBetweenPings)
      iterator += 1
      response = httpClient.newCall(request).execute()
      testResults = scala.xml.XML.loadString(response.body.string)
    }
    if(iterator < maxCount) {
      if ((testResults \\ "statusCode").text.toInt == 200) {
        //Add one final request as occasionally 200 code comes before the data we want.
        Thread.sleep(3000)
        response = httpClient.newCall(request).execute()
        testResults = scala.xml.XML.loadString(response.body.string)
        if ((testResults \\ "response" \ "data" \ "successfulFVRuns").text.toInt > 0) {
          println("\n" + DateTime.now + " statusCode == 200: Page ready after " + ((iterator + 1) * msTimeBetweenPings).toDouble / 1000 + " seconds\n Refining results")
          try {
            refineResults(pageUrl, testResults)
          } catch {
            case _: Throwable => {
              println("Page failed for some reason")
              failedTestUnknown(pageUrl, testResults)
            }
          }
        } else {
          println(DateTime.now + " Test results show 0 successful runs ")
          failedTestNoSuccessfulRuns(pageUrl, testResults)
        }
      } else {
        if ((testResults \\ "statusCode").text.toInt == 404) {
          println(DateTime.now + " Test returned 404 error. Test ID: " + resultUrl + " is not valid")
          failedTestUnknown(pageUrl, testResults)
        } else {
          println(DateTime.now + " Test failed for unknown reason. Test ID: " + resultUrl)
          failedTestUnknown(pageUrl, testResults)
        }
      }
    } else {
      println(DateTime.now + " Test timed out after " + ((iterator + 1) * msTimeBetweenPings).toDouble / 1000 + " seconds")
      println("Test id is: "+ resultUrl)
      failedTestTimeout(pageUrl, resultUrl)
    }
  }

  def refineResults(pageUrl: String, rawXMLResult: Elem): PerformanceResultsObject = {
    println("parsing the XML results")
    try {
      val testUrl: String = (rawXMLResult \\ "response" \ "data" \ "testUrl").text.toString.split("#noads")(0)
      val testType: String = {
        if ((rawXMLResult \\ "response" \ "data" \ "from").text.toString.contains("Emulated Nexus 5")) {
          "Android/3G"
        } else {
          "Desktop"
        }
      }
      val testSummaryPage: String = (rawXMLResult \\ "response" \ "data" \ "summary").text.toString
      val timeToFirstByte: Int = (rawXMLResult \\ "response" \ "data" \ "run" \ "firstView" \ "results" \ "TTFB").text.toInt
      val firstPaint: Int = (rawXMLResult \\ "response" \ "data" \ "run" \ "firstView" \ "results" \ "firstPaint").text.toInt
      println("firstPaint = " + firstPaint)
      val startRender: Int = (rawXMLResult \\ "response" \ "data" \ "run" \ "firstView" \ "results" \ "render").text.toInt
      println("start render = " + startRender)
      val docTime: Int = (rawXMLResult \\ "response" \ "data" \ "run" \ "firstView" \ "results" \ "docTime").text.toInt
      println("docTime = " + docTime)
      val bytesInDoc: Int = (rawXMLResult \\ "response" \ "data" \ "run" \ "firstView" \ "results" \ "bytesInDoc").text.toInt
      println("bytesInDoc = " + bytesInDoc)
      val fullyLoadedTime: Int = (rawXMLResult \\ "response" \ "data" \ "run" \ "firstView" \ "results" \ "fullyLoaded").text.toInt
      println("Time to Fully loaded = " + fullyLoadedTime)
      val totalbytesIn: Int = (rawXMLResult \\ "response" \ "data" \ "run" \ "firstView" \ "results" \ "bytesIn").text.toInt
      println("Total bytes = " + totalbytesIn)
      val speedIndex: Int = (rawXMLResult \\ "response" \ "data" \ "run" \ "firstView" \ "results" \ "SpeedIndex").text.toInt
      println("SpeedIndex = " + speedIndex)
      val visualComplete: Int = (rawXMLResult \\ "response" \ "data" \ "run" \ "firstView" \ "results" \ "visualComplete").text.toInt
      println("Visually Complete = " + visualComplete)
      val status: String = "Test Success"

      println("Creating PerformanceResultsObject")
      val result: PerformanceResultsObject = new PerformanceResultsObject(testUrl, testType, testSummaryPage, timeToFirstByte, startRender, firstPaint, docTime, bytesInDoc, fullyLoadedTime, totalbytesIn, speedIndex, visualComplete, status, false)
      println("Returning PerformanceResultsObject")
      result
    } catch {
      case _: Throwable => {
        println("Page failed for some reason while refining results")
        failedTestUnknown(pageUrl, rawXMLResult)
      }
    }
  }

  def testMultipleTimes(url: String, typeOfTest: String, wptLocation: String, testCount: Int): String = {
      println("Alert registered on url: " + url + "\n" + "verify by retesting " + testCount + " times and taking median value")
      if(typeOfTest.contains("Desktop")){
        println("Forming desktop webpage test query to confirm alert status")
        val getUrl: HttpUrl = new HttpUrl.Builder()
          .scheme("http")
          .host(apihost)
          .addPathSegment("/runtest.php")
          .addQueryParameter("f", wptResponseFormat)
          .addQueryParameter("k", apiKey)
          .addQueryParameter("runs", testCount.toString)
          .addQueryParameter("priority", "1")
          .addQueryParameter("fvonly", "1")
          .addQueryParameter("url", url + fragments)
          .build()

        val request: Request = new Request.Builder()
          .url(getUrl)
          .get()
          .build()

        println("sending request: " + request.toString)
        val response: Response = httpClient.newCall(request).execute()
        val responseXML: Elem = scala.xml.XML.loadString(response.body.string)
        val resultPage: String =  (responseXML \\ "xmlUrl").text
        println(resultPage)
        //val testResultObject: PerformanceResultsObject = getMultipleResults(url, resultPage)
        //testResultObject
        resultPage
    }
    else{
        println("Forming mobile 3G webpage test query to confirm alert status")
        val getUrl: HttpUrl = new HttpUrl.Builder()
          .scheme("http")
          .host(apihost)
          .addPathSegment("/runtest.php")
          .addQueryParameter("f", wptResponseFormat)
          .addQueryParameter("k", apiKey)
          .addQueryParameter("mobile", "1")
          .addQueryParameter("mobileDevice", "Nexus5")
          .addQueryParameter("location", wptLocation + ":Chrome.3G")
          .addQueryParameter("runs", testCount.toString)
          .addQueryParameter("priority", "1")
          .addQueryParameter("fvonly", "1")
          .addQueryParameter("url", url + fragments)
          .build()

        val request: Request = new Request.Builder()
          .url(getUrl)
          .get()
          .build()

        println("sending request: " + request.toString)
        val response: Response = httpClient.newCall(request).execute()
        val responseXML: Elem = scala.xml.XML.loadString(response.body.string)
        val resultPage: String =  (responseXML \\ "xmlUrl").text
        println(resultPage)
        //val testResultObject: PerformanceResultsObject = getMultipleResults(url, resultPage)
        //testResultObject
        resultPage
      }
  }


  def getMultipleResults(pageUrl: String, resultUrl: String): PerformanceResultsObject = {
    println("Requesting url:" + resultUrl)
    val request: Request = new Request.Builder()
      .url(resultUrl)
      .get()
      .build()
    var response: Response = httpClient.newCall(request).execute()
    println("Processing response and checking if results are ready")
    var testResults: Elem = scala.xml.XML.loadString(response.body.string)
    var iterator: Int = 0
    while (((testResults \\ "statusCode").text.toInt != 200) && (iterator < maxCountForMultipleTests)) {
      println(DateTime.now + " " + (testResults \\ "statusCode").text + " statusCode response - test not ready. " + iterator + " of " + maxCountForMultipleTests + " attempts\n")
      Thread.sleep(msTimeBetweenPingsForMultipleTests)
      iterator += 1
      response = httpClient.newCall(request).execute()
      testResults = scala.xml.XML.loadString(response.body.string)
    }
    if ((testResults \\ "statusCode").text.toInt == 200) {
      println("Add one final request as occasionally 200 code comes before the data we want.")
      Thread.sleep(20000)
      response = httpClient.newCall(request).execute()
      testResults = scala.xml.XML.loadString(response.body.string)
      if ((testResults \\ "response" \ "data" \ "successfulFVRuns").text.toInt > 0) {
        println("\n" + DateTime.now + " statusCode == 200: Page ready after " + roundAt(0)(((iterator + 1) * msTimeBetweenPings).toDouble / 1000).toInt + " seconds\n Refining results")
        try {
          refineMultipleResults(pageUrl, testResults)
        } catch {
          case _: Throwable => {
            println("Page failed for some reason")
            failedTestUnknown(pageUrl, testResults)
          }
        }
      } else {
        println(DateTime.now + " Test results show 0 successful runs ")
        failedTestNoSuccessfulRuns(pageUrl, testResults)
      }
    } else {
      println(DateTime.now + " Test timed out after " + roundAt(0)(((iterator + 1) * msTimeBetweenPings) / 1000).toInt + " seconds")
      failedTestTimeout(pageUrl, resultUrl)
    }
  }

  def refineMultipleResults(pageUrl: String, rawXMLResult: Elem): PerformanceResultsObject = {
    println("parsing the XML results")
    try {
      val testUrl: String = (rawXMLResult \\ "response" \ "data" \ "testUrl").text.toString.split("#noads")(0)
      val testType: String = if ((rawXMLResult \\ "response" \ "data" \ "from").text.toString.contains("Emulated Nexus 5")) {
        "Android/3G"
      } else {
        "Desktop"
      }
      val testSummaryPage: String = (rawXMLResult \\ "response" \ "data" \ "summary").text.toString
      val timeToFirstByte: Int = (rawXMLResult \\ "response" \ "data" \ "median" \ "firstView" \ "TTFB").text.toInt
      val firstPaint: Int = (rawXMLResult \\ "response" \ "data" \ "median" \ "firstView" \ "firstPaint").text.toInt
      println("firstPaint = " + firstPaint)
      val startRender: Int = (rawXMLResult \\ "response" \ "data" \ "median" \ "firstView" \ "render").text.toInt
      println("start render = " + startRender)
      val docTime: Int = (rawXMLResult \\ "response" \ "data" \ "median" \ "firstView" \ "docTime").text.toInt
      println("docTime = " + docTime)
      val bytesInDoc: Int = (rawXMLResult \\ "response" \ "data" \ "median" \ "firstView" \ "bytesInDoc").text.toInt
      println("bytesInDoc = " + bytesInDoc)
      val fullyLoadedTime: Int = (rawXMLResult \\ "response" \ "data" \ "median" \ "firstView" \ "fullyLoaded").text.toInt
      println("Time to Fully loaded = " + fullyLoadedTime)
      val totalbytesIn: Int = (rawXMLResult \\ "response" \ "data" \ "median" \ "firstView" \ "bytesIn").text.toInt
      println("Total bytes = " + totalbytesIn)
      val speedIndex: Int = (rawXMLResult \\ "response" \ "data" \ "median" \ "firstView" \ "SpeedIndex").text.toInt
      println("SpeedIndex = " + speedIndex)
      val visualComplete: Int = (rawXMLResult \\ "response" \ "data" \ "median" \ "firstView" \ "visualComplete").text.toInt
      println("Visually Complete = " + visualComplete)
      val status: String = "Test Success"
      println("Creating PerformanceResultsObject")
      val result: PerformanceResultsObject = new PerformanceResultsObject(testUrl, testType, testSummaryPage, timeToFirstByte, startRender, firstPaint, docTime, bytesInDoc, fullyLoadedTime, totalbytesIn, speedIndex, visualComplete, status, false)
      println("Returning PerformanceResultsObject")
      result
    } catch {
      case _: Throwable => {
        println("Page failed for some reason")
        failedTestUnknown(pageUrl, rawXMLResult)
      }
    }
  }

  def obtainPageRequestDetails(webpageTestResultUrl: String): List[PageElementFromHTMLTableRow] = {
    val sliceStart: Int = apiBaseUrl.length + "/xmlResult/".length
    val sliceEnd: Int = webpageTestResultUrl.length - 1
    val testId: String = webpageTestResultUrl.slice(sliceStart,sliceEnd)
    val resultDetailsPage: String =  apiBaseUrl + "/result/" + testId + "/1/details/"
    val request:Request  = new Request.Builder()
      .url(resultDetailsPage)
      .get()
      .build()
    val response: Response = httpClient.newCall(request).execute()
    val responseString:String = response.body.string
    println("calling trimToHTMLTable")
    val tableString: String = trimToHTMLTable(responseString)
    println("calling generatePageElementList")
    val pageElementList: List[PageElementFromHTMLTableRow] = generatePageElementList(tableString)
    println("List generated - contains: " + pageElementList.length + " elements.")
    pageElementList
  }


  def trimToHTMLTable(pageHTML: String): String = {
    val responseStringOuterTableStart: Int = pageHTML.indexOf("<table class=\"tableDetails details center\">")
    val responseStringOuterTableEnd: Int = pageHTML.indexOf("</table>", responseStringOuterTableStart)
    val outerTableString: String = pageHTML.slice(responseStringOuterTableStart, responseStringOuterTableEnd)
    val innerTableStart: Int = outerTableString.indexOf("<tbody>")
    val innerTableEnd: Int = outerTableString.indexOf("</tbody>")
    val innerTableString: String = outerTableString.slice(innerTableStart, innerTableEnd)
    val tableDataRows: String = innerTableString.slice(innerTableString.indexOf("<tr>"), innerTableString.length)
    tableDataRows
  }

  def generatePageElementList(htmlTableRows: String): List[PageElementFromHTMLTableRow] = {
    var restOfTable: String = htmlTableRows
    var pageElementList: List[PageElementFromHTMLTableRow] = List()
    var counter: Int = 0
    while (restOfTable.nonEmpty){
      val (currentRow, rest): (String, String) = restOfTable.splitAt(restOfTable.indexOf("</tr>")+5)
      pageElementList = pageElementList :+ new PageElementFromHTMLTableRow(currentRow)
      restOfTable = rest
      counter += 1
    }
    pageElementList
  }

  def failedTestNoSuccessfulRuns(url: String, rawResults: Elem): PerformanceResultsObject = {
    val failIndicator: Int = -1
    val testType: String = if((rawResults \\ "response" \ "data" \ "from").text.toString.contains("Emulated Nexus 5")){"Android/3G"}else{"Desktop"}
    val testSummaryPage: String = (rawResults \\ "response" \ "data" \ "summary").text.toString
    val failComment: String = "No successful runs of test"
    val failElement: PerformanceResultsObject = new PerformanceResultsObject(url, testType, testSummaryPage, failIndicator, failIndicator, failIndicator, failIndicator,failIndicator,failIndicator,failIndicator,failIndicator, failIndicator, failComment, true)
    failElement
  }

  def failedTestTimeout(url: String, resultUrl: String): PerformanceResultsObject = {
    val failIndicator: Int = -1
    val testType: String = "Unknown"
    val failComment: String = "Test request timed out"
    // set warning status as result may have timed out due to very large page
    val failElement: PerformanceResultsObject = new PerformanceResultsObject(url, testType, failComment, failIndicator, failIndicator, failIndicator, failIndicator,failIndicator,failIndicator,failIndicator,failIndicator, failIndicator, failComment, true)
    failElement
  }

  def failedTestUnknown(url: String, rawResults: Elem): PerformanceResultsObject = {
    val failIndicator: Int = -2
    val testType: String = "Unknown"
    val failComment: String = "Test failed for unknown reason"
    // set warning status as result may have timed out due to very large page
    val failElement: PerformanceResultsObject = new PerformanceResultsObject(url, testType, failComment, failIndicator, failIndicator, failIndicator, failIndicator,failIndicator,failIndicator,failIndicator,failIndicator, failIndicator, rawResults.mkString, true)
    failElement
  }

  def sortPageElementList(elementList: List[PageElementFromHTMLTableRow]):List[PageElementFromHTMLTableRow] = {
    elementList.sortWith(_.bytesDownloaded > _.bytesDownloaded)
  }

  def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) / s }

}
