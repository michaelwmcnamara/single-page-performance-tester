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
          data(7),
          data(8),
          data(9).toInt,
          data(10).toInt,
          data(11).toInt,
          data(12).toInt,
          data(13).toInt,
          data(14).toInt,
          data(15).toInt,
          data(16),
          data(17).toBoolean,
          data(18).toBoolean,
          data(19).toBoolean)
        //todo - get element list
        val elementArray = data.drop(20)
//        if(elementArray.nonEmpty) {
//          println("\n\n\n Element Array \n" + elementArray.map(element => element.toString + "\n"))
//          result.populateEditorialElementList(getElementListFromArray(elementArray))
//        }
        result.setHeadline(Option(data(2)))
        result.setPageType(data(3))
        val firstPublishedTime: Option[CapiDateTime] = result.stringtoCAPITime(data(4))
        result.setPageLastUpdated(firstPublishedTime)
        val lastUpdateTime: Option[CapiDateTime] = result.stringtoCAPITime(data(5))
        result.setPageLastUpdated(lastUpdateTime)
        result.setLiveBloggingNow(data(6))
        result
      }
      resultsIterator.toList
    }

  def getElementListFromArray(elementArray: Array[String]): List[PageElementFromHTMLTableRow] = {
    //println("\n\n\n\n  **************** elementArray: **************** \n"+ elementArray.map(element => element.toString + "\n").mkString + "\n\n\n\n\n\n" )
    if(elementArray.nonEmpty){
      val length = elementArray.length
      //println("element Array length = " + elementArray.length)
      var index = 0
      var elementList: List[PageElementFromHTMLTableRow] = List()
      while(index < length-1){
/*        println("index = " + index)
        println("resource = " + elementArray(index))
        println("content type = " + elementArray(index + 1))
        println("request start = " + elementArray(index + 2))
        println("dns link up = " + elementArray(index + 3))
        println("init connection = " + elementArray(index + 4))
        println("ssl negotiation = " + elementArray(index + 5))
        println("time to first byte = " + elementArray(index + 6))
        println("content download = " + elementArray(index + 7))
        println("bytes downloaded = " + elementArray(index + 8))
        println("status code = " + elementArray(index + 9))
        println("ipaddress = " + elementArray(index + 10))*/
        val newElement: PageElementFromHTMLTableRow = new PageElementFromParameters(elementArray(index),
          elementArray(index+1),
          elementArray(index+2).toInt,
          elementArray(index+3).toInt,
          elementArray(index+4).toInt,
          elementArray(index+5).toInt,
          elementArray(index+6).toInt,
          elementArray(index+7).toInt,
          elementArray(index+8).toInt,
          elementArray(index+9).toInt,
          elementArray(index+10)).convertToPageElementFromHTMLTableRow()
        elementList = elementList ::: List(newElement)
        index = index + 11
      }
      //println("\n\nElementList Populated. \nLength of list: " + elementList.length)
      //println("Element List Contents: \n" + elementList.map(element => element.toCSVString() + "\n"))
      elementList
    }else{
      val emptyList: List[PageElementFromHTMLTableRow] = List()
      emptyList
    }
  }


}
