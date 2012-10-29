package forms

import java.util.Date
import java.text.SimpleDateFormat
import org.specs2.mutable._
import play.api.test.Helpers._

class PrizeSpec extends Specification {

  import controllers.Prize.prizeForm

  "Prize form" should {

    "require all fields" in {
      val form = prizeForm.bind(Map.empty[String, String])

      form.hasErrors must beTrue
      form.errors.size must equalTo(4)

      form("name").hasErrors must beTrue
      form("description").hasErrors must beTrue
      form("order").hasErrors must beTrue
      form("hackathonId").hasErrors must beTrue

      form.value must beNone
    }

    "require name and description and fail if order and hackathonId not filled" in {
      val form = prizeForm.bind(Map("name" -> "Name", "description" -> "Description"))

      form.hasErrors must beTrue
      form.errors.size must equalTo(2)

      // no errors
      form("name").hasErrors must beFalse
      form("description").hasErrors must beFalse

      // errors
      form("order").hasErrors must beTrue
      form("hackathonId").hasErrors must beTrue

      form.data must havePair("name" -> "Name")
      form.data must havePair("description" -> "Description")

      form("name").value must beSome.which(_ == "Name")
      form("description").value must beSome.which(_ == "Description")

      form("order").value must beNone
      form("hackathonId").value must beNone

      form.value must beNone
    }

    "validate order and hackathonId as numeric" in {
      val form = prizeForm.bind(Map("name" -> "Name", "description" -> "Description", "order" -> "_", "hackathonId" -> "_"))

      form.hasErrors must beTrue
      form.errors.size must equalTo(2)

      form("order").hasErrors must beTrue
      form("hackathonId").hasErrors must beTrue

      form.value must beNone
    }

    "be filled" in {
      val form = prizeForm.bind(Map("name" -> "Name", "description" -> "Description", "order" -> "1",
        "founderName" -> "Founder name", "founderWebPage" -> "Founder web page", "hackathonId" -> "1"))

      form.hasErrors must beFalse

      form.value must beSome.which {
        _ match {
          case (model.Prize("Name", "Description", 1, Some("Founder name"), Some("Founder web page"), 1L)) => true
          case _ => false
        }
      }
    }

    "be filled from model" in {
      val date = new Date
      val form = prizeForm.fill(model.Prize("Name", "Description", 1, Some("Founder name"), Some("Founder web page"), 1L))

      form.hasErrors must beFalse

      form.value must beSome.which {
        _ match {
          case (model.Prize("Name", "Description", 1, Some("Founder name"), Some("Founder web page"), 1L)) => true
          case _ => false
        }
      }
    }

  }
}