package forms

import java.util.Date
import java.text.SimpleDateFormat
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import model.dto.HackathonWithLocations$
import controllers.Hackathon.hackathonForm
	
class HackathonSpec extends Specification {

	"Hackathon form" should { 
		
		"require all fields" in {
			running(FakeApplication()) {
				val form = hackathonForm.bind(Map.empty[String, String])
				
				form.hasErrors must beTrue
				form.errors.size must equalTo(5)

				form("subject").hasErrors must beTrue
				form("status").hasErrors must beTrue
				form("date").hasErrors must beTrue
				form("description").hasErrors must beTrue
				form("organizerId").hasErrors must beTrue

				form.value must beNone
			}
		}

		"require subject, description and fail if status, date and organizerId not filled" in {
			running(FakeApplication()) {
				val form = hackathonForm.bind(Map("subject" -> "Subject", "description" -> "Description"))

				form.hasErrors must beTrue
				form.errors.size must equalTo(3)

				// no errors
				form("subject").hasErrors must beFalse
				form("description").hasErrors must beFalse

				// errors
				form("status").hasErrors must beTrue
				form("date").hasErrors must beTrue
				form("organizerId").hasErrors must beTrue

				form.data must havePair("subject" -> "Subject")
				form.data must havePair("description" -> "Description")

				form("subject").value must beSome.which(_ == "Subject")
				form("description").value must beSome.which(_ == "Description")

				form("status").value must beNone
				form("date").value must beNone
				form("organizerId").value must beNone

				form.value must beNone
			}
		}

		"validate status as model.HackathonStatus, date as date and organizerId as numeric" in {
			running(FakeApplication()) {
				val form = hackathonForm.bind(Map("subject" -> "Subject", "status" -> "_", "date" -> "_", "description" -> "Description", "organizerId" -> "organizerId"))

				form.hasErrors must beTrue
				form.errors.size must equalTo(3)

				form("status").hasErrors must beTrue
				form("date").hasErrors must beTrue
				form("organizerId").hasErrors must beTrue

				form.value must beNone
			}
		}

	}
}