package ru.maizy.dislk.app

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.concurrent.ExecutionContext
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

    case Right(AppConfig(Some(personalToken))) =>
      implicit val ec = ExecutionContext.global
      val slackClientConfig = slackapi.Config(personalToken = personalToken)
      val slackClient = Client.withConfig(slackClientConfig)

      val queue = new LinkedBlockingQueue[Event]

      val snoozeWatcher = new SnoozeWatcher(queue, slackClient)
      val snoozeNotifications = new SnoozeNotifications(queue, MacOsNotification)

      new Thread(snoozeWatcher, "snooze-watcher").start()
      new Thread(snoozeNotifications, "snooze-notifications").start()

    case Right(AppConfig(None)) =>
      Console.err.println(s"Personal token required. Add ${AppConfig.CONFIG_PATH}")
      System.exit(2)
  }

}
