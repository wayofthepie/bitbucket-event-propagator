package com.dataember.stash.plugin.hook

import java.util

import com.atlassian.bitbucket.hook.repository.{AsyncPostReceiveRepositoryHook, RepositoryHookContext}
import com.atlassian.bitbucket.repository.{RefChange, Repository}
import com.atlassian.bitbucket.setting.{RepositorySettingsValidator, Settings, SettingsValidationErrors}
import com.dataember.stash.plugin.msg.MessagingService

/**
  * Created on 28/11/15.
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
