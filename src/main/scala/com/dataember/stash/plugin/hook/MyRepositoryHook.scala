package com.dataember.stash.plugin.hook

import java.util
import java.util.Properties

import com.atlassian.bitbucket.hook.HookResponse
import com.atlassian.bitbucket.hook.repository.{PreReceiveRepositoryHook, RepositoryHookContext, AsyncPostReceiveRepositoryHook}
import com.atlassian.bitbucket.repository.{RefChange, Repository}
import com.atlassian.bitbucket.setting.{Settings, SettingsValidationErrors, RepositorySettingsValidator}
import com.dataember.stash.plugin.msg.{NatsMessagingService, MessagingService}
import org.nats.Conn
import org.springframework.beans.factory.annotation.Autowired

import scalaj.http.Http

/**
  * Created by chaospie on 28/11/15.
  */
class MyRepositoryHook(val messagingService: MessagingService)
  extends  AsyncPostReceiveRepositoryHook with RepositorySettingsValidator {



  override def validate(settings: Settings,
                        settingsValidationErrors: SettingsValidationErrors,
                        repository: Repository): Unit = {



  }

  override def postReceive(repositoryHookContext: RepositoryHookContext,
                           collection: util.Collection[RefChange]): Unit = {

    messagingService.publish("Post receive!!!")
  }

}
