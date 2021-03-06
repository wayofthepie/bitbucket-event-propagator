package com.dataember.stash.plugin

import java.util.Properties

import com.atlassian.bitbucket.event.repository.RepositoryCreatedEvent
import com.atlassian.bitbucket.project.ProjectService
import com.atlassian.event.api.{EventListener, AsynchronousPreferred}
import org.nats._
import scalaz.Reader


/**
 * Created by chaospie on 04/07/15.
 */
class RepoCreateEventListener(val projectService: ProjectService) {

  val opts: Properties = new Properties()
  val conn = Conn.connect(opts)

  @EventListener
  def repoCreateListener(createEvent: RepositoryCreatedEvent): Unit = {
    conn.publish("test",
      "Repo " + createEvent.getRepository.getName +
        " created by " + createEvent.getUser + " in project " +
        createEvent.getRepository.getProject.getName)
  }
}
