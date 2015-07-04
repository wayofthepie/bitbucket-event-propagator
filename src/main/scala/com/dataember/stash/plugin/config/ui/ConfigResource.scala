package com.dataember.stash.plugin.config.ui

import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status
import javax.ws.rs.{Produces, GET, Path}

import com.atlassian.sal.api.pluginsettings.{PluginSettings, PluginSettingsFactory}
import com.atlassian.sal.api.transaction.{TransactionCallback, TransactionTemplate}
import com.atlassian.sal.api.user.{UserProfile, UserManager}
import org.springframework.http.MediaType

/**
 * Created by chaospie on 04/07/15.
 */
@Path("/")
class ConfigResource(val userManager: UserManager,
                      val pluginSettingsFactory: PluginSettingsFactory,
                      val transactionTemplate: TransactionTemplate) {

  class Config {
    var name = ""
    var time = 0
  }

  @GET
  @Produces(Array("application/json"))
  def get(request: HttpServletRequest): Response = {
    val userProfile: Option[UserProfile] = Option(userManager.getRemoteUser(request))
    val username: Option[String] = userProfile.map(up => up.getUsername)
    val isSysAdmin: Boolean =
      userProfile.map(up => userManager.isSystemAdmin(up.getUserKey)) match {
        case Some(b) => b
        case None    => false
    }

    if(username == None || ! isSysAdmin)
      Response.status(Status.UNAUTHORIZED).build()
    else
      Response.ok(transactionTemplate.execute(new TransactionCallback[Config] {
        override def doInTransaction(): Config = {
          val settings: PluginSettings = pluginSettingsFactory.createGlobalSettings()
          val config: Config = new Config

          // FIXME: Should be more functional....
          config.name = settings.get(classOf[Config].getName + ".name").asInstanceOf[String]
          val time = settings.get(classOf[Config].getName + ".time").asInstanceOf[String]

          if(time != null) config.time = Integer.parseInt(time)

          config
        }
      })).build()
  }

}
