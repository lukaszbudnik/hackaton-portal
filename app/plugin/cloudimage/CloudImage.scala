package plugin.cloudimage

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
import play.api.Play
import play.api.Plugin

abstract class CloudImageResponse

case class CloudImageErrorResponse(val message: String) extends CloudImageResponse

case class CloudImageSuccessResponse(val url: String, val secureUrl: String,
  val publicId: String, val version: String, val width: String, val height: String,
  val format: String, val resourceType: String, val signature: String) extends CloudImageResponse

trait CloudImageService {
  def upload(filename: String, fileInBytes: Array[Byte]): CloudImageResponse
  def destroy(publicId: String): Unit
}

trait CloudImagePlugin extends play.api.Plugin {
  def cloudImageService: CloudImageService
}
