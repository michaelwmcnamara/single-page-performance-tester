package app.api

import app.apiutils.PerformanceResultsObject
import com.gu.contentapi.client.model.v1.{Tag, CapiDateTime, ContentFields}

/**
 * Created by mmcnamara on 15/03/16.
 */
class WptResultPageListener(page: String, tone: String,resultUrl: String) {

  val pageUrl: String = page
  val pageType: String = tone
  val wptResultUrl: String = resultUrl
  var testComplete: Boolean = false
  var confirmationNeeded: Boolean = false
  var wptConfirmationResultUrl: String = ""
  var confirmationComplete: Boolean = false
  var testResults: PerformanceResultsObject = null





}
