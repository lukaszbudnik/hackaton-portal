package helpers

import play.api.data.format.Formatter
import play.api.data.format.Formats.stringFormat
import play.api.data._
import helpers.Formats.doubleFormat

object Forms {
  var real: Mapping[Double] = play.api.data.Forms.of[Double]
}