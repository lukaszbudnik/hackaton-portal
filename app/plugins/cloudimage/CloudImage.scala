package plugins.cloudimage

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

class MockCloudImageService extends CloudImageService {
  
  def upload(filename: String, fileInBytes: Array[Byte]): CloudImageResponse = {
    CloudImageSuccessResponse("url", "secureUrl", "publicId", "1",  "1", "1", "png", "image", "signature")
  }
  
  def destroy(publicId: String) {
  }
  
}
