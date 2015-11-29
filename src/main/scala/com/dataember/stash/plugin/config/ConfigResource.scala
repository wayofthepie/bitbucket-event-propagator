package com.dataember.stash.plugin.config

import javax.ws.rs._
import javax.ws.rs.core.{MediaType, Response}
import javax.xml.bind.annotation.{XmlElement, XmlRootElement}

import com.atlassian.bitbucket.rest.util.RestUtils
import com.atlassian.plugins.rest.common.security.AnonymousAllowed
import com.atlassian.sal.api.pluginsettings.{PluginSettings, PluginSettingsFactory}
import com.atlassian.sal.api.transaction.{TransactionCallback, TransactionTemplate}
import com.atlassian.sal.api.user.UserManager
import com.dataember.stash.plugin.msg.MessagingService
import org.slf4j.{Logger, LoggerFactory}

/**
 *
 */
@Path("/")
@Consumes(Array({MediaType.APPLICATION_JSON}))
@Produces(Array({RestUtils.APPLICATION_JSON_UTF8}))
@AnonymousAllowed
class ConfigResource(val messagingService : MessagingService,
                     val userManager: UserManager,
                     val pluginSettingsFactory: PluginSettingsFactory,
                     val transactionTemplate: TransactionTemplate) {

  val logger: Logger = LoggerFactory.getLogger(classOf[ConfigResource])

  val NATS_SERVERS_KEY = "servers"
  val pluginSettings: PluginSettings = pluginSettingsFactory.createGlobalSettings()

  def errorHandler(e:Exception) = Response.serverError().entity(e.getLocalizedMessage).build()
  def success[A](entity:Config):Response = Response.ok(entity).build()



  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  def  get(): Response = {
    success(transactionTemplate.execute(new TransactionCallback[Config] {
      override def doInTransaction(): Config = {

        val config: Config = new Config

        Option[String](pluginSettings.get(NATS_SERVERS_KEY).asInstanceOf[String]) match {
          case Some(servers) =>
            config.servers = servers
            messagingService.publish("retrieved name " + " retrieved " + config.servers)
          case None => logger.warn("No servers found!")
        }
        config
      }
    }))
  }

  @PUT
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def put(config: Config): Response = {
    transactionTemplate.execute(new TransactionCallback[Unit] {
      override def doInTransaction(): Unit= {
        val pluginSettings: PluginSettings = pluginSettingsFactory.createGlobalSettings()
        pluginSettings.put(NATS_SERVERS_KEY, config.servers)
        messagingService.refreshConfig()
      }
    })
    Response.noContent().build()
  }
}

@XmlRootElement
class Config {
  @XmlElement
  var servers: String = ""
}