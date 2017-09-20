package ru.maizy.dislk.app

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import java.util.{ Date, IllegalFormatConversionException }
import java.util.concurrent.BlockingQueue
import scala.sys.process._
import com.typesafe.scalalogging.LazyLogging
import ru.maizy.dislk.app.ui.{ SetDnd, UIEvent, UnsetDnd }
import ru.maizy.dislk.app.watcher.{ Event, SnoozeActivatedOnStartUp, SnoozeBegin, SnoozeDeactivatedOnStartUp }
import ru.maizy.dislk.app.watcher.{ SnoozeEndtimeChanged, SnoozeFinish }
import ru.maizy.dislk.macos.notification.MacOsNotification
import ru.maizy.dislk.slackapi


class AppBroker(
    slackEventQueue: BlockingQueue[Event],
    uiEventQueue: BlockingQueue[UIEvent],
    osxNotifier: MacOsNotification.type,
    slackClient: slackapi.Client,
    appConfig: AppConfig
) extends Runnable with LazyLogging {

  private def setSlackStatus(ends: ZonedDateTime): Unit = {
    appConfig.autoSetStatus foreach { autoSetConfig =>
      val endDate = Date.from(ends.toInstant)
      val text = try {
        String.format(autoSetConfig.text, endDate)
      } catch {
        case e: IllegalFormatConversionException =>
          logger.warn("Bad status text format", e)
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
            uiEventQueue.put(SetDnd(ends))

          case SnoozeFinish =>
            osxNotifier.notify("snooze ended", "time to read Slack")
            unsetSlackStatus()
            Seq("open", appConfig.slackApp).!!
            uiEventQueue.put(UnsetDnd)

          case SnoozeActivatedOnStartUp(ends) =>
            setSlackStatus(ends)
            uiEventQueue.put(SetDnd(ends))

          case SnoozeEndtimeChanged(newEnds) =>
            setSlackStatus(newEnds)
            uiEventQueue.put(SetDnd(newEnds))

          case SnoozeDeactivatedOnStartUp =>
            unsetSlackStatus()
            uiEventQueue.put(UnsetDnd)

          case other => logger.warn(s"Unknown slack event: $other")
        }
      } catch {
        case _: InterruptedException =>
      }

    }
  }

}
