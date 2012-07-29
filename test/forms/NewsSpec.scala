package forms

import java.util.Date
import java.text.SimpleDateFormat
import org.specs2.mutable._
import play.api.test.Helpers._

class NewsSpec extends Specification {
  
  import controllers.News.newsForm
  
  "News form" should {
    
    "require all fields" in {
      val form = newsForm.bind(Map.empty[String,String])
      
      form.hasErrors must beTrue
      form.errors.size must equalTo(5)
      
      form("title").hasErrors must beTrue
      form("text").hasErrors must beTrue
      form("labelsAsString").hasErrors must beTrue
      form("authorId").hasErrors must beTrue
      form("publishedDate").hasErrors must beTrue
      
      form.value must beNone
    }
    
    "require title, text, labels and fail if authorId and published not filled" in {
      val form = newsForm.bind(Map("title" -> "ABC", "text" -> "XYZ", "labelsAsString" -> "QWQ"))
      
      form.hasErrors must beTrue
      form.errors.size must equalTo(2)
      
      // no errors
      form("title").hasErrors must beFalse
      form("text").hasErrors must beFalse
      form("labelsAsString").hasErrors must beFalse
      
      // errors
      form("authorId").hasErrors must beTrue
      form("publishedDate").hasErrors must beTrue
      
      form.data must havePair("title" -> "ABC")
      form.data must havePair("text" -> "XYZ")
      form.data must havePair("labelsAsString" -> "QWQ")
      
      form("title").value must beSome.which(_ == "ABC")
      form("text").value must beSome.which(_ == "XYZ")
      form("labelsAsString").value must beSome.which(_ == "QWQ")

      form("authorId").value must beNone
      form("publishedDate").value must beNone
      
      form.value must beNone
    }
    
    "validate authorId as numeric and published as date" in {
      val form = newsForm.bind(Map("title" -> "ABC", "text" -> "XYZ", "labelsAsString" -> "QWQ", "authorId" -> "_", "publishedDate" -> "string"))
      
      form.hasErrors must beTrue
      form.errors.size must equalTo(2)
      
      form("authorId").hasErrors must beTrue
      form("publishedDate").hasErrors must beTrue
      
      form.value must beNone
    }
    
    "be filled" in {
      val form = newsForm.bind(Map("title" -> "ABC", "text" -> "XYZ", "labelsAsString" -> "QWQ", "authorId" -> "12", "publishedDate" -> "31-12-2012"))
      val date = new SimpleDateFormat("yyy-MM-dd").parse("2012-01-01")
      
      form.hasErrors must beFalse
      
      form.value must beSome.which { _ match {
        case (model.News("ABC", "XYZ", "QWQ", 12L, date, None)) => true
        case _ => false
      }}
    }
    
    "be filled from model" in {
	  val date = new SimpleDateFormat("yyyy-MM-dd").parse("2012-01-01")
      val form = newsForm.fill(model.News("ABC", "XYZ", "QWQ", 12L, date, Some(123)))
      
      form.hasErrors must beFalse
      
      form.value must beSome.which { _ match {
        case (model.News("ABC", "XYZ", "QWQ", 12L, date, Some(123))) => true
        case _ => false
      }}
    }
    
  }
  
}