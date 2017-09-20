package ru.maizy.dislk.app

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.ExecutionContext
import com.typesafe.scalalogging.LazyLogging
import ru.maizy.dislk.app.watcher.{ Event, SnoozeWatcher }
import ru.maizy.dislk.macos.notification.MacOsNotification
import ru.maizy.dislk.slackapi
import ru.maizy.dislk.slackapi.Client

object AppLauncher extends App with LazyLogging {

  if (!System.getProperty("os.name").toLowerCase.startsWith("mac os x")) {
    throw new Exception("only macOS supported for now")
  }

  logger.info("Launch app")
  implicit val ec = ExecutionContext.global

  val uiEventQueue = new LinkedBlockingQueue[ui.UIEvent]
  val uiDispatcher = new ui.UIDispatcher(uiEventQueue)
  uiDispatcher.start()

  AppConfig.loadConfig() match {
    case Left(error) =>
      val errorMsg = s"Config load error: $error"
      logger.error(errorMsg)
      uiEventQueue.put(ui.CriticalError(error))

    case Right(appConfig) if appConfig.personalToken.isDefined =>
      uiEventQueue.put(ui.Init)

      val slackClientConfig = slackapi.Config(personalToken = appConfig.personalToken.get)
      val slackClient = Client.withConfig(slackClientConfig)
      val slackEventQueue = new LinkedBlockingQueue[Event]

      val snoozeWatcher = new SnoozeWatcher(slackEventQueue, slackClient)
      new Thread(snoozeWatcher, "snooze-watcher").start()

      val broker = new AppBroker(
        slackEventQueue,
        uiEventQueue,
        MacOsNotification,
        slackClient,
        appConfig
      )
      new Thread(broker, "broker").start()

    case Right(appConfig) if appConfig.personalToken.isEmpty =>
      logger.error("Personal token required")
      uiEventQueue.put(ui.CriticalError(
        s"Personal token required. Add json config to ${AppConfig.CONFIG_PATH}\n" +
        " (see https://github.com/maizy/dislk#setup for details)"
      ))
  }

}
