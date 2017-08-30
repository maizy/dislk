package ru.maizy.dislk.app

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.ExecutionContext
import ru.maizy.dislk.app.watcher.{ Event, SnoozeWatcher }
import ru.maizy.dislk.macos.notification.MacOsNotification
import ru.maizy.dislk.slackapi
import ru.maizy.dislk.slackapi.Client

object AppLauncher extends App {

  if (!System.getProperty("os.name").toLowerCase.startsWith("mac os x")) {
    throw new Exception("only macOS supported for now")
  }
//   val ui = new UIDispatcher
//   ui.initUi()

  AppConfig.loadConfig() match {
    case Left(error) =>
      Console.err.println(error)
      System.exit(1)

    case Right(appConfig) if appConfig.personalToken.isDefined =>
      implicit val ec = ExecutionContext.global
      val slackClientConfig = slackapi.Config(personalToken = appConfig.personalToken.get)
      val slackClient = Client.withConfig(slackClientConfig)

      val slackEventQueue = new LinkedBlockingQueue[Event]

      val snoozeWatcher = new SnoozeWatcher(slackEventQueue, slackClient)
      val snoozeNotifications = new SnoozeNotifications(slackEventQueue, MacOsNotification, slackClient, appConfig)

      new Thread(snoozeWatcher, "snooze-watcher").start()
      new Thread(snoozeNotifications, "snooze-notifications").start()

    case Right(appConfig) if appConfig.personalToken.isEmpty =>
      Console.err.println(s"Personal token required. Add ${AppConfig.CONFIG_PATH}")
      System.exit(2)
  }

}
