
package object plugin {
  
  import play.api._
  import play.api.mvc._

  def use[A <: Plugin](implicit app: Application, m: Manifest[A]) = {
    app.plugin[A].getOrElse(throw new RuntimeException(m.erasure.toString+ " plugin should be available at this point"))
  }

}