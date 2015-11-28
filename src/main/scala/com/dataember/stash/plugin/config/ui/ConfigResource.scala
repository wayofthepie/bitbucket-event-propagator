package com.dataember.stash.plugin.config.ui

import java.util.Properties
import javax.servlet.http.HttpServletRequest
import javax.sound.sampled.UnsupportedAudioFileException
import javax.ws.rs.core.{MediaType, Response}
import javax.ws.rs.core.Response.Status
import javax.ws.rs._
import javax.xml.bind.annotation.{XmlElement, XmlRootElement}

import com.atlassian.bitbucket.rest.util.{RestUtils, ResourcePatterns}
import com.atlassian.plugins.rest.common.security.AnonymousAllowed
import com.atlassian.sal.api.pluginsettings.{PluginSettings, PluginSettingsFactory}
import com.atlassian.sal.api.transaction.{TransactionCallback, TransactionTemplate}
import com.atlassian.sal.api.user.{UserProfile, UserManager}
import org.nats.Conn
import org.springframework.beans.factory.annotation.Autowired

import scala.collection.JavaConverters._

/**
 *
 */
@Path("/")
@Consumes(Array({MediaType.APPLICATION_JSON}))
@Produces(Array({RestUtils.APPLICATION_JSON_UTF8}))
@AnonymousAllowed
class ConfigResource(val userManager: UserManager,
                      val pluginSettingsFactory: PluginSettingsFactory,
                      val transactionTemplate: TransactionTemplate) {


  val NATS_SERVERS_KEY = "servers"

  /* TODO : There must be a better way to do this, without resorting to spring... */
  @Autowired
  implicit var conn : Conn = null

  def errorHandler(e:Exception) = Response.serverError().entity(e.getLocalizedMessage).build()
  def success[A](entity:Config):Response = Response.ok(entity).build()



  @GET
  @Path(value = "test")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def test(implicit conn: Conn): Response = {
    val config = new Config
    config.servers = "test"
    config.time = ""
    conn.publish("test", "test")
    success(config)
  }

  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  def  get(): Response = {

      success(transactionTemplate.execute(new TransactionCallback[Config] {
        override def doInTransaction(): Config = {
          val settings: PluginSettings = pluginSettingsFactory.createGlobalSettings()
          val config: Config = new Config

          // FIXME: Should be more functional....
          val name: Option[String] =
            Option(settings.get(classOf[Config].getName + ".name").asInstanceOf[String])

          val time: Option[String] =
            Option(settings.get(classOf[Config].getName + ".time").asInstanceOf[String])



          config.time = time match {
            case None    => "0"
            case Some(t) => t
          }
          conn.publish("test", "retrieved name " + " retrieved " + config.time)
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

        pluginSettings.put(classOf[Config].getName + ".time", config.time)
      }
    })
    conn.publish("test", "updated settings "  + "  : " + config.time)

    Response.noContent().build()
  }
}

@XmlRootElement
class Config {
  @XmlElement
  var servers = "nats://192.168.1.4:4222"

  @XmlElement
  var time = ""
}

