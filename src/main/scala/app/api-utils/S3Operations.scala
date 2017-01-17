package app.api

import java.io._

import app.apiutils._
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model._
import com.gu.contentapi.client.model.v1.CapiDateTime
import com.typesafe.config.{ConfigFactory, Config}
import scala.collection.JavaConversions._
import org.joda.time.DateTime

import scala.io.Source


/**
 * Created by mmcnamara on 09/02/16.
 */
class S3Operations(s3BucketName: String, configFile: String) {
  val s3Client: AmazonS3Client = new AmazonS3Client()
  val bucket: String = s3BucketName
  val configFileName = configFile


  def doesFileExist(fileKeyName: String): Boolean = {
    try {
      s3Client.getObjectMetadata(bucket, fileKeyName); true
    } catch {
      case ex: Exception => println("File: " + fileKeyName + " was not present \n"); false
    }
  }

  def getConfig: (String, String, String, String, String, String, String, List[String], String) = {
    println(DateTime.now + " retrieving config from S3 bucket: " + bucket)

    println("Obtaining configfile: " + configFileName + " from S3")
    val s3Object = s3Client.getObject(new GetObjectRequest(bucket, configFileName))
    val objectData = s3Object.getObjectContent

    println("Converting to string")
    val configString = scala.io.Source.fromInputStream(objectData).mkString

    println("calling parseString on ConfigFactory object")
    val conf = ConfigFactory.parseString(configString)

    println("returning config object")
    val contentApiKey: String = conf.getString("content.api.key")
    val wptBaseUrl: String = conf.getString("wpt.api.baseUrl")
    val wptApiKey: String = conf.getString("wpt.api.key")
    val wptLocation = conf.getString("wpt.location")
    val emailUsername = conf.getString("email.username")
    val emailPassword = conf.getString("email.password")
    val visualsFeedUrl = conf.getString("visuals.page.list")
    val pageFragments: List[String] = conf.getStringList("page.fragments").toList
    val comparisonBaseUrl = conf.getString("comparison.base.url")
    if ((contentApiKey.length > 0) && (wptBaseUrl.length > 0) && (wptApiKey.length > 0) && (wptLocation.length > 0) && (emailUsername.length > 0) && (emailPassword.length > 0) && (visualsFeedUrl.length > 0)){
      println(DateTime.now + " Config retrieval successful. \n You are using the following webpagetest instance: " + wptBaseUrl)
      (contentApiKey, wptBaseUrl, wptApiKey, wptLocation, emailUsername, emailPassword, visualsFeedUrl, pageFragments, comparisonBaseUrl)
    }
    else {
      println(DateTime.now + " ERROR: Problem retrieving config file - one or more parameters not retrieved")
      s3Client.shutdown()
      ("", "", "", "", "", "", "", List(), "")
    }

  }

  def getUrls(fileName: String): List[String] = {
    println(DateTime.now + " retrieving url file from S3 bucket: " + bucket)

    println("Obtaining list of urls: " + fileName + " from S3")
    val s3Object = s3Client.getObject(new GetObjectRequest(bucket, fileName))
    val objectData = s3Object.getObjectContent

    println("Converting to string")
    val configString = scala.io.Source.fromInputStream(objectData).mkString

    println("calling parseString on ConfigFactory object")
    val conf = ConfigFactory.parseString(configString)

    println("returning config object")
    val interactives = conf.getStringList("sample.large.interactives").toList
    if (interactives.nonEmpty){
      println(DateTime.now + " Config retrieval successful. \n You have retrieved the following users\n" + interactives)
      interactives
    }
    else {
      println(DateTime.now + " ERROR: Problem retrieving config file - one or more parameters not retrieved")
      s3Client.shutdown()
      val emptyList: List[String] = List()
      emptyList
    }

  }

  def getResultsFileFromS3(fileName:String): List[PerformanceResultsObject] = {
    // todo - update to include new fields
    if (doesFileExist(fileName)) {
      val s3Response = s3Client.getObject(new GetObjectRequest(s3BucketName, fileName))
      val objectData = s3Response.getObjectContent
      val myData = scala.io.Source.fromInputStream(objectData).getLines()
      val resultsIterator = for (line <- myData) yield {
        val data: Array[String] = line.split(",")
        val result = new PerformanceResultsObject(data(1),
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
    } else {
      val emptyList: List[PerformanceResultsObject] = List()
      emptyList
    }
  }


  def getCSVFileFromS3(fileName: String): List[String] = {
    if (doesFileExist(fileName)) {
      val s3Response = s3Client.getObject(new GetObjectRequest(s3BucketName, fileName))
      val objectData = s3Response.getObjectContent
      val myData = scala.io.Source.fromInputStream(objectData).getLines()
      val resultsIterator = for (line <- myData) yield {
      line
      }
      resultsIterator.toList
    }else{
      val emptyList: List[String] = List()
      emptyList
    }
  }

  def getVisualsFileFromS3(fileName:String): String = {
    if (doesFileExist(fileName)) {
      val s3Response = s3Client.getObject(new GetObjectRequest(s3BucketName, fileName))
      val objectData = s3Response.getObjectContent
      val myData = scala.io.Source.fromInputStream(objectData).getLines().toString()
      myData
    } else {
        ""
      }
    }

  def writeFileToS3(fileName:String, outputString: String): Unit ={
    println(DateTime.now + " Writing the following file to S3:\n" + fileName + "\n")
    s3Client.putObject(new PutObjectRequest(s3BucketName, fileName, createOutputFile(fileName, outputString)))
    val acl: AccessControlList = s3Client.getObjectAcl(bucket, fileName)
    acl.grantPermission(GroupGrantee.AllUsers, Permission.Read)
    s3Client.setObjectAcl(bucket, fileName, acl)

  }

  def createOutputFile(fileName: String, content: String): File = {
    println("creating output file")
    val file: File = File.createTempFile(fileName.takeWhile(_ != '.'), fileName.dropWhile(_ != '.'))
    file.deleteOnExit()
    val writer: Writer = new OutputStreamWriter(new FileOutputStream(file))
    writer.write(content)
    writer.close()
    println("returning File object")
    file
  }



  def closeS3Client(): Unit = s3Client.shutdown()

}
