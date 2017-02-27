import app.api.S3Operations
import app.apiutils.WebPageTest
import org.joda.time.DateTime
import org.scalatest._

/**
 * Created by mmcnamara on 27/02/17.
 */

abstract class WPTUnitSpec extends FlatSpec with Matchers with OptionValues with Inside with Inspectors

class WPTests extends UnitSpec with Matchers {

  val amazonDomain = "https://s3-eu-west-1.amazonaws.com"
  val s3BucketName = "capi-wpt-querybot"
  val folderName = "branch-comparison-tester/"
  val configFileName = "config.conf"
  val emailFileName = "addresses.conf"

  println("defining new S3 Client (this is done regardless but only used if 'iamTestingLocally' flag is set to false)")
  val s3Interface = new S3Operations(s3BucketName, folderName + configFileName)
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


  "A page" should "retrieve a result successfully" in {
    val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
    val result = wpt.getMultipleResults("http://ec2-34-250-48-44.eu-west-1.compute.amazonaws.com:9000/books/2014/may/21/guardian-journalists-jonathan-freedland-ghaith-abdul-ahad-win-orwell-prize-journalism", "http://wpt.gu-web.net/xmlResult/170224_SA_K/")
    println("result is: \n" + result.toCSVString)
    result.speedIndex > 0 should be (true)
  }

}
