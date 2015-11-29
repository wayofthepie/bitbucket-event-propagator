package com.dataember.stash.plugin.msg

import java.util.Properties

import com.atlassian.sal.api.pluginsettings.{PluginSettings, PluginSettingsFactory}
import org.nats.Conn
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by chaospie on 28/11/15.
  */
class NatsMessagingService(val pluginSettingsFactory: PluginSettingsFactory) extends MessagingService {

  val logger: Logger = LoggerFactory.getLogger(classOf[NatsMessagingService])

  val NATS_SERVERS_KEY = "servers"

  var conn: Option[Conn] = None

  /**
    * TODO : Figure a better way of handling connections.
    * @return
    */
  def initConnection(): Option[Conn] = {
    val pluginSettings : PluginSettings = pluginSettingsFactory.createGlobalSettings()
    val maybeServers: Option[String] = Option(pluginSettings.get(NATS_SERVERS_KEY).toString)

    conn = maybeServers match {
      case Some(s) =>
        val opts : Properties = new Properties
        opts.put(NATS_SERVERS_KEY,s)

        // The underlying nats library does some crazy checking in this url
        // the craziest bit is that this will throw an array out of bounds
        // exception if not a certain length! So just make sure it starts with
        // nats for now
        if(s.startsWith("nats://")) {
          Some(Conn.connect(opts))
        } else {
          logger.warn("Server protocol incorrect...")
          None
        }
      case None =>
        logger.warn("No servers defined in plugin configuration!")
        None
    }
    conn foreach { _.publish("event-propagator", "Hello fellow listeners!") }
    conn
  }

  /**
    * Calls initConnection() which in turn reloads the connection
    * from the configuration stored in pluginSettings.
    */
  override def refreshConfig(): Unit = {
    logger.warn("Refreshing nats connection configuration.")
    initConnection()
  }

  /**
    * Publish a message.
    * @param msg
    */
  override def publish(msg : String): Unit = {
    conn match {
      case Some(c) =>
        c.isConnected match {
          case true => c.publish("event-propagator", msg)
          case _    => logger.warn("No connection is established!")
        }
      case None => logger.warn("Sending message failed!")
    }
  }

}
