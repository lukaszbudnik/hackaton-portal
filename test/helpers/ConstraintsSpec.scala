package helpers

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.data.validation.Invalid
import play.api.data.validation.Valid

class ConstraintsSpec extends Specification {

  "nonHtml constraint" should {

    "return invalid when xml tags detected" in {

      val invalidTexts = Seq("<how>are you!", " asd s </div> saasd", "this is a test<br/>", "<nested><tags>", "<script><!-- danger --></script>", "<div class=\"asas\" />")

      invalidTexts.map(it => Constraints.nonHtml(it).getClass().getSimpleName()) must beEqualTo(Seq("Invalid", "Invalid", "Invalid", "Invalid", "Invalid", "Invalid"))
    }

    "allow < and > when used with space" in {

      val invalidTexts = Seq("2 < 3", "1 > -1 and -1 < 1")

      invalidTexts.map(it => Constraints.nonHtml(it)) must beEqualTo(Seq(Valid, Valid))
    }
  }

  "simpleHtmlOnly constraint" should {

    "allow simple tags" in {
      val text = "br, ul, ol, li, p, div, h1, h2, h3, h4, h5, h6, b, i, strong"
        
      val html = text.split(",").map(_.trim).mkString("<", "> some content here <", ">") + "<ul><li><b>qwq</b></li></ul>"
      
      Constraints.simpleHtmlOnly(html) must beEqualTo(Valid)
    }
    
    "return invalid when xml tags detected" in {

      val invalidTexts = Seq("<how>are you!", " asd s </div> saasd", "this is a test<br/>", "<nested><tags>", "<script><!-- danger --></script>", "<div class=\"asas\" />")

      invalidTexts.map(it => Constraints.nonHtml(it).getClass().getSimpleName()) must beEqualTo(Seq("Invalid", "Invalid", "Invalid", "Invalid", "Invalid", "Invalid"))
    }

    "allow < and > when used with space" in {

      val invalidTexts = Seq("2 < 3", "1 > -1 and -1 < 1")

      invalidTexts.map(it => Constraints.nonHtml(it)) must beEqualTo(Seq(Valid, Valid))
    }

  }

}