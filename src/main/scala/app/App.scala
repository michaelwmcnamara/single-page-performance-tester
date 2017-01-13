package app


import app.api._
import app.apiutils._
import com.gu.contentapi.client.model.v1._
import com.typesafe.config.{Config, ConfigFactory}
import org.joda.time.DateTime

import scala.collection.parallel.immutable.ParSeq


object App {
  def main(args: Array[String]) {
    val suppliedUrl = args(0)
    val exportPath = if(args(1).endsWith("/")){args(1)}else{args(1) + "/"}

    /*  This value stops the forces the config to be read and the output file to be written locally rather than reading and writing from/to S3
    #####################    this should be set to false before merging!!!!################*/
    val iamTestingLocally = false
    /*#####################################################################################*/
    println("Job started at: " + DateTime.now)
    println("Local Testing Flag is set to: " + iamTestingLocally.toString)

    val jobStart = DateTime.now
    //  Define names of s3bucket, configuration and output Files
    val s3BucketName = "capi-wpt-querybot"
    val configFileName = "config.conf"


    val pageResults = exportPath + "performanceComparisonSummary.txt"
    val pageList = exportPath + "performanceTestResults.csv"

    // summary files

    val jobStartHour = jobStart.hourOfDay.getAsString
    val jobStartDayOfWeek = jobStart.dayOfWeek.getAsString

    //Define colors to be used for average values, warnings and alerts
    val averageColor: String = "#d9edf7"
    //    val warningColor: String = "#fcf8e3"

    //initialize combinedResultsLists - these will be used to sort and accumulate test results
    // for the combined page and for long term storage file
    var combinedResultsList: List[PerformanceResultsObject] = List()


    //Initialize Page-Weight email alerts lists - these will be used to generate emails


    var pageWeightAnchorId: Int = 0

    //Initialize Interactive email alerts lists - these will be used to generate emails


    //Create new S3 Client
    println("defining new S3 Client (this is done regardless but only used if 'iamTestingLocally' flag is set to false)")
    val s3Interface = new S3Operations(s3BucketName, configFileName)
    var configArray: Array[String] = Array("", "", "", "", "", "")
    var urlFragments: List[String] = List()

    //Get config settings
    println("Extracting configuration values")
    if (!iamTestingLocally) {
      println(DateTime.now + " retrieving config from S3 bucket: " + s3BucketName)
      val returnTuple = s3Interface.getConfig
      configArray = Array(returnTuple._1, returnTuple._2, returnTuple._3, returnTuple._4, returnTuple._5, returnTuple._6, returnTuple._7)
      urlFragments = returnTuple._8
    }
    else {
      println(DateTime.now + " retrieving local config file: " + configFileName)
      val configReader = new LocalFileOperations
      configArray = configReader.readInConfig(configFileName)
    }
    println("checking validity of config values")
    if ((configArray(0).length < 1) || (configArray(1).length < 1) || (configArray(2).length < 1) || (configArray(3).length < 1)) {
      println("problem extracting config\n" +
        "contentApiKey length: " + configArray(0).length + "\n" +
        "wptBaseUrl length: " + configArray(1).length + "\n" +
        "wptApiKey length: " + configArray(2).length + "\n" +
        "wptLocation length: " + configArray(3).length + "\n" +
        "emailUsername length: " + configArray(4).length + "\n" +
        "emailPassword length: " + configArray(5).length) + "\n" +
        "visuals URL length: " + configArray(6).length

      System exit 1
    }
    println("config values ok")
    val wptBaseUrl: String = configArray(1)
    val wptApiKey: String = configArray(2)
    val wptLocation: String = configArray(3)


    // sendPageWeightAlert all urls to webpagetest at once to enable parallel testing by test agents

    val urlsToSend: List[String] = List(suppliedUrl, makeProdUrl(suppliedUrl))
    println("Sending " + urlsToSend.length + " urls.")
    val resultUrlList: List[(String, String)] = getResultPages(urlsToSend, urlFragments, wptBaseUrl, wptApiKey, wptLocation)
    // build result page listeners
    // first format alerts from previous test that arent in the new capi queries

    //obtain results for articles
    if (resultUrlList.nonEmpty) {
      println("Generating average values for articles")
      val articleAverages: PageAverageObject = new ArticleDefaultAverages(averageColor)
      val articleResultsList = listenForResultPages(urlsToSend, "Article", resultUrlList, articleAverages, wptBaseUrl, wptApiKey, wptLocation, urlFragments)
      combinedResultsList = articleResultsList
      println("\n \n \n article tests complete. \n tested " + articleResultsList.length + "pages")
      println("Total number of results gathered so far " + combinedResultsList.length + "pages")
      println("Article Performance Test Complete")

    } else {
      println("CAPI query found no article pages")
    }

    combinedResultsList.foreach(result => "speedindex = " + println(result.speedIndex) + "\n")
    val errorFreeCombinedResults = (for (result <- combinedResultsList if result.speedIndex > 0) yield result).toArray
    val errorFreeCombinedListLength = errorFreeCombinedResults.length

    val ComparisonSummary: String = {if (errorFreeCombinedListLength == 2) {
      val branchResult = errorFreeCombinedResults(0)
      val prodResult = errorFreeCombinedResults(1)

      val TitleString = "Comparison of performance between your branch and prod: \n"
      val StartRenderString = compareValues("Start-Render", branchResult.startRenderInMs - branchResult.timeToFirstByte, prodResult.startRenderInMs - prodResult.timeToFirstByte)
      val VisuallyCompleteString = compareValues("Visually-Complete", branchResult.visualComplete - branchResult.timeToFirstByte , prodResult.visualComplete - prodResult.timeToFirstByte)
      val SpeedIndexString = compareValues("SpeedIndex", branchResult.speedIndex, prodResult.speedIndex)
      val PageWeightString = compareValues("PageWeight", branchResult.bytesInFullyLoaded, prodResult.bytesInFullyLoaded)

      TitleString + StartRenderString + VisuallyCompleteString + SpeedIndexString + PageWeightString
      } else {
       "We were unable to measure your pages performance. Please check local webpagetest instance is working."
      }
    }

    //write combined results to file
      println(DateTime.now + " Writing results to " + pageResults )
      val outputWriter = new LocalFileOperations
      val writeSuccessSummary: Int = outputWriter.writeLocalResultFile(pageResults, ComparisonSummary)
      if (writeSuccessSummary != 0) {
        println("problem writing local outputfile")
        System exit 1
      }
      val writeSuccessList: Int = outputWriter.writeLocalResultFile(pageList, combinedResultsList.map(_.toCSVString()).mkString)
      if (writeSuccessList != 0) {
      println("problem writing local outputfile")
      System exit 1
    }

  }

  def compareValues(valueName: String, branch: Int, prod: Int): String ={
    val percentageDifference: (String, Double) = {
      if (branch > prod) {
        ("Greater", roundAt(2)(((branch - prod).toDouble / prod.toDouble) * 100))
      } else {
        if (branch < prod) {
          ("Less", roundAt(2)(((prod - branch).toDouble / prod.toDouble) * 100))
        } else {
          ("Equal", 0.0)
        }
      }
    }
    if (percentageDifference._1.contains("Greater") && (percentageDifference._2 > 3)){
      "Warning: " + valueName + " has increased by: " + percentageDifference._2 + "%.\n" +
        "Value of: " + valueName + " on the live site is: " + prod + "\n" +
        "Value of: " + valueName + " on your branch is: " + branch + "\n\n"
    } else {
      if (percentageDifference._1.contains("Less") && (percentageDifference._2 > 3)) {
        "Pass: " + valueName + " has decreased by: " + percentageDifference._2 + "%.\n" +
          "Value of " + valueName + " on the live site is: " + prod + "\n" +
          "Value of " + valueName + " on your branch is: " + branch + "\n\n"
      } else {
        "Pass: " + "No change of any significance for " + valueName + ".\n\n" +
          "Value of " + valueName + " on the live site is: " + prod + "\n" +
          "Value of " + valueName + " on your branch is: " + branch + "\n\n"
      }
    }
  }

  def getResultPages(urlList: List[String], urlFragments: List[String], wptBaseUrl: String, wptApiKey: String, wptLocation: String): List[(String, String)] = {
    val wpt: WebPageTest = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
    /*val desktopResults: List[(String, String)] = urlList.map(page => {
      (page, wpt.sendHighPriorityPage(page))
    })*/
    /*val desktopResults: List[(String, String)] = urlList.map(page => {
      (page, wpt.testMultipleTimes(page,"Desktop", wptLocation ,5))
    })*/
    /*val mobileResults: List[(String, String)] = urlList.map(page => {
      (page, wpt.sendHighPriorityMobile3GPage(page, wptLocation))
    })*/
    val mobileResults: List[(String, String)] = urlList.map(page => {
      (page, wpt.testMultipleTimes(page, "Android/3G", wptLocation, 5))
    })
    println("number of requests sent: " /*+ desktopResults.length*/ + mobileResults.length)
    /*desktopResults :::*/ mobileResults
  }

  def listenForResultPages(capiPages: List[String], contentType: String, resultUrlList: List[(String, String)], averages: PageAverageObject, wptBaseUrl: String, wptApiKey: String, wptLocation: String, urlFragments: List[String]): List[PerformanceResultsObject] = {
    println("ListenForResultPages called with: \n\n" +
      " Number of Urls: " + capiPages.length + "\n" +
      " List of Urls: \n" + capiPages.mkString +
      "\n\nNumber of WebPage Test results: " + capiPages.length + "\n" +
      "List of WebPage Test results: \n" + resultUrlList.mkString +
      "\n\nList of averages: \n" + averages.toHTMLString + "\n")

    val listenerList: List[WptResultPageListener] = capiPages.flatMap(page => {
      for (element <- resultUrlList if element._1 == page) yield new WptResultPageListener(element._1, contentType, element._2)
    })

    println("Listener List created: \n" + listenerList.map(element => "list element: \n" + "url: " + element.pageUrl + "\n" + "resulturl: " + element.wptResultUrl + "\n"))

    val resultsList: ParSeq[WptResultPageListener] = listenerList.par.map(element => {
      val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
      val newElement = new WptResultPageListener(element.pageUrl, element.pageType, element.wptResultUrl)
      println("getting result for page element: " + newElement.pageUrl)
      newElement.testResults = wpt.getMultipleResults(newElement.pageUrl,newElement.wptResultUrl)
      println("result received\n setting headline")
      newElement.testResults.setPageType(newElement.pageType)
      println("pagetype set\n setting FirstPublished")
      println("all variables set for element")
      newElement
    })
    println("Generating list of results objects from list of listener objects")
    val testResults = resultsList.map(element => element.testResults).toList
    println("setting alert status of results in list")
    val resultsWithAlerts: List[PerformanceResultsObject] = testResults.map(element => setAlertStatus(element, averages))
    println("about to return list of results")
    resultsWithAlerts
  }

  def setAlertStatus(resultObject: PerformanceResultsObject, averages: PageAverageObject): PerformanceResultsObject = {
    //  Add results to string which will eventually become the content of our results file
    if (resultObject.typeOfTest == "Desktop") {
      if (resultObject.kBInFullyLoaded >= averages.desktopKBInFullyLoaded) {
        println("PageWeight Alert Set")
        resultObject.pageWeightAlertDescription = "the page is too heavy. Please examine the list of embeds below for items that are unexpectedly large."
        resultObject.alertStatusPageWeight = true
      }
      else {
        println("PageWeight Alert not set")
        resultObject.alertStatusPageWeight = false
      }
      if ((resultObject.timeFirstPaintInMs >= averages.desktopTimeFirstPaintInMs) ||
          (resultObject.speedIndex >= averages.desktopSpeedIndex)) {
        println("PageSpeed alert set")
        resultObject.alertStatusPageSpeed = true
        if ((resultObject.timeFirstPaintInMs >= averages.desktopTimeFirstPaintInMs) && (resultObject.speedIndex >= averages.desktopSpeedIndex)) {
          resultObject.pageSpeedAlertDescription = "Time till page is scrollable (time-to-first-paint) and time till page looks loaded (SpeedIndex) are unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
        } else {
          if (resultObject.speedIndex >= averages.desktopSpeedIndex) {
            resultObject.pageSpeedAlertDescription = "Time till page looks loaded (SpeedIndex) is unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
          }
          else {
            resultObject.pageSpeedAlertDescription = "Time till page is scrollable (time-to-first-paint) is unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
          }
        }
      } else {
        println("PageSpeed alert not set")
        resultObject.alertStatusPageSpeed = false
      }
    } else {
      //checking if status of mobile test needs an alert
      if (resultObject.kBInFullyLoaded >= averages.mobileKBInFullyLoaded) {
        println("PageWeight Alert Set")
        resultObject.pageWeightAlertDescription = "the page is too heavy. Please examine the list of embeds below for items that are unexpectedly large."
        resultObject.alertStatusPageWeight = true
      }
      else {
        println("PageWeight Alert not set")
        resultObject.alertStatusPageWeight = false
      }
      if ((resultObject.timeFirstPaintInMs >= averages.mobileTimeFirstPaintInMs) ||
        (resultObject.speedIndex >= averages.mobileSpeedIndex)) {
        println("PageSpeed alert set")
        resultObject.alertStatusPageSpeed = true
        if ((resultObject.timeFirstPaintInMs >= averages.mobileTimeFirstPaintInMs) && (resultObject.speedIndex >= averages.mobileSpeedIndex)) {
          resultObject.pageSpeedAlertDescription = "Time till page is scrollable (time-to-first-paint) and time till page looks loaded (SpeedIndex) are unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
        } else {
          if (resultObject.speedIndex >= averages.mobileSpeedIndex) {
            resultObject.pageSpeedAlertDescription = "Time till page looks loaded (SpeedIndex) is unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
          }
          else {
            resultObject.pageSpeedAlertDescription = "Time till page is scrollable (time-to-first-paint) is unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
          }
        }
      } else {
        println("PageSpeed alert not set")
        resultObject.alertStatusPageSpeed = false
      }
    }
    println("Returning test result with alert flags set to relevant values")
    resultObject
  }

  def generateInteractiveAverages(urlList: List[String], wptBaseUrl: String, wptApiKey: String, wptLocation: String, urlFragments: List[String], itemtype: String, averageColor: String): PageAverageObject = {
    val setHighPriority: Boolean = true
    val webpageTest: WebPageTest = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)

    val resultsList: List[Array[PerformanceResultsObject]] = urlList.map(url => {
      val webPageDesktopTestResults: PerformanceResultsObject = webpageTest.desktopChromeCableTest(url, setHighPriority)
      val webPageMobileTestResults: PerformanceResultsObject = webpageTest.mobileChrome3GTest(url, wptLocation, setHighPriority)
      val combinedResults = Array(webPageDesktopTestResults, webPageMobileTestResults)
      combinedResults
    })

    val pageAverages: PageAverageObject = new GeneratedInteractiveAverages(resultsList, averageColor)
    pageAverages
  }

  def applyAnchorId(resultsObjectList: List[PerformanceResultsObject], lastIDAssigned: Int): (List[PerformanceResultsObject], Int) = {
    var iterator = lastIDAssigned + 1
    val resultList = for (result <- resultsObjectList) yield {
      result.anchorId = Option(result.headline.getOrElse(iterator) + result.typeOfTest)
      iterator = iterator + 1
      result
    }
    (resultList,iterator)
  }



  def jodaDateTimetoCapiDateTime(time: DateTime): CapiDateTime = {
    new CapiDateTime {
      override def dateTime: Long = time.getMillis
    }
  }

  def makeProdUrl(url: String): String = {
    if(url.take(4).contains("http")) {
      val contentPath: List[String] = url.split("/").toList.drop(3).map(fragment => "/" + fragment)
      (List("https://www.theguardian.com") ::: contentPath).mkString
    } else {
      val contentPath: List[String] = url.split("/").toList.drop(1).map(fragment => "/" + fragment)
      (List("https://www.theguardian.com") ::: contentPath).mkString
    }
  }

  def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) / s }
}


