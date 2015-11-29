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
  val pluginSettings: PluginSettings = pluginSettingsFactory.createGlobalSettings()

  /**
    * Respond with the exception message.
    * @param e
    * @return
    */
  def errorHandler(e:Exception) = Response.serverError().entity(e.getLocalizedMessage).build()

  /**
    * HTTP 200 with the given type.
    * @param entity
    * @tparam A
    * @return
    */
  def success[A](entity:A):Response = Response.ok(entity).build()

  /**
    * Retrieve The current nats connection configuration.
    * @return
    */
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getCurrentConfig(): Response = {
    success[Config](transactionTemplate.execute(new TransactionCallback[Config] {
      override def doInTransaction(): Config = {
        val config: Config = new Config
        Option[String](pluginSettings.get(NatsProps.NATS_SERVERS_KEY).asInstanceOf[String]) match {
          case Some(servers) =>
            config.servers = servers
            messagingService.publish("retrieved name " + " retrieved " + config.servers)
          case None => logger.warn("No servers found!")
        }
        config
      }
    }))
  }

  /**
    * Update the nats connection configuration.
    * @param config
    * @return
    */
  @PUT
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def updateConfig(config: Config): Response = {
    transactionTemplate.execute(new TransactionCallback[Unit] {
      override def doInTransaction(): Unit= {
        val pluginSettings: PluginSettings = pluginSettingsFactory.createGlobalSettings()
        pluginSettings.put(NatsProps.NATS_SERVERS_KEY, config.servers)
        messagingService.refreshConfig()
      }
    })
    Response.noContent().build()
  }
}

/**
  * Represents the nats connection configuration.
  */
@XmlRootElement
class Config {
  @XmlElement
  var servers: String = ""
}