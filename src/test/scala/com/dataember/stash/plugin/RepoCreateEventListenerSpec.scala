package com.dataember.stash.plugin

import java.io.IOException
import java.util.Properties

import org.junit.Test
import java.net.ConnectException
import org.junit.runner.RunWith
import org.nats._
import org.scalatest._
import org.scalatest.junit._


/**
 * Created by chaospie on 04/07/15.
 */
@RunWith(classOf[JUnitRunner])
class RepoCreateEventListenerSpec extends FlatSpec {


  "A conn" should "publish" in {

    val servers : String = "nats://192.168.1.4:4222"

    val conn : Either[String, Conn] = {
      val props: Properties = new Properties()
      props.put("servers", servers)
      try {
        val conn=Conn.connect(props)
        Right(conn)
      } catch {
        case ex: Throwable =>
          Left("Connex!")
        case ex: IOException =>
          Left("IOex!")
      }

    }
    conn match {
      case Right(c) => c.publish("event-propagator", "test passed")
      case Left(e)   => print(e)
    }
  }


}
