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

  val opts: Properties = new Properties()
  val conn = Conn.connect(opts)

  def errorHandler(e:Exception) = Response.serverError().entity(e.getLocalizedMessage).build()
  def success[A](entity:Config):Response = Response.ok(entity).build()

  @GET
  @Path(value = "test")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def test(): Response = {
    val config = new Config
    config.name = "test"
    config.time = ""

    success(config)
  }

  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  def get(): Response = {

      success(transactionTemplate.execute(new TransactionCallback[Config] {
        override def doInTransaction(): Config = {
          val settings: PluginSettings = pluginSettingsFactory.createGlobalSettings()
          val config: Config = new Config

          // FIXME: Should be more functional....
          val name: Option[String] =
            Option(settings.get(classOf[Config].getName + ".name").asInstanceOf[String])

          val time: Option[String] =
            Option(settings.get(classOf[Config].getName + ".time").asInstanceOf[String])

          config.name = name match {
            case None    => ""
            case Some(x) => x
          }

          config.time = time match {
            case None    => "0"
            case Some(t) => t
          }
          conn.publish("test", "retrieved name " + config.name + " retrieved " + config.time)
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
        pluginSettings.put(classOf[Config].getName + ".name", config.name)
        pluginSettings.put(classOf[Config].getName + ".time", config.time)
      }
    })
    conn.publish("test", "updated settings " + config.name + "  : " + config.time)

    Response.noContent().build()
  }
}

@XmlRootElement
class Config {
  @XmlElement
  var name = ""

  @XmlElement
  var time = ""
}

