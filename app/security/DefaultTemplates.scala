package security

import play.api.Application
import play.api.data.Form
import play.api.mvc.Request
import play.api.templates.Html
import securesocial.controllers.DefaultTemplatesPlugin 

class DefaultTemplates(application: Application) extends DefaultTemplatesPlugin(application)
{
 /**
   * Returns the html for the login page
   * @param request
   * @tparam A
   * @return
   */
  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)],
                               msg: Option[String] = None): Html =
  {
    views.html.securitytemplates.login(form, msg)
  }
}