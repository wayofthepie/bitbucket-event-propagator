package com.dataember.stash.plugin.hook

import java.util

import com.atlassian.bitbucket.hook.repository.{RepositoryHookContext, AsyncPostReceiveRepositoryHook}
import com.atlassian.bitbucket.repository.{RefChange, Repository}
import com.atlassian.bitbucket.setting.{Settings, SettingsValidationErrors, RepositorySettingsValidator}

import scalaj.http.Http

/**
  * Created by chaospie on 28/11/15.
  */
object MyRepositoryHook extends AsyncPostReceiveRepositoryHook with RepositorySettingsValidator {



  override def validate(settings: Settings,
                        settingsValidationErrors: SettingsValidationErrors,
                        repository: Repository): Unit = {



  }

  override def postReceive(repositoryHookContext: RepositoryHookContext,
                           collection: util.Collection[RefChange]): Unit = {

    Option[String](repositoryHookContext.getSettings().getString("url"))
      .filterNot(_.isEmpty) map(s => Http("http://foo.com/add")
        .postForm(Seq("name" -> "jon", "age" -> "29")).asString)
  }


}
