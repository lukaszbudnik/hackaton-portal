package forms

import java.util.Date
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
      form("labels").hasErrors must beTrue
      form("authorId").hasErrors must beTrue
      form("published").hasErrors must beTrue
      
      form.value must beNone
    }
    
    "require title, text, labels and fail if authorId and published not filled" in {
      val form = newsForm.bind(Map("title" -> "ABC", "text" -> "XYZ", "labels" -> "QWQ"))
      
      form.hasErrors must beTrue
      form.errors.size must equalTo(2)
      
      // no errors
      form("title").hasErrors must beFalse
      form("text").hasErrors must beFalse
      form("labels").hasErrors must beFalse
      
      // errors
      form("authorId").hasErrors must beTrue
      form("published").hasErrors must beTrue
      
      form.data must havePair("title" -> "ABC")
      form.data must havePair("text" -> "XYZ")
      form.data must havePair("labels" -> "QWQ")
      
      form("title").value must beSome.which(_ == "ABC")
      form("text").value must beSome.which(_ == "XYZ")
      form("labels").value must beSome.which(_ == "QWQ")

      form("authorId").value must beNone
      form("published").value must beNone
      
      form.value must beNone
    }
    
    "validate authorId as numeric and published as date" in {
      val form = newsForm.bind(Map("title" -> "ABC", "text" -> "XYZ", "labels" -> "QWQ", "authorId" -> "_", "published" -> "string"))
      
      form.hasErrors must beTrue
      form.errors.size must equalTo(2)
      
      form("authorId").hasErrors must beTrue
      form("published").hasErrors must beTrue
      
      form.value must beNone
    }
    
    "be filled" in {
      val form = newsForm.bind(Map("title" -> "ABC", "text" -> "XYZ", "labels" -> "QWQ", "authorId" -> "12", "published" -> "31/12/2012"))
      val date = new Date(31,12,2012);
      
      form.hasErrors must beFalse
      
      form.value must beSome.which { _ match {
        case (model.News("ABC", "XYZ", "QWQ", 12L, date)) => true
        case _ => false
      }}
    }
    
    "be filled from model" in {
	  val date = new Date(31,12,2012);
      val form = newsForm.fill(model.News("ABC", "XYZ", "QWQ", 12L, date))
      
      form.hasErrors must beFalse
      
      form.value must beSome.which { _ match {
        case (model.News("ABC", "XYZ", "QWQ", 12L, date)) => true
        case _ => false
      }}
    }
    
  }
  
}