package ru.maizy.dislk.app

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import java.util.{ Date, IllegalFormatConversionException }
import java.util.concurrent.BlockingQueue
import scala.sys.process._
import ru.maizy.dislk.app.watcher.{ Event, SnoozeActivatedOnStartUp, SnoozeBegin, SnoozeDeactivatedOnStartUp }
import ru.maizy.dislk.app.watcher.SnoozeFinish
import ru.maizy.dislk.macos.notification.MacOsNotification
import ru.maizy.dislk.slackapi


class SnoozeNotifications(
    slackEventQueue: BlockingQueue[Event],
    osxNotifier: MacOsNotification.type,
    slackClient: slackapi.Client,
    appConfig: AppConfig
) extends Runnable {

  private def setSlackStatus(ends: ZonedDateTime): Unit = {
    appConfig.autoSetStatus foreach { autoSetConfig =>
      val endDate = Date.from(ends.toInstant)
      val text = try {
        String.format(autoSetConfig.text, endDate)
        // TODO: warn log
      } catch {
        case e: IllegalFormatConversionException =>
          String.format(AutosetStatus.DEFAULT_STATUS_TEXT, endDate)
      }
      slackClient.setStatus(text, autoSetConfig.emoji)
    }

    ()
  }

  private def unsetSlackStatus(): Unit = {
    if(appConfig.autoSetStatus.isDefined) {
      slackClient.setStatus("", "")
    }
    ()
  }

  override def run(): Unit = {
    while(true) {
      try {
        val event = slackEventQueue.take()
        event match {
          case SnoozeBegin(ends) =>
            osxNotifier.notify("snooze started", "close Slack.app and work")
            setSlackStatus(ends)

          case SnoozeFinish =>
            osxNotifier.notify("snooze ended", "time to read Slack")
            unsetSlackStatus()
            // TODO: config
            s"open ${appConfig.slackApp}".!

          case SnoozeActivatedOnStartUp(ends) =>
            setSlackStatus(ends)

          case SnoozeDeactivatedOnStartUp =>
            unsetSlackStatus()

          case _ =>

        }
      } catch {
        case _: InterruptedException =>
      }

    }
  }

}
