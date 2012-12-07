package helpers

import play.api.data.format.Formatter
import play.api.data.format.Formats.stringFormat
import play.api.data._
import play.api.data.Forms.of
import helpers.Formats._

object Forms {
  val real: Mapping[Double] = of[Double]
  
  val nonEmptyTextNonHtml: Mapping[String] = of[String] verifying (play.api.data.validation.Constraints.nonEmpty, helpers.Constraints.nonHtml) 

  def enum[E <: Enumeration](enum: E): Mapping[E#Value] = of(enumFormat(enum))
}