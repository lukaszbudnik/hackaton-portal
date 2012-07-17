package helpers
import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import play.api.libs.json.Format
import java.util.UUID
import play.api.libs.json.JsString
import play.api.libs.json.JsArray
import play.api.libs.json.JsValue
import play.api.libs.json.JsNumber

case class SponsorLogoDetails(url: String, resourceId : String) 
{

	
}
  object SponsorLogoDetails {
  
  implicit object SponsorLogoDetailsFormat extends Format[SponsorLogoDetails] {
    def reads(json: JsValue): SponsorLogoDetails = new SponsorLogoDetails(null, null)

    def writes(h: SponsorLogoDetails): JsValue = JsArray(Seq(JsObject(List(
      "url" -> JsString(h.url),
      "resourceId" -> JsString(h.resourceId)
    ))))
}
}