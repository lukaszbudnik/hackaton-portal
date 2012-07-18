package plugin.cloudimage

import play.api.Application
import play.api.Logger
import play.api.Play
import play.api.Plugin


abstract class CloudImageResponse;


case class CloudImageErrorResponse(val message: String) extends CloudImageResponse

case class CloudImageSuccessResponse(val url: String, val secureUrl: String,
    val publicId: String, val version: String, val width: String, val height: String,
    val format: String, val resourceType: String, val signature: String) extends CloudImageResponse




trait CloudImageService {
  def upload(filename: String, fileInBytes: Array[Byte]): CloudImageResponse
  def destroy(publicId: String): Unit
}

abstract class CloudImageServicePlugin(application: Application) extends Plugin with CloudImageService {

  private val parsingTheme = """cloudinary://(.*)[:](.*)[@](.*)""".r

  private val UPLOAD_URL_PATTERN = "http://api.cloudinary.com/v1_1/%s/image/upload"
  private val DESTROY_URL_PATTERN = "http://api.cloudinary.com/v1_1/%s/image/destroy"

  protected var apiKey: String = _
  protected var secretKey: String = _
  protected var uploadUrl: String = _
  protected var destroyUrl: String = _

  override def onStart() {
    CloudImageService.setService(this)
    configure
    Logger.info("Registered CloudImageService: " + this.getClass)
  }

  private def configure() {

    val confUrl = Play.application(application).configuration.getString("cloudinary.url").getOrElse {
      CloudImageService.notInitialized()
    }
    val parsingTheme(p_api_key, p_secret_key, p_cloud_name) = confUrl
    apiKey = p_api_key
    secretKey = p_secret_key
    uploadUrl = String.format(UPLOAD_URL_PATTERN, p_cloud_name)
    destroyUrl = String.format(DESTROY_URL_PATTERN, p_cloud_name)

    Logger.debug(String.format("CloudImageServicePlugin initialized: api_key: %s, secret_key: %s, cloud_name: %s", apiKey, secretKey, p_cloud_name));
  }

}

object CloudImageService {

  var delegate: Option[CloudImageService] = None

  def setService(service: CloudImageService) {
    delegate = Some(service)
  }

  def upload(filename: String, fileInBytes: Array[Byte]) = {
    delegate.map(_.upload(filename, fileInBytes)).getOrElse {
      notInitialized()
    }
  }
  def destroy(publicId: String) {
    delegate.map(_.destroy(publicId)).getOrElse {
      notInitialized()
    }
  }

  def notInitialized() {
    Logger.error("CloudImageService was not initialized. Make sure a CloudImageService plugin is specified in your play.plugins file and check cloudinary.url application variable")
    throw new RuntimeException("CloudImageService not initialized")
  }

}

