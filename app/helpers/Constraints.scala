package helpers

import play.api.data.validation.Constraint
import play.api.data.validation.Invalid
import play.api.data.validation.Valid
import play.api.data.validation.ValidationError
import java.util.regex.Pattern
import java.util.regex.Matcher

object Constraints {

  private lazy val htmlTagRegex = "[^<]*</?([a-zA-Z]+[0-9]*)[^>]*?>.*?"

  private lazy val allowedSimpleHtmlTags = "br, ul, ol, li, p, div, h1, h2, h3, h4, h5, h6, b, i, strong".split(",").toList.map(_.trim)

  def nonHtml: Constraint[String] = Constraint[String]("constraint.nonHtml") { o =>
    if (o.matches(htmlTagRegex)) Invalid(ValidationError("error.nonHtml")) else Valid
  }

  def simpleHtmlOnly: Constraint[String] = Constraint[String]("constraint.simpleHtmlOnly") { o =>

    def findAllTags(accu: List[String], m: Matcher): List[String] = {
      if (m.find()) {
        findAllTags(m.group(1) :: accu, m)
      } else {
        accu.distinct
      }
    }

    val m: Matcher = Pattern.compile(htmlTagRegex).matcher(o);

    val tags = findAllTags(Nil, m)

    println("found tags")
    println(tags)
    println("intersection 2" + (tags filterNot (allowedSimpleHtmlTags contains)))

    if ((tags filterNot (allowedSimpleHtmlTags contains)).size > 0) Invalid(ValidationError("error.simpleHtmlOnly")) else Valid
  }

}