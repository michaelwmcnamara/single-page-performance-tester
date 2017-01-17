package app.apiutils

import java.io.FileWriter

import com.amazonaws.services.s3.model.GetObjectRequest
import com.gu.contentapi.client.model.v1.CapiDateTime
import org.joda.time.DateTime

import scala.io.Source


/**
 * Created by mmcnamara on 10/02/16.
 */
class LocalFileOperations {

  def readInConfig(fileName: String):Array[String] = {
    var contentApiKey: String = ""
    var wptBaseUrl: String = ""
    var wptApiKey: String = ""
    var wptLocation: String = ""
    var emailUsername: String = ""
    var emailPassword: String = ""
    var visualsApiUrl: String = ""

    println(DateTime.now + " retrieving local config file: " + fileName)
    for (line <- Source.fromFile(fileName).getLines()) {
      if (line.contains("content.api.key")) {
        println("capi key found")
        contentApiKey = line.takeRight((line.length - line.indexOf("=")) - 1)
      }
      if (line.contains("wpt.api.baseUrl")) {
        println("wpt url found")
        wptBaseUrl = line.takeRight((line.length - line.indexOf("=")) - 1)
      }
      if (line.contains("wpt.api.key")) {
        println("wpt api key found")
        wptApiKey = line.takeRight((line.length - line.indexOf("=")) - 1)
      }
      if (line.contains("wpt.location")) {
        println("wpt location found")
        wptLocation = line.takeRight((line.length - line.indexOf("=")) - 1)
      }
      if (line.contains("email.username")) {
        println("email username found")
        emailUsername = line.takeRight((line.length - line.indexOf("=")) - 1)
      }
      if (line.contains("email.password")) {
        println("email password found")
        emailPassword = line.takeRight((line.length - line.indexOf("=")) - 1)
      }
      if (line.contains("visuals.page.list")) {
        println("visuals api url found")
        visualsApiUrl = line.takeRight((line.length - line.indexOf("=")) - 1)
      }
    }
    Array(contentApiKey, wptBaseUrl, wptApiKey, wptLocation, emailUsername, emailPassword, visualsApiUrl)
  }


  def writeLocalResultFile(outputFileName: String, results: String): Int = {
    val output: FileWriter = new FileWriter(outputFileName)
    println(DateTime.now + " Writing " + results.length + " rows to the following local file: " + outputFileName + "\n")
    output.write(results)
    output.close()
    println(DateTime.now + " Writing to file: " + outputFileName + " complete. \n")
    0
  }

  def getResultsFile(fileName:String): List[PerformanceResultsObject] = {
    // todo - update to include new fields
    val myData = Source.fromFile(fileName).getLines()
      val resultsIterator = for (line <- myData) yield {
        val data: Array[String] = line.split(",")
        var result = new PerformanceResultsObject(data(1),
          data(2),
          data(3),
          data(4).toInt,
          data(5).toInt,
          data(6).toInt,
          data(7).toInt,
          data(8).toInt,
          data(9).toInt,
          data(10).toInt,
          data(11).toInt,
          data(12).toInt,
          data(13),
          data(14).toBoolean)
        result.setTimeOfTest(data(0))
        result
      }
      resultsIterator.toList
    }

}
