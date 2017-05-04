package app.apiutils

import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model.{ItemQuery, SearchQuery}
import com.gu.contentapi.client.model.v1._
import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ArticleUrls(key: String) {
  val testApi:String = key
  println("testApi = " + testApi)
  val contentApiClient = new GuardianContentClient(key)

  val until = DateTime.now
  val from = until.minusHours(3)
  val pageSize: Int = 30
// todo get top 20 for each section
// You can do this per section, for example for the uk section:
// http://some.example.url/uk?show-most-viewed=true&page-size=0

  def getUrlsForContentType(contentType: String): List[(Option[ContentFields], Seq[Tag], String)] = {
     contentType match {
      case("Article") => getArticles(1)
      case ("LiveBlog") =>  getMinByMins(1)
      case ("Interactive") => getInteractives(1)
      case ("Video") => getVideoPages(1)
      case ("Audio") => getAudioPages(1)
      case("Front") => getFronts(1)
      case (_) => {
        val emptyList: List[(Option[ContentFields], Seq[Tag], String)] = List()
        emptyList
      }
    }
  }

  def shutDown = {
    println("Closing connection to Content API")
    contentApiClient.shutdown()
  }

  def getArticles(pageNumber: Int): List[(Option[ContentFields], Seq[Tag],String)] = {

    try {
      val searchQuery = new SearchQuery()
        .fromDate(from)
        .toDate(until)
        .showElements("all")
        .showFields("all")
        .showTags("all")
        .page(pageNumber)
        .pageSize(pageSize)
        .orderBy("newest")
        .contentType("article")
      println("Sending query to CAPI: \n" + searchQuery.toString)

      val apiResponse = contentApiClient.getResponse(searchQuery)
      val returnedResponse = Await.result(apiResponse, (20, SECONDS))
      val articleContentAndUrl: List[(Option[ContentFields], Seq[Tag], String)] = for (result <- returnedResponse.results) yield {
        (result.fields, result.tags, result.webUrl)
      }
      println("received " + articleContentAndUrl.length + " pages from this query")
      if (articleContentAndUrl.length < pageSize) {
        articleContentAndUrl
      } else {
        Thread.sleep(2000)
        println("calling page: " + pageNumber + 1)
        articleContentAndUrl ::: getArticles(pageNumber + 1)
      }
    } catch {
      case _: Throwable => {
        println("bad request - page is empty - returning empty list")
        val emptyList: List[(Option[ContentFields], Seq[Tag], String)] = List()
        emptyList
      }
    }
  }

  def getMinByMins(pageNumber: Int): List[(Option[ContentFields], Seq[Tag],String)] = {
  try {
    val searchQuery = new SearchQuery()
      .fromDate(from)
      .toDate(until)
      .showElements("all")
      .showFields("all")
      .showTags("all")
      .page(pageNumber)
      .pageSize(pageSize)
      .orderBy("newest")
      .tag("tone/minutebyminute")
    println("Sending query to CAPI: \n" + searchQuery.toString)

    val apiResponse = contentApiClient.getResponse(searchQuery)
    val returnedResponse = Await.result(apiResponse, (20, SECONDS))
    val liveBlogContentAndUrl: List[(Option[ContentFields], Seq[Tag], String)] = for (result <- returnedResponse.results) yield {
        (result.fields, result.tags, result.webUrl)
      }
    println("received " + liveBlogContentAndUrl.length + " pages from this query")
    if (liveBlogContentAndUrl.length < pageSize) {
      liveBlogContentAndUrl
    } else {
      Thread.sleep(2000)
      liveBlogContentAndUrl ::: getMinByMins(pageNumber + 1)
    }
  } catch {
      case _: Throwable => {
        println("bad request - page is empty - returning empty list")
        val emptyList: List[(Option[ContentFields], Seq[Tag], String)] = List()
        emptyList
      }
    }
  }


  def getInteractives(pageNumber: Int): List[(Option[ContentFields],Seq[Tag], String)] = {
  try {
    val searchQuery = new SearchQuery()
      .fromDate(from)
      .toDate(until)
      .showElements("all")
      .showFields("all")
      .showTags("all")
      .page(pageNumber)
      .pageSize(pageSize)
      .orderBy("newest")
      .contentType("interactive")
    println("Sending query to CAPI: \n" + searchQuery.toString)

    val apiResponse = contentApiClient.getResponse(searchQuery)
    val returnedResponse = Await.result(apiResponse, (20, SECONDS))
    val interactiveContentAndUrl: List[(Option[ContentFields], Seq[Tag], String)] = for (result <- returnedResponse.results) yield {
        (result.fields, result.tags, result.webUrl)
    }
    println("received " + interactiveContentAndUrl.length + " pages from this query")
    if (interactiveContentAndUrl.length < pageSize) {
      interactiveContentAndUrl
    } else {
      Thread.sleep(2000)
      interactiveContentAndUrl ::: getInteractives(pageNumber + 1)
    }
  } catch {
      case _: Throwable => {
        println("bad request - page is empty - returning empty list")
        val emptyList: List[(Option[ContentFields], Seq[Tag], String)] = List()
        emptyList
      }
    }
  }

  def getFronts(pageNumber: Int): List[(Option[ContentFields], Seq[Tag], String)] = {
    val listofFronts: List[String] = List("http://www.theguardian.com/uk"/*,
      "http://www.theguardian.com/us",
      "http://www.theguardian.com/au",
      "http://www.theguardian.com/uk-news",
      "http://www.theguardian.com/world",
      "http://www.theguardian.com/politics",
      "http://www.theguardian.com/uk/sport",
      "http://www.theguardian.com/football",
      "http://www.theguardian.com/uk/commentisfree",
      "http://www.theguardian.com/uk/culture",
      "http://www.theguardian.com/uk/business",
      "http://www.theguardian.com/uk/lifeandstyle",
      "http://www.theguardian.com/fashion",
      "http://www.theguardian.com/uk/environment",
      "http://www.theguardian.com/uk/technology",
      "http://www.theguardian.com/travel"*/)
    val emptyContentFields: Option[ContentFields] = None
    val emptyTags: Seq[Tag] = List()
    val returnList:List[(Option[ContentFields], Seq[Tag], String)] = listofFronts.map(url => (emptyContentFields, emptyTags, url))
    println("CAPI Query Success - Fronts: \n" + returnList.map(element => element._2).mkString)
    returnList
  }


  def getVideoPages(pageNumber: Int): List[(Option[ContentFields], Seq[Tag],String)] = {
  try {
    val searchQuery = new SearchQuery()
      .fromDate(from)
      .toDate(until)
      .showElements("all")
      .showFields("all")
      .showTags("all")
      .page(1)
      .pageSize(20)
      .orderBy("newest")
      .contentType("video")
    println("Sending query to CAPI: \n" + searchQuery.toString)

    val apiResponse = contentApiClient.getResponse(searchQuery)
    val returnedResponse = Await.result(apiResponse, (20, SECONDS))
    val videoContentAndUrl: List[(Option[ContentFields], Seq[Tag], String)] = for (result <- returnedResponse.results) yield {
        (result.fields, result.tags, result.webUrl)
      }
    println("received " + videoContentAndUrl.length + " pages from this query")
    if (videoContentAndUrl.length < pageSize) {
      videoContentAndUrl
    } else {
      Thread.sleep(2000)
      videoContentAndUrl ::: getVideoPages(pageNumber + 1)
    }
  } catch {
      case _: Throwable => {
        println("bad request - page is empty - returning empty list")
        val emptyList: List[(Option[ContentFields], Seq[Tag], String)] = List()
        emptyList
      }
    }
  }

  def getAudioPages(pageNumber: Int): List[(Option[ContentFields], Seq[Tag], String)] = {
    try {
      val liveBlogSearchQuery = new SearchQuery()
        .fromDate(from)
        .toDate(until)
        .showElements("all")
        .showFields("all")
        .showTags("all")
        .page(1)
        .pageSize(20)
        .orderBy("newest")
        .contentType("audio")
      println("Sending query to CAPI: \n" + liveBlogSearchQuery.toString)

      val apiResponse = contentApiClient.getResponse(liveBlogSearchQuery)
      val returnedResponse = Await.result(apiResponse, (20, SECONDS))
      val audioContentAndUrl: List[(Option[ContentFields], Seq[Tag], String)] = for (result <- returnedResponse.results) yield {
          (result.fields, result.tags, result.webUrl)
        }
      println("received " + audioContentAndUrl.length + " pages from this query")
      if (audioContentAndUrl.length < pageSize) {
        audioContentAndUrl
      } else {
        Thread.sleep(2000)
        audioContentAndUrl ::: getAudioPages(pageNumber + 1)
      }
    } catch {
      case _: Throwable => {
        println("bad request - page is empty - returning empty list")
        val emptyList: List[(Option[ContentFields], Seq[Tag], String)] = List()
        emptyList
      }
    }
  }


  /*def getContentTypeFronts: List[String] = {
    println("Creating CAPI query")
    val until = DateTime.now
    val from = until.minusHours(24)

    val FrontsSearchQuery = new SearchQuery()
      .fromDate(from)
      .toDate(until)
      .showBlocks("all")
      .showElements("all")
      .showFields("all")
      .showTags("all")
      .page(1)
      .pageSize(20)
      .orderBy("newest")
      .contentType("front")
    println("Sending query to CAPI: \n" + FrontsSearchQuery.toString)

    val apiResponse = contentApiClient.getResponse(FrontsSearchQuery)
    val returnedResponse = Await.result(apiResponse, (20, SECONDS))
    println("CAPI has returned response")
    val liveBlogUrlString: List[String] = for (result <- returnedResponse.results) yield {
      println("liveBlog result: " + result.webUrl)
      result.webUrl }
    liveBlogUrlString
  }
*/

  def getSinglePage(urlString: String): (Option[ContentFields], Seq[Tag], String) = {
    val domainName= "www.theguardian.com"
    val urlId = urlString.substring(urlString.indexOf(domainName)+domainName.length+1,urlString.length)
    try {
      val searchQuery = new ItemQuery(urlId)
        .showFields("all")
        .showElements("all")
      println("Sending query to CAPI: \n" + searchQuery.toString)

      val apiResponse = contentApiClient.getResponse(searchQuery)
      val returnedResponse = Await.result(apiResponse, (20, SECONDS))
      val resultAndUrl = (returnedResponse.content.get.fields, returnedResponse.content.get.tags, returnedResponse.content.get.webUrl)
      /*val resultAndUrl: List[(Option[ContentFields], String)] = for (result <- returnedResponse.results if result.webUrl.contains(urlString)) yield {
        (result.fields, result.webUrl)
      }*/
      if(resultAndUrl._1.isEmpty){
        println("result and url is coming back as Empty")
        val emptyTags: Seq[Tag] = Seq()
        val emptyContentFields: (Option[ContentFields], Seq[Tag], String) = (Option(makeContentStub(Option(urlString), None, Option(false))), emptyTags, urlString)
        emptyContentFields
      } else {
        println("CAPI headline result: \n" + resultAndUrl._1.get.headline)
        resultAndUrl
      }
    } catch {
      case _: Throwable => {
        println("bad request - page is empty - returning empty content fields")
        val emptyTags: Seq[Tag] = Seq()
        val emptyContentFields: (Option[ContentFields], Seq[Tag], String) = (Option(makeContentStub(Option(urlString), None, Option(false))), emptyTags, urlString)
        emptyContentFields
      }
    }
  }

  def makeContentStub(passedHeadline: Option[String], passedLastModified: Option[CapiDateTime], passedLiveBloggingNow: Option[Boolean]): ContentFields = {
    val contentStub = new ContentFields {override def newspaperEditionDate: Option[CapiDateTime] = None

      override def internalStoryPackageCode: Option[Int] = None

      override def displayHint: Option[String] = None

      override def legallySensitive: Option[Boolean] = None

      override def creationDate: Option[CapiDateTime] = None

      override def shouldHideAdverts: Option[Boolean] = None

      override def wordcount: Option[Int] = None

      override def thumbnail: Option[String] = None

      override def liveBloggingNow: Option[Boolean] = passedLiveBloggingNow

      override def showInRelatedContent: Option[Boolean] = None

      override def internalComposerCode: Option[String] = None

      override def lastModified: Option[CapiDateTime] = passedLastModified

      override def byline: Option[String] = None

      override def isInappropriateForSponsorship: Option[Boolean] = None

      override def commentable: Option[Boolean] = None

      override def trailText: Option[String] = None

      override def internalPageCode: Option[Int] = None

      override def main: Option[String] = None

      override def body: Option[String] = None

      override def productionOffice: Option[Office] = None

      override def newspaperPageNumber: Option[Int] = None

      override def shortUrl: Option[String] = None

      override def publication: Option[String] = None

      override def secureThumbnail: Option[String] = None

      override def contributorBio: Option[String] = None

      override def firstPublicationDate: Option[CapiDateTime] = None

      override def isPremoderated: Option[Boolean] = None

      override def membershipAccess: Option[MembershipTier] = None

      override def scheduledPublicationDate: Option[CapiDateTime] = None

      override def starRating: Option[Int] = None

      override def hasStoryPackage: Option[Boolean] = None

      override def headline: Option[String] = passedHeadline

      override def commentCloseDate: Option[CapiDateTime] = None

      override def internalOctopusCode: Option[String] = None

      override def standfirst: Option[String] = None
    }
    contentStub
  }

}

