package app.api

import app.apiutils.PerformanceResultsObject
import org.joda.time.DateTime

/**
 * Created by mmcnamara on 15/04/16.
 */
class PageWeightDashboardCombined (resultsList: List[PerformanceResultsObject]) {

  //HTML Page elements
  //Page Header
  val HTML_PAGE_HEAD: String = "<!DOCTYPE html><html lang=\"en\">" + "\n" +
    "<head> <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>" + "\n" +
    "<title>Daily REPORT - [Pageweight Dashboard - Combined]</title>" + "\n" +
    "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css\"/>" + "\n" +
    "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js\"></script>" + "\n" +
    "<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js\"></script>" + "\n" +
    "<link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css\" rel=\"stylesheet\"/>" + "\n" +
    "<link rel=\"stylesheet\" href=\"/capi-wpt-querybot/assets/css/style.css\"/>"+ "\n" +
    "<script src=\"/capi-wpt-querybot/assets/js/script.js\"></script>" + "\n" +
    "</head>"

  //Page Container
  val HTML_PAGE_CONTAINER: String = "<body>" + "\n" +
    "<div id=\"container\">" + "\n" +
    "<div id=\"head\">" + "\n" +
    "<h1>Current performance of today's Pages</h1>" + "\n" +
    "<p>Job started at: " + DateTime.now + "</p>" + "\n" +
    "</div>"

  //Page Content
  val HTML_PAGE_CONTENT: String = "<div id=\"content\">" + "\n" +
    "<h2>Combined Mobile and Desktop view</h2>" + "\n"

  //Page Tables
  val HTML_REPORT_TABLE_HEADERS: String = "<table id=\"report\">"+ "\n" +
    "<thead>" + "\n" +
    "<tr> <th>Time Last Tested</th>" + "<th>Test VisualsElementType</th>" + "<th>Headline</th>" + "<th>VisualsElementType of Page</th>" + "<th>Time till page looks loaded</th>" + "<th>Page weight (MB)</th>" + "<th>Click for more details</th>" + "</tr>"+ "\n" +
    "</thead>" +"\n" +
    "<tbody>"

  val HTML_PAGE_ELEMENT_TABLE_HEADERS: String = "<tr>" + "\n" +
    "<td colspan=\"12\">" + "<table id=\"data\" class=\"data\">" + "\n" +
    "<caption>List of 5 heaviest elements on page - Recommend reviewing these items </caption>" + "\n" +
    "<thead>" + "\n" +
    "<tr>" + "<th>Resource</th>" + "<th>Content VisualsElementType</th>" + "<th>Bytes Transferred</th>" + "</tr>" + "\n" +
    "</thead>" +"\n" +
    "<tbody>"

  val HTML_TABLE_END: String = "</tbody>" + "\n" + "</table>"+ "\n"

  val HTML_PAGE_ELEMENT_TABLE_END: String = "</tbody>" + "\n" + "</table>"+ "\n" + "</td>" + "\n" + "</tr>" + "\n"

  //Page Footer
  val HTML_FOOTER: String = "</div>" + "\n" +
    "<div id=\"footer\">" + "<p>Job completed at: [DATA]</p>" + "</div>" + "\n" +
    "</div>" + "\n" +
    "</body>" + "\n" +
    "</html>"


  //HTML_PAGE
  val HTML_PAGE: String = HTML_PAGE_HEAD + HTML_PAGE_CONTAINER + HTML_PAGE_CONTENT + generateHTMLTable(resultsList) + HTML_FOOTER


  //page generation methods
  def generateHTMLTable(resultsList: List[PerformanceResultsObject]): String = {
    HTML_REPORT_TABLE_HEADERS + "\n" + generateHTMLDataRows(resultsList) + "\n" + HTML_TABLE_END
  }

  def generateHTMLDataRows(resultsList: List[PerformanceResultsObject]): String = {
    (for (result <- resultsList) yield {
      "<tr class=\"pageclass " + getAlertClass(result) + "\">" + result.toHTMLPageWeightTableCells() + "<td><div class=\"arrow\"></div></td></tr>" + "\n" +
        generatePageElementTable(result)
    }).mkString

  }

  def generatePageElementTable(resultsObject: PerformanceResultsObject): String = {
      HTML_PAGE_ELEMENT_TABLE_HEADERS + "\n"  + getHTMLForPageElements(resultsObject) + HTML_PAGE_ELEMENT_TABLE_END
  }

  def getHTMLForPageElements(resultsObject: PerformanceResultsObject): String = {
    if (resultsObject.getPageType.contains("Interactive")){
      resultsObject.returnHTMLFullElementList()
    } else {
      resultsObject.returnHTMLEditorialElementList()
    }
  }

  def getAlertClass(resultsObject: PerformanceResultsObject): String = {
    if (resultsObject.alertStatusPageWeight) {
      "alert"
    } else {
        "default"
      }
  }

  // Access Methods

  override def toString(): String = {
    HTML_PAGE
  }


  //  def initialisePageForArticle: String = {
  //    hTMLPageHeader + hTMLTitleArticle + hTMLJobStarted
  //  }
  //
  //  def initialisePageForLiveblog: String = {
  //    hTMLPageHeader + hTMLTitleLiveblog + hTMLJobStarted
  //  }
  //
  //  def initialisePageForInteractive: String = {
  //    hTMLPageHeader + hTMLTitleInteractive + hTMLJobStarted
  //  }
  //
  //  def initialisePageForFronts: String = {
  //    hTMLPageHeader + hTMLTitleFronts + hTMLJobStarted
  //  }
  //
  //  def initialiseTable: String = {
  //    hTMLSimpleTableHeaders
  //  }
  //
  //  def interactiveTable: String = {
  //    hTMLInteractiveTableHeaders
  //  }


}

