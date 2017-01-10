package app.api


import app.apiutils.PerformanceResultsObject
import org.joda.time.DateTime

/**
 * Created by mmcnamara on 15/04/16.
 */
class PageSpeedDashboardTabbed(combinedResultsList: List[PerformanceResultsObject], desktopResultsList: List[PerformanceResultsObject], mobileResultsList: List[PerformanceResultsObject]) {

  //HTML Page elements
  //Page Header
  val HTML_PAGE_HEAD: String = "<!DOCTYPE html><html lang=\"en\">" + "\n" +
    "<head> <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>" + "\n" +
    "<title>Dotcom Page Speed Dashboard - [Dotcom Page Speed Dashboard]</title>" + "\n" +
    "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css\"/>" + "\n" +
    "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js\"></script>" + "\n" +
    "<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js\"></script>" + "\n" +
    "<link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css\" rel=\"stylesheet\"/>" + "\n" +
    "<link rel=\"stylesheet\" href=\"/capi-wpt-querybot/assets/css/tabs.css\"/>"+ "\n" +
    "<link rel=\"stylesheet\" href=\"/capi-wpt-querybot/assets/css/style.css\"/>"+ "\n" +
    "<script src=\"/capi-wpt-querybot/assets/js/script.js\"></script>" + "\n" +
    "<script src=\"/capi-wpt-querybot/assets/js/tabs.js\"></script>" + "\n" +
    "</head>"

  //Page Container
  val HTML_PAGE_CONTAINER: String = "<body>" + "\n" +
    "<div id=\"container\">" + "\n" +
    "<div id=\"head\">" + "\n" +
    "<h1>Current performance of today's Pages</h1>" + "\n" +
    "<p>Job started at: " + DateTime.now + "</p>" + "\n" +
    "</div>" + "\n"

  val HTML_PAGE_TABS_LIST: String = "<div class=\"tabs\">" + "\n" +
    "<ul class=\"tab-links\">" + "\n" +
    "<li class=\"active\">" + "<a href=\"#mobile\">Mobile view</a>" + "</li>" + "\n" +
    "<li>" + "<a href=\"#desktop\">Desktop view</a>" + "</li>" + "\n" +
    "<li>" + "<a href=\"#combined\">Combined view</a>" + "</li>" + "\n" +
    "</ul>" + "\n"
  //close div added to HTML_FOOTER

  //Page Content
  val HTML_TAB_CONTENT: String = "<div id=\"tab-content\">" + "\n"

  val HTML_TAB_HEADER: String = "<div class=\"tab-content\">" + "\n"


  val HTML_MOBILE_TAB_CONTENT_HEADER: String = "<div id=\"mobile\" class=\"tab active\">" + "\n" +
    "<p>" + "<h2>Mobile view" + "</h2>" + "</p>" + "\n"

  val HTML_DESKTOP_TAB_CONTENT_HEADER: String = "<div id=\"desktop\" class=\"tab\">" + "\n" +
    "<p>" + "<h2>Desktop view" + "</h2>" + "</p>" + "\n"

  val HTML_COMBINED_TAB_CONTENT_HEADER: String = "<div id=\"combined\" class=\"tab\">" + "\n" +
    "<p>" + "<h2>Combined view" + "</h2>" + "</p>" + "\n"

  val HTML_TAB_CONTENT_FOOTER: String = "</div>" + "\n"

  //Page Tables
  val HTML_REPORT_TABLE_HEADERS: String = "<table id=\"report\">"+ "\n" +
    "<thead>" + "\n" +
    "<tr> <th>Time Last Tested</th>" + "<th>Test VisualsElementType</th>" + "<th>Headline</th>" + "<th>VisualsElementType of Page</th>" + "<th>Time till page is scrollable</th>" + "<th>Time till page looks loaded</th>" + "<th>Page weight (MB)</th>" + "<th>Click for more details</th>" +  "</tr>"+ "\n" +
    "</thead>" +"\n" +
    "<tbody>"

  val HTML_PAGE_ELEMENT_TABLE_HEADERSPT1: String = "<tr>" + "\n" +
    "<td colspan=\"12\">" + "<table id=\"data\" class=\"data\">" + "\n"

  val HTML_PAGE_ELEMENT_TABLE_HEADERSPT2: String =
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
    "</div>" + "\n" +
    "</body>" + "\n" +
    "</html>"


  //HTML_PAGE
  val HTML_PAGE: String = HTML_PAGE_HEAD + HTML_PAGE_CONTAINER + HTML_PAGE_TABS_LIST +
    HTML_TAB_CONTENT + HTML_MOBILE_TAB_CONTENT_HEADER + generateHTMLTable(mobileResultsList) + HTML_TAB_CONTENT_FOOTER +
    HTML_DESKTOP_TAB_CONTENT_HEADER + generateHTMLTable(desktopResultsList) + HTML_TAB_CONTENT_FOOTER +
    HTML_COMBINED_TAB_CONTENT_HEADER + generateHTMLTable(combinedResultsList) + HTML_TAB_CONTENT_FOOTER + HTML_FOOTER


  //page generation methods
  def generateHTMLTable(resultsList: List[PerformanceResultsObject]): String = {
    HTML_REPORT_TABLE_HEADERS + "\n" + generateHTMLDataRows(resultsList) + "\n" + HTML_TABLE_END
  }

  def generateHTMLDataRows(resultsList: List[PerformanceResultsObject]): String = {
    (for (result <- resultsList) yield {
      if(result.alertStatusPageSpeed){
        "<tr class=\"pageclass " + getAlertClass(result) + "\">" + result.toHTMLInteractiveTableCells() + "<td><div class=\"arrow\"></div></td></tr>" + "\n" +
          generatePageElementTable(result)
      } else {
        "<tr class=\"pageclass " + getAlertClass(result) + "\">" + result.toHTMLInteractiveTableCells() + "<td><div>" + "" + "</div></td></tr>" + "\n"
      }
    }).mkString

  }

  def generatePageElementTable(resultsObject: PerformanceResultsObject): String = {
    HTML_PAGE_ELEMENT_TABLE_HEADERSPT1 + "\n"  +
      "<caption class=\"" + getAlertClass(resultsObject) + "\">" + "Alert was triggered because: " + resultsObject.gentestResultStringForInteractivePage() + "</caption>" +
      HTML_PAGE_ELEMENT_TABLE_HEADERSPT2 + getHTMLForPageElements(resultsObject) + HTML_PAGE_ELEMENT_TABLE_END
  }

  def getHTMLForPageElements(resultsObject: PerformanceResultsObject): String = {
      resultsObject.returnHTMLFullElementList()
  }

  def getAlertClass(resultsObject: PerformanceResultsObject): String = {
    if (resultsObject.alertStatusPageSpeed) {
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
