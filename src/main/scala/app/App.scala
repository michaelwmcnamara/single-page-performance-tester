package app

// note an _ instead of {} would get everything

import java.io._
import java.util

import app.api._
import app.apiutils._
import com.gu.contentapi.client.model.v1._
import com.typesafe.config.{Config, ConfigFactory}
import org.joda.time.DateTime
import sbt.complete.Completion

import scala.collection.parallel.immutable.ParSeq
import scala.io.Source


object App {
  def main(args: Array[String]) {
    /*  This value stops the forces the config to be read and the output file to be written locally rather than reading and writing from/to S3
    #####################    this should be set to false before merging!!!!################*/
    val iamTestingLocally = false
    /*#####################################################################################*/
    println("Job started at: " + DateTime.now)
    println("Local Testing Flag is set to: " + iamTestingLocally.toString)

    val jobStart = DateTime.now
    val startOfWeek = jobStart.minusDays(jobStart.getDayOfWeek).minusMillis(jobStart.getMillisOfDay)
    //  Define names of s3bucket, configuration and output Files
    val amazonDomain = "https://s3-eu-west-1.amazonaws.com"
    val s3BucketName = "capi-wpt-querybot"
    val configFileName = "config.conf"

    val editorialPageweightFilename = "editorialpageweightdashboard.html"
    val editorialDesktopPageweightFilename = "editorialpageweightdashboarddesktop.html"
    val editorialMobilePageweightFilename = "editorialpageweightdashboardmobile.html"
    val dotcomPageSpeedFilename = "dotcompagespeeddashboard.html"

    val pageResults = "singlePageTestResults.csv"

    val alertsThatHaveBeenFixed = "alertsthathavebeenfixed.csv"
    val pagesWithInsecureElements = "pagesWithInsecureElements.csv"
    val duplicateResultList = "duplicateresultsfromlastrun.csv"
    val runLog = "runLog.csv"

    // summary files

    val jobStartHour = jobStart.hourOfDay.getAsString
    val jobStartDayOfWeek = jobStart.dayOfWeek.getAsString

    val runSummaryFile = "runLogs/runSummary" + jobStartDayOfWeek + "_" + jobStartHour + ".txt"
    val pageWeightAlertSummaryFile = "runLogs/pageWeightAlertSummary" + jobStartDayOfWeek + "_" + jobStartHour + ".txt"
    val interactiveAlertSummaryFile = "runLogs/interactiveAlertSummary" + jobStartDayOfWeek + "_" + jobStartHour + ".txt"


    //Define colors to be used for average values, warnings and alerts
    val averageColor: String = "#d9edf7"
    //    val warningColor: String = "#fcf8e3"
    val warningColor: String = "rgba(227, 251, 29, 0.32)"
    val alertColor: String = "#f2dede"

    //initialize combinedResultsLists - these will be used to sort and accumulate test results
    // for the combined page and for long term storage file
    var combinedResultsList: List[PerformanceResultsObject] = List()


    //Initialize Page-Weight email alerts lists - these will be used to generate emails

    var articlePageWeightAlertList: List[PerformanceResultsObject] = List()
    var liveBlogPageWeightAlertList: List[PerformanceResultsObject] = List()
    var interactivePageWeightAlertList: List[PerformanceResultsObject] = List()
    var frontsPageWeightAlertList: List[PerformanceResultsObject] = List()
    var audioPageWeightAlertList: List[PerformanceResultsObject] = List()
    var videoPageWeightAlertList: List[PerformanceResultsObject] = List()

    var pageWeightAnchorId: Int = 0

    //Initialize Interactive email alerts lists - these will be used to generate emails
    var interactiveAlertList: List[PerformanceResultsObject] = List()
    var gLabsAlertList: List[PerformanceResultsObject] = List()


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
    val contentApiKey: String = configArray(0)
    val wptBaseUrl: String = configArray(1)
    val wptApiKey: String = configArray(2)
    val wptLocation: String = configArray(3)
    val emailUsername: String = configArray(4)
    val emailPassword: String = configArray(5)
    val visualsApiUrl: String = configArray(6)


    // sendPageWeightAlert all urls to webpagetest at once to enable parallel testing by test agents
    val urlsToSend: List[String] = List("https://www.theguardian.com/books/2017/jan/10/ruby-wax-defuse-my-depression-a-mindfulness-guide-for-the-frazzled")
    println("Combined list of urls: \n" + urlsToSend)

    val resultUrlList: List[(String, String)] = getResultPages(urlsToSend, urlFragments, wptBaseUrl, wptApiKey, wptLocation)
    // build result page listeners
    // first format alerts from previous test that arent in the new capi queries

    //obtain results for articles
    if (resultUrlList.nonEmpty) {
      println("Generating average values for articles")
      val articleAverages: PageAverageObject = new ArticleDefaultAverages(averageColor)
      val articleResultsList = listenForResultPages(urlsToSend, "Article", resultUrlList, articleAverages, wptBaseUrl, wptApiKey, wptLocation, urlFragments)
      val getAnchorId: (List[PerformanceResultsObject], Int) = applyAnchorId(articleResultsList, pageWeightAnchorId)
      val articleResultsWithAnchor = getAnchorId._1
      pageWeightAnchorId = getAnchorId._2

      combinedResultsList = articleResultsWithAnchor
      println("\n \n \n article tests complete. \n tested " + articleResultsWithAnchor.length + "pages")
      println("Total number of results gathered so far " + combinedResultsList.length + "pages")
      println("Article Performance Test Complete")

    } else {
      println("CAPI query found no article pages")
    }

    val errorFreeSortedByWeightCombinedResults = for (result <- combinedResultsList if result.speedIndex > 0) yield result
    val errorFreeCombinedListLength = errorFreeSortedByWeightCombinedResults.length
    println("length of errorFreeSortedByWeightCombinedResults: " + errorFreeCombinedListLength)

   // val editorialPageWeightDashboardDesktop = new PageWeightDashboardDesktop(sortedByWeightCombinedResults, sortedByWeightCombinedDesktopResults, sortedByWeightCombinedMobileResults)
   // val editorialPageWeightDashboardMobile = new PageWeightDashboardMobile(sortedByWeightCombinedResults, sortedByWeightCombinedDesktopResults, sortedByWeightCombinedMobileResults)
  //  val editorialPageWeightDashboard = new PageWeightDashboardTabbed(sortedByWeightCombinedResults, sortedByWeightCombinedDesktopResults, sortedByWeightCombinedMobileResults)

    // record results
    val combinedResultsForFile = errorFreeSortedByWeightCombinedResults.filter(_.fullElementList.nonEmpty)

    println("combinedResultsForFile length = " + combinedResultsForFile.length)
    val resultsToRecord = combinedResultsForFile

    //val resultsToRecord = (combinedResultsForFile ::: previousResultsWithElementsAdded).distinct
    println("\n\n\n ***** There are " + resultsToRecord.length + " results to be saved to the previous results file  ********* \n\n\n")
    val resultsToRecordCSVString: String = resultsToRecord.map(_.toCSVString()).mkString

    //write combined results to file
    if (!iamTestingLocally) {
      println(DateTime.now + " Writing liveblog results to S3")
    //  s3Interface.writeFileToS3(editorialDesktopPageweightFilename, editorialPageWeightDashboardDesktop.toString())
    //  s3Interface.writeFileToS3(editorialMobilePageweightFilename, editorialPageWeightDashboardMobile.toString())
    //  s3Interface.writeFileToS3(editorialPageweightFilename, editorialPageWeightDashboard.toString())
      s3Interface.writeFileToS3(pageResults, resultsToRecordCSVString)
    }
    else {
      val outputWriter = new LocalFileOperations
      /*val writeSuccessPWDC: Int = outputWriter.writeLocalResultFile(editorialPageweightFilename, editorialPageWeightDashboard.toString())
      if (writeSuccessPWDC != 0) {
        println("problem writing local outputfile")
        System exit 1
      }
      val writeSuccessPWDD: Int = outputWriter.writeLocalResultFile(editorialDesktopPageweightFilename, editorialPageWeightDashboardDesktop.toString())
      if (writeSuccessPWDD != 0) {
        println("problem writing local outputfile")
        System exit 1
      }
      val writeSuccessPWDM: Int = outputWriter.writeLocalResultFile(editorialMobilePageweightFilename, editorialPageWeightDashboardMobile.toString())
      if (writeSuccessPWDM != 0) {
        println("problem writing local outputfile")
        System exit 1
      }*/
      val writeSuccessAlertsRecord: Int = outputWriter.writeLocalResultFile(pageResults, resultsToRecordCSVString)
      if (writeSuccessAlertsRecord != 0) {
        println("problem writing local outputfile")
        System exit 1
      }
    }


  }

  def getResultPages(urlList: List[String], urlFragments: List[String], wptBaseUrl: String, wptApiKey: String, wptLocation: String): List[(String, String)] = {
    val wpt: WebPageTest = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
    val desktopResults: List[(String, String)] = urlList.map(page => {
      (page, wpt.sendHighPriorityPage(page))
    })
    val mobileResults: List[(String, String)] = urlList.map(page => {
      (page, wpt.sendHighPriorityMobile3GPage(page, wptLocation))
    })
    desktopResults ::: mobileResults
  }

  def listenForResultPages(capiPages: List[String], contentType: String, resultUrlList: List[(String, String)], averages: PageAverageObject, wptBaseUrl: String, wptApiKey: String, wptLocation: String, urlFragments: List[String]): List[PerformanceResultsObject] = {
    println("ListenForResultPages called with: \n\n" +
      " List of Urls: \n" + capiPages.mkString +
      "\n\nList of WebPage Test results: \n" + resultUrlList.mkString +
      "\n\nList of averages: \n" + averages.toHTMLString + "\n")

    val listenerList: List[WptResultPageListener] = capiPages.flatMap(page => {
      for (element <- resultUrlList if element._1 == page) yield new WptResultPageListener(element._1, contentType, element._2)
    })

    println("Listener List created: \n" + listenerList.map(element => "list element: \n" + "url: " + element.pageUrl + "\n" + "resulturl" + element.wptResultUrl + "\n"))

    val resultsList: ParSeq[WptResultPageListener] = listenerList.par.map(element => {
      val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
      val newElement = new WptResultPageListener(element.pageUrl, element.pageType, element.wptResultUrl)
      println("getting result for page element: " + newElement.pageUrl)
      newElement.testResults = wpt.getResults(newElement.pageUrl,newElement.wptResultUrl)
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

  def confirmAlert(initialResult: PerformanceResultsObject, averages: PageAverageObject, urlFragments: List[String],wptBaseUrl: String, wptApiKey: String, wptLocation: String): PerformanceResultsObject = {
    val webPageTest = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
    val testCount: Int = if (initialResult.timeToFirstByte > 1000) {
      5
    } else {
      3
    }
    println("TTFB for " + initialResult.testUrl + "\n therefore setting test count of: " + testCount)
    val AlertConfirmationTestResult: PerformanceResultsObject = setAlertStatus(webPageTest.testMultipleTimes(initialResult.testUrl, initialResult.typeOfTest, wptLocation, testCount), averages)
    AlertConfirmationTestResult
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


  def retestUrl(initialResult: PerformanceResultsObject, wptBaseUrl: String, wptApiKey: String, wptLocation: String, urlFragments: List[String]): PerformanceResultsObject = {
    val webPageTest = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
    val testCount: Int = if (initialResult.timeToFirstByte > 1000) {
      5
    } else {
      3
    }
    println("TTFB for " + initialResult.testUrl + "\n therefore setting test count of: " + testCount)
    //   val AlertConfirmationTestResult: PerformanceResultsObject = setAlertStatusPageWeight(webPageTest.testMultipleTimes(initialResult.testUrl, initialResult.typeOfTest, wptLocation, testCount), averages)
    webPageTest.testMultipleTimes(initialResult.testUrl, initialResult.typeOfTest, wptLocation, testCount)
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

}

