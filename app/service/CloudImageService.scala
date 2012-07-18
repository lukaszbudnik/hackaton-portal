package service
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.content.ByteArrayBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.HttpResponse

import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.Application
import play.api.Logger
import plugin.cloudimage.CloudImageErrorResponse
import plugin.cloudimage.CloudImageResponse
import plugin.cloudimage.CloudImageServicePlugin
import plugin.cloudimage.CloudImageSuccessResponse


class CloudImageService(application: Application) extends CloudImageServicePlugin(application) {


  
  def upload(filename: String, fileInBytes: Array[Byte]): CloudImageResponse = {
    Logger.debug(String.format("CloudImageService - upload - start"));
    
    val httpClient = new DefaultHttpClient
    val post = new HttpPost(uploadUrl)
    val timestamp = scala.compat.Platform.currentTime.toString();
    val multiPartEntity: MultipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE)
    val signature = "timestamp=" + timestamp + secretKey

    multiPartEntity.addPart("file", new ByteArrayBody(fileInBytes, filename))
    multiPartEntity.addPart("timestamp", new StringBody(timestamp))
    multiPartEntity.addPart("api_key", new StringBody(apiKey))

    val signedParams: String = DigestUtils.shaHex(signature)
    multiPartEntity.addPart("signature", new StringBody(signedParams))
    post.setEntity(multiPartEntity)

    val r: HttpResponse = httpClient.execute(post)
    val responseBody = IOUtils.toString(r.getEntity().getContent());
    Logger.debug("cloudinary - upload image - response: " + responseBody)
    val jsonResponse = Json.parse(responseBody)

    val error = (jsonResponse \ "error" \ "message").asOpt[String]
    if (error.isEmpty) {
      val publicId = (jsonResponse \ "public_id").asOpt[String]

      if (publicId.isDefined) {
        new CloudImageSuccessResponse(
          url = (jsonResponse \ "url").as[String],
          secureUrl = (jsonResponse \ "secure_url").as[String],
          publicId = publicId.get,
          version = (jsonResponse \ "version").as[Long].toString,
          width = (jsonResponse \ "width").as[Long].toString,
          height = (jsonResponse \ "height").as[Long].toString,
          format = (jsonResponse \ "format").as[String],
          resourceType = (jsonResponse \ "resource_type").as[String],
          signature = (jsonResponse \ "signature").as[String])

      } else {
        new CloudImageErrorResponse(message = Messages("error.unknown"))
      }
    } else {
      new CloudImageErrorResponse(message = error.get)
    }
  }

  def destroy(publicId: String) {
    
    Logger.debug(String.format("CloudImageService - destroy, publicId= %s", publicId));
    
    val httpClient = new DefaultHttpClient
    val post = new HttpPost(destroyUrl)
    val timestamp = scala.compat.Platform.currentTime.toString();
    val multiPartEntity: MultipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE)
    val signature = "timestamp=" + timestamp + secretKey

    multiPartEntity.addPart("timestamp", new StringBody(timestamp))
    multiPartEntity.addPart("api_key", new StringBody(apiKey))
    multiPartEntity.addPart("public_id", new StringBody(publicId))
    val signedParams: String = DigestUtils.shaHex(signature)
    multiPartEntity.addPart("signature", new StringBody(signedParams))
    post.setEntity(multiPartEntity)

    val r: HttpResponse = httpClient.execute(post)
    r.getEntity().getContent();
  }


}
  	