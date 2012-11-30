package controllers

import org.specs2.mutable.Specification
import scala.io.Source
import scala.tools.nsc.io.File
import play.api.test._
import play.api.test.Helpers._
import helpers.SecureSocialUtils
import org.specs2.matcher.MatchResult
import scala.collection.Map
import scala.xml.XML
import play.api.Play

class RoutesFileSpec extends Specification {
  
  val idMap = Map("/locations/:id" -> "/locations/1",
    "/hackathons/:id/:uid" -> "/hackathons/1/1",
    "/hackathons/:id" -> "/hackathons/1",
    "/hackathons/:hid" -> "/hackathons/1",
    "/hackathons/:hid/prizes" -> "/hackathons/1/prizes",
    "/changeLanguage/:lang" -> "/changeLanguage/PL",
    "/locations/find" -> "/locations/find?term=dummy",
    "/news/:id" -> "/news/1",
    "/search/:label" -> "/search/string",
    "/hackathon.json/:id" -> "/hackathon.json/1",
    "/teams/:id" -> "/teams/1",
    "/prizes/:id" -> "/prizes/1",
    "/sponsors/:id" -> "/sponsors/1",
    "/authenticate/:provider" -> "/authenticate/google",
    "/contents/:key" -> "/contents/1",
    "chat/:id" -> "chat/1",
    "/:uid" -> "/1",
    "/problems/:id" -> "/problems/1",
    """/assets/\*file""" -> "/assets/images/favicon.png",
    "uploadLogo/:id" -> "uploadLogo/1")

  "entry in routes file" >> {

    parse foreach { tuple =>
      ("action renders correctly: " + tuple) >> {
        val fakeApp = FakeApplication(additionalConfiguration = inMemoryDatabase())
        running(fakeApp) {
          val httpMethod = tuple._1
          var action = tuple._2

          val mongoMock = Play.current.configuration.getString("mongodb.uri").get == "mock"

          if (mongoMock && (action.startsWith("/contents") || action.startsWith("/messages.js"))) {
            skipped("skipping test for mock mongodb")
          }

          idMap.map(id => action = action.replaceAll(id._1, id._2))

          val result = SecureSocialUtils.fakeAuth(FakeRequest(httpMethod, action), fakeApp)
          
//          contentType(result) match {
//            case Some(contentType: String) if contentType == "text/html" || contentType == "application/xml" => XML.loadString(contentAsString(result).replaceAll("&", "&#38;"))
//            case _ =>
//          }
          
          status(result) must beOneOf(OK, SEE_OTHER)

        }
      }

    }

  }

  def parse: Iterator[(String, String)] = {

    val regex = """(\w+)\s+([^\s]+)\s+(.+)""".r
    val lines: Iterator[String] = scala.io.Source.fromFile("conf/routes").getLines.filter((l: String) => (!l.startsWith("#") && !l.startsWith("POST") && l.trim().length() > 0))
    lines.map(line => line.trim() match {
      case regex(httpM, action, contrMeth) => (httpM.trim(), action.trim())
      case _ => {
        throw new IllegalArgumentException("routes file format incorrect")
      }
    })

  }

}