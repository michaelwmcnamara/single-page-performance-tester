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
    val folderName = "branch-comparison-tester/"
    val configFileName = "config.conf"


    val pageResults = exportPath + "performanceComparisonSummary.txt"
    val pageList = "performanceTestResults.csv"

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
    val s3Interface = new S3Operations(s3BucketName, folderName + configFileName)
    var configArray: Array[String] = Array("", "", "", "", "", "", "", "")
    var urlFragments: List[String] = List()

    //Get config settings
    println("Extracting configuration values")
    if (!iamTestingLocally) {
      println(DateTime.now + " retrieving config from S3 bucket: " + s3BucketName)
      val returnTuple = s3Interface.getConfig
      configArray = Array(returnTuple._1, returnTuple._2, returnTuple._3, returnTuple._4, returnTuple._5, returnTuple._6, returnTuple._7, returnTuple._9)
      urlFragments = returnTuple._8
      
    }
    else {
      println(DateTime.now + " retrieving local config file: " + configFileName)
      val configReader = new LocalFileOperations
      configArray = configReader.readInConfig(folderName + configFileName)
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
    val comparisonBaseUrl: String = configArray(7)

    val previousTestResults = s3Interface.getResultsFileFromS3(folderName + pageList)

//    val urlsToSend: List[String] = List(suppliedUrl, makeComparisonUrl(suppliedUrl, comparisonBaseUrl))
    val urlsToSend: List[String] = List(suppliedUrl)
    val resultUrlList: List[(String, String)] = getResultPages(urlsToSend, urlFragments, wptBaseUrl, wptApiKey, wptLocation)
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

    val errorFreeCombinedResults = for (result <- combinedResultsList if result.speedIndex > 0) yield result
    val errorFreeCombinedListLength = errorFreeCombinedResults.length

    val ComparisonSummary: String = {if (errorFreeCombinedListLength > 0) {
      val branchResult = errorFreeCombinedResults.head
      // take 10 most recent results to use for average
      val resultsForAverage = previousTestResults.splitAt(previousTestResults.length - 11)._2
      generateComparisonSummary(resultsForAverage, branchResult)
      } else {
       "Error: No performance results returned. Please check local webpagetest instance is working."
      }
    }

    //write combined results to file

    println(DateTime.now + " Writing results to " + pageResults )
    val allTestResults = errorFreeCombinedResults ::: previousTestResults.take(5999)
    val outputWriter = new LocalFileOperations
    val writeSuccessSummary: Int = outputWriter.writeLocalResultFile(pageResults, ComparisonSummary)
    if (writeSuccessSummary != 0) {
        println("problem writing local outputfile")
        System exit 1
      }
      if(!iamTestingLocally){
       s3Interface.writeFileToS3(folderName + pageList, allTestResults.map(_.toCSVString()).mkString)
      }else {
        val writeSuccessList: Int = outputWriter.writeLocalResultFile(exportPath + pageList, allTestResults.map(_.toCSVString()).mkString)
        if (writeSuccessList != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
      }
  }

  def generateComparisonSummary(previousResults: List[PerformanceResultsObject], latestResult: PerformanceResultsObject): String = {
    val TitleString = "Comparison of performance between your branch and prod: \n"
    val averageResult = generateAverageValues(previousResults)
    if (previousResults.length >= 10 && averageResult.nonEmpty && latestResult.speedIndex > 0) {
      val StartRenderString = compareValues("Start-Render", latestResult.startRenderInMs, averageResult.get.startRenderInMs)
      val VisuallyCompleteString = compareValues("Visually-Complete", latestResult.visualComplete, averageResult.get.visualComplete)
      val SpeedIndexString = compareValues("SpeedIndex", latestResult.speedIndex, averageResult.get.speedIndex)
      val PageWeightString = compareValues("PageWeight", latestResult.bytesInFullyLoaded, averageResult.get.bytesInFullyLoaded)
      TitleString + StartRenderString + VisuallyCompleteString + SpeedIndexString + PageWeightString
    } else {
      if (latestResult.speedIndex > 0) {
        "I don't yet have enough measurements to generate a decent average for comparison. PLease try again later"
      } else {
        "We were unable to measure your pages performance. Please check that local webpagetest instance is working."
      }

    }
  }

  def generateAverageValues(results: List[PerformanceResultsObject]): Option[PerformanceResultsObject] = {
    val numberOfResults = results.length
    if(numberOfResults > 0) {
      val urlName: String = "\"Average of \" + numberOfResults + \" results.\""
      val testType = "Not Appliable"
      val testSummaryPage = "Not Appliable"
      val avgTimeToFirstByte = (results.map(_.timeToFirstByte).sum.toDouble / numberOfResults.toDouble).toInt
      val avgFirstPaint: Int = (results.map(_.timeFirstPaintInMs).sum.toDouble / numberOfResults.toDouble).toInt
      val avgStartRender: Int =  (results.map(_.startRenderInMs).sum.toDouble / numberOfResults.toDouble).toInt
      val avgDocTime: Int = (results.map(_.timeDocCompleteInMs).sum.toDouble / numberOfResults.toDouble).toInt
      val avgBytesInDoc: Int = (results.map(_.bytesInDocComplete).sum.toDouble / numberOfResults.toDouble).toInt
      val avgFullyLoadedTime: Int = (results.map(_.timeFullyLoadedInMs).sum.toDouble / numberOfResults.toDouble).toInt
      val avgTotalBytesIn: Int = (results.map(_.bytesInFullyLoaded).sum.toDouble / numberOfResults.toDouble).toInt
      val avgSpeedIndex: Int = (results.map(_.speedIndex).sum.toDouble / numberOfResults.toDouble).toInt
      val avgVisualComplete: Int = (results.map(_.visualComplete).sum.toDouble / numberOfResults.toDouble).toInt
      val status: String = "Average Values"

      Some(new PerformanceResultsObject(urlName, testType, testSummaryPage, avgTimeToFirstByte, avgStartRender, avgFirstPaint, avgDocTime, avgBytesInDoc, avgFullyLoadedTime, avgTotalBytesIn, avgSpeedIndex, avgVisualComplete, status, false))
    } else {
      None
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
      println("pagetype set\n setting FirstPublished")
      println("all variables set for element")
      newElement
    })
    println("Generating list of results objects from list of listener objects")
    resultsList.map(element => element.testResults).toList
  }



  def jodaDateTimetoCapiDateTime(time: DateTime): CapiDateTime = {
    new CapiDateTime {
      override def dateTime: Long = time.getMillis
    }
  }

  def makeComparisonUrl(url: String, comparisonBase: String): String = {
    if(url.take(4).contains("http")) {
      val contentPath: List[String] = url.split("/").toList.drop(3).map(fragment => "/" + fragment)
      (List(comparisonBase) ::: contentPath).mkString
    } else {
      val contentPath: List[String] = url.split("/").toList.drop(1).map(fragment => "/" + fragment)
      (List(comparisonBase) ::: contentPath).mkString
    }
  }

  def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) / s }
}


