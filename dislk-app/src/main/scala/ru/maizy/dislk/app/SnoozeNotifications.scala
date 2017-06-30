package ru.maizy.dislk.app

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.util.concurrent.BlockingQueue
import scala.sys.process._
import ru.maizy.dislk.app.watcher.{ Event, SnoozeBegin, SnoozeFinish }
import ru.maizy.dislk.macos.notification.MacOsNotification


class SnoozeNotifications(queue: BlockingQueue[Event], osxNotifier: MacOsNotification.type) extends Runnable {

  override def run(): Unit = {
    while(true) {
      try {
        val event = queue.take()
        event match {
          case SnoozeBegin(_) =>
            osxNotifier.notify("snooze started", "close Slack.app and work")

          case SnoozeFinish =>
            osxNotifier.notify("snooze ended", "time to read Slack")
            // TODO: config
            "open /Applications/Slack.app" !

        }
      } catch {
        case _: InterruptedException =>
      }

    }
  }

}
