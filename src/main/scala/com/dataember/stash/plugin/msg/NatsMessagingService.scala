package com.dataember.stash.plugin.msg

import java.util.Properties

import com.atlassian.sal.api.pluginsettings.{PluginSettings, PluginSettingsFactory}
import org.nats.Conn
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.stereotype.Service

/**
  * Created by chaospie on 28/11/15.
  */
class NatsMessagingService(val pluginSettingsFactory: PluginSettingsFactory) extends MessagingService {

  val NATS_SERVERS_KEY = "servers"
  val NATS_SERVERS = "nats://192.168.1.4:4222"

  implicit val conn: Conn = initConnection()

  def initConnection(): Conn = {
    val pluginSettings : PluginSettings = pluginSettingsFactory.createGlobalSettings()
    pluginSettings.put(NATS_SERVERS_KEY, "nats://192.168.1.4:4222")

    val opts : Properties = new Properties

    val maybeServers: Option[String] = Option(pluginSettings.get(NATS_SERVERS_KEY).toString)

    val conn = maybeServers filterNot(_.isEmpty) map (s => {
      opts.put(NATS_SERVERS_KEY,s)
      Conn.connect(opts)
    }) getOrElse Conn.connect(new Properties())
    conn.publish("event-propagator", "Connection initiated.")
    return conn
  }

  override def publish(msg : String): Unit = {
    conn.publish("event-propagator", msg)
  }

}
