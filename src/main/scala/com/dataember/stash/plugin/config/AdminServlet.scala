package com.dataember.stash.plugin.config

import java.net.URI
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}

import com.atlassian.sal.api.auth.LoginUriProvider
import com.atlassian.sal.api.user.{UserProfile, UserManager}
import com.atlassian.templaterenderer.TemplateRenderer

/**
 * Created by chaospie on 04/07/15.
 */
class AdminServlet(val userManager: UserManager,
                   val loginUriProvider: LoginUriProvider,
                   val renderer: TemplateRenderer)
  extends HttpServlet {

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    val userProfile: Option[UserProfile] = Option(userManager.getRemoteUser(req))
    val username: Option[String] = userProfile.map(up => up.getUsername)

    // Repetition....
    def isSysAdmin(userProfile: Option[UserProfile],
                    userManager: UserManager): Boolean = {
      userProfile.map(up => up.getUserKey) match {
        case Some(userKey) => userManager.isSystemAdmin(userKey)
        case None          => false
      }
    }

    if(username == null || ! isSysAdmin(userProfile,userManager))
      redirectToLogin(req,resp)
    else
      resp.setContentType("text/html;charset=utf-8");
      renderer.render("admin.vm", resp.getWriter());

  }

  private def redirectToLogin(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString)
  }

  private def getUri(request: HttpServletRequest ): URI = {
    val buff: StringBuffer = request.getRequestURL

    URI.create(if(request.getQueryString != null)
      buff.append("?" + request.getQueryString).toString
    else
      buff.toString)
  }
}
