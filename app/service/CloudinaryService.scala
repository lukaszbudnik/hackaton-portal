package service
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpPost
import play.Play
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.ByteArrayBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.binary.Base64
import org.apache.http.HttpResponse
import org.apache.commons.io.IOUtils
import play.api.Logger
import org.apache.http.entity.mime.HttpMultipartMode
import play.api.libs.json.Json
import play.api.libs.json.JsValue

object CloudinaryService {
  
  val API_KEY = Play.application().configuration().getString("cloudinary.apikey");
  val SECRET_KEY = Play.application().configuration().getString("cloudinary.secretkey")
  val UPLOAD_URL = Play.application().configuration().getString("cloudinary.uploadUrl")
  val DESTROY_URL = Play.application().configuration().getString("cloudinary.destroyUrl")
  
  def uploadImage(filename: String, fileInBytes : Array[Byte]) : Option[CloudinaryResponse] = {
    val httpClient = new DefaultHttpClient
    val post = new HttpPost(UPLOAD_URL)
    val timestamp = scala.compat.Platform.currentTime.toString();
    val multiPartEntity : MultipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE)
    val signature = "timestamp=" + timestamp + SECRET_KEY
    
    multiPartEntity.addPart("file", new ByteArrayBody(fileInBytes, filename))
    multiPartEntity.addPart("timestamp",new StringBody(timestamp))
    multiPartEntity.addPart("api_key", new StringBody(API_KEY))
    
    val signedParams :String = DigestUtils.shaHex(signature)
    multiPartEntity.addPart("signature", new StringBody(signedParams))
    post.setEntity(multiPartEntity)
    
    val r : HttpResponse = httpClient.execute(post)
    val responseBody = IOUtils.toString(r.getEntity().getContent());
    Logger.debug("cloudinary - upload image - response: " + responseBody)
    val jsonResponse = Json.parse(responseBody)
    
    val error = (jsonResponse \ "error" \ "message").asOpt[String]
    if(error.isEmpty) {
      val publicId = (jsonResponse \ "public_id").asOpt[String]
    
      if (publicId.isDefined) {
    	  Some(new CloudinaryImageResponse(
    			  url = (jsonResponse \ "url").as[String],
    			  secureUrl = (jsonResponse \ "secure_url").as[String],
    			  publicId = publicId.get,
    			  version = (jsonResponse \ "version").as[Long].toString,
    			  width = (jsonResponse \ "width").as[Long].toString,
    			  height = (jsonResponse \ "height").as[Long].toString,
    			  format = (jsonResponse \ "format").as[String],
    			  resourceType = (jsonResponse \ "resource_type").as[String],
    			  signature = (jsonResponse \ "signature").as[String]
    	  ))
    	  
      } else {
        None
      }
    } else {
    	Some(new CloudinaryErrorResponse(
    	    message = error.get
    	    ))
      }
    }


  
  def destroyImage(publicId : String) : Unit =  {
    val httpClient = new DefaultHttpClient
    val post = new HttpPost(DESTROY_URL)
    val timestamp = scala.compat.Platform.currentTime.toString();
    val multiPartEntity : MultipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE)
    val signature = "timestamp=" + timestamp + SECRET_KEY
 
    multiPartEntity.addPart("timestamp",new StringBody(timestamp))
    multiPartEntity.addPart("api_key", new StringBody(API_KEY))
    multiPartEntity.addPart("public_id", new StringBody(publicId))
    val signedParams :String = DigestUtils.shaHex(signature)
    multiPartEntity.addPart("signature", new StringBody(signedParams))
    post.setEntity(multiPartEntity)
    
    val r : HttpResponse = httpClient.execute(post)
    r.getEntity().getContent();
    Logger.debug("destroyImage " + publicId)
  }
  
  abstract class CloudinaryResponse;
  
  
  case class CloudinaryImageResponse(val url: String
	  , val secureUrl : String
      , val publicId : String
      , val version : String
      , val width : String
      , val height : String
      , val format : String
      , val resourceType : String
      , val signature : String) extends CloudinaryResponse
  
  case class CloudinaryErrorResponse(val message : String) extends CloudinaryResponse
    object SponsorLogoDetails {
  
 
}
      
     
}