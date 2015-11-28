package com.dataember.stash.plugin.msg

import java.util.Properties

import org.nats.Conn

/**
  * Created by chaospie on 28/11/15.
  */
object Nats {
  val opts: Properties = new Properties()
  val conn = Conn.connect(opts)


}
