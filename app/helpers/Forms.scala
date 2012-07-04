package helpers

import play.api.data.format.Formatter
import play.api.data.format.Formats.stringFormat
import play.api.data._
import play.api.data.Forms.of
import helpers.Formats._

object Forms {
  var real: Mapping[Double] = of[Double]

  def enum[E <: Enumeration](enum: E): Mapping[E#Value] = of(enumFormat(enum))
}