package helpers

import play.api.data.validation.Constraint
import play.api.data.validation.Invalid
import play.api.data.validation.Valid
import play.api.data.validation.ValidationError
import java.util.regex.Pattern
import java.util.regex.Matcher

object Constraints {

  private lazy val htmlTagRegex = "[^<]*</?([a-zA-Z]+[0-9]*)[^>]*?>.*?"

  /**
   * @TODO this can be moved to application config (with default values hardcoded here)
   */
  private lazy val allowedSimpleHtmlTags = "a, br, ul, ol, li, p, div, span, h1, h2, h3, h4, h5, h6, b, i, strong".split(",").toList.map(_.trim)

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

    val forbiddenTags = tags filterNot (allowedSimpleHtmlTags contains)

    if (forbiddenTags.size > 0) Invalid(ValidationError("error.simpleHtmlOnly", allowedSimpleHtmlTags.mkString(", "))) else Valid
  }

}