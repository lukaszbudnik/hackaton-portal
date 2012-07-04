package helpers

import play.api.data.format.Formatter
import play.api.data.format.Formats.stringFormat
import play.api.data.FormError

object Formats {
  implicit def doubleFormat: Formatter[Double] = new Formatter[Double] {

    override val format = Some("format.numeric", Nil)

    def bind(key: String, data: Map[String, String]) = {
      stringFormat.bind(key, data).right.flatMap {
        s =>
          scala.util.control.Exception.allCatch[Double]
            .either(s.toDouble)
            .left.map(e => Seq(FormError(key, "error.number", Nil)))
      }
    }

    def unbind(key: String, value: Double) = Map(key -> value.toString)
  }

  def enumFormat[E <: Enumeration](enum: E): Formatter[E#Value] = new Formatter[E#Value] {

    override val format = Some("format.enum", Nil)

    def bind(key: String, data: Map[String, String]) = {
      stringFormat.bind(key, data).right.flatMap {
        s =>
          scala.util.control.Exception.allCatch[E#Value]
            .either(enum.withName(s))
            .left.map(e => Seq(FormError(key, "error.enum", Nil)))
      }
    }

    def unbind(key: String, value: E#Value) = Map(key -> value.toString)
  }

}
