import app.api.S3Operations
import app.apiutils.{PageElementFromHTMLTableRow, WebPageTest, PageElement, LocalFileOperations}
import org.joda.time.DateTime
import org.scalatest._

import scala.io.Source

/**
 * Created by mmcnamara on 01/03/16.
 */
  abstract class UnitSpec extends FlatSpec with Matchers with
  OptionValues with Inside with Inspectors

  class PageElementTests extends UnitSpec with Matchers {
    //Create new S3 Client
    val amazonDomain = "https://s3-eu-west-1.amazonaws.com"
    val s3BucketName = "capi-wpt-querybot"
    val configFileName = "config.conf"
    val emailFileName = "addresses.conf"

    println("defining new S3 Client (this is done regardless but only used if 'iamTestingLocally' flag is set to false)")
    val s3Interface = new S3Operations(s3BucketName, configFileName)
    var configArray: Array[String] = Array("", "", "", "", "", "")
    var urlFragments: List[String] = List()
    val resultsFromPreviousTests = "resultsFromPreviousTests.csv"
    val resultsFromPreviousTestsInput = "resultsFromPreviousTestsTest.csv"
    val resultsFromPreviousTestsOutput = "resultsFromPreviousTestsOutput.csv"

    println(DateTime.now + " retrieving config from S3 bucket: " + s3BucketName)
    val returnTuple = s3Interface.getConfig
    configArray = Array(returnTuple._1,returnTuple._2,returnTuple._3,returnTuple._4,returnTuple._5,returnTuple._6,returnTuple._7)
    urlFragments = returnTuple._8

    val contentApiKey: String = configArray(0)
    val wptBaseUrl: String = configArray(1)
    val wptApiKey: String = configArray(2)
    val wptLocation: String = configArray(3)
/*
    "A pageElementList" should "contain page elements" in {
      val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)

      var htmlString: String = ""
      for (line <- Source.fromFile("webpagetestresultdetails.html").getLines()) {
        htmlString = htmlString + line
      }

      val pageElementList: List[PageElementFromHTMLTableRow] = wpt.generatePageElementList(wpt.trimToHTMLTable(htmlString))
      pageElementList.head.printElement()
      println("testing list is not empty")
      assert(pageElementList.nonEmpty)
    }*/
/*
    "A non-empty pageElementList" should "contain page elements that have non-empty values" in {
      val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)

      var htmlString: String = ""
      for (line <- Source.fromFile("webpagetestresultdetails.html").getLines()) {
        htmlString = htmlString + line
      }

      val pageElementList: List[PageElementFromHTMLTableRow] = wpt.generatePageElementList(wpt.trimToHTMLTable(htmlString))

      val checkall: List[Boolean] = pageElementList.map(element => {
        val nonEmptyElement: Boolean = (element.timeToFirstByte>0) ||
        (element.bytesDownloaded>0) ||
        (element.contentDownload>0) ||
        element.contentType.nonEmpty ||
        (element.dnsLookUp>0) ||
        (element.errorStatusCode>0) ||
        (element.initialConnection>0) ||
        element.iP.nonEmpty ||
        (element.requestStart>0) ||
        element.resource.nonEmpty ||
        (element.sslNegotiation>0)
        if (!nonEmptyElement) {println("This element is completely empty: \n" + element.returnString())}
        nonEmptyElement
      })
      assert(!checkall.contains(false))
    }*/

 /*   "A sorted pageElementList" should "be sorted in order of largest bytesDownloaded to smallest" in {
      val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)

      var htmlString: String = ""
      for (line <- Source.fromFile("webpagetestresultdetails.html").getLines()) {
        htmlString = htmlString + line
      }

      val pageElementList: List[PageElementFromHTMLTableRow] = wpt.generatePageElementList(wpt.trimToHTMLTable(htmlString))
      var sortedPageElementList: List[PageElementFromHTMLTableRow] = wpt.sortPageElementList(pageElementList)
      var isordered: Boolean = true
      var counter: Int = 1
      while (sortedPageElementList.tail.nonEmpty && isordered) {
        val current: Int = sortedPageElementList.head.bytesDownloaded
        val next: Int = sortedPageElementList.tail.head.bytesDownloaded
        isordered = (current >= next)
        if(!isordered){println("Element number: " + counter + "with size: " + pageElementList.head.bytesDownloaded + "should be greater than " + pageElementList.tail.head.bytesDownloaded)}
        counter += 1
        sortedPageElementList = sortedPageElementList.tail
      }
      if (isordered) {println("Tested list of " + counter + " elements. All are in order")}
      assert(isordered)
    }*/

    "File loaded from S3" should "have elements in its Full Element List populated" in {
      val resultList = s3Interface.getResultsFileFromS3(resultsFromPreviousTestsInput)
      s3Interface.writeFileToS3(resultsFromPreviousTestsOutput,resultList.map(_.toCSVString()).mkString)
      val resultList2 = s3Interface.getResultsFileFromS3(resultsFromPreviousTestsOutput)
      val resultListLength = resultList.length
      val errorFreeResultListLength = resultList.length
      var resultList2Length = resultList2.length
      var resultListHead = resultList.head
      var resultList2Head = resultList2.head
      println(resultList.head.toCSVString())
      println(resultList.head.resultStatus)
      assert(resultListLength == errorFreeResultListLength && resultListLength == resultList2Length && resultListHead.testUrl == resultList2Head.testUrl && resultListHead.resultStatus == resultList2Head.resultStatus)
    }

  /*  "A list of elements from results" should "not contain any commas" in {
      val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
      val result = wpt.getResults("testUrl", "http://wpt.gu-web.net/xmlResult/160708_D4_ZK/")
      assert(!result.editorialElementList.map(element => element.resource).contains(","))

    }*/
 
  }



