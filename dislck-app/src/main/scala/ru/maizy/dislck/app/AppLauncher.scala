package ru.maizy.dislck.app

import ru.maizy.dislck.macos.notification.OsxNotification

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

object AppLauncher extends App {

  OsxNotification.notify("test", "Some description")

  Thread.sleep(1 * 1000)

  OsxNotification.notify("test 2", "Привет")

  Thread.sleep(1 * 1000)

  OsxNotification.notify("test 3", "Пока")

//
//
//  AppParams.parse(args.toSeq) match {
//    case Left(error) =>
//      Console.err.println(error)
//      System.exit(1)
//
//    case Right(AppConfig(Some(personalToken))) =>
//      implicit val ec = ExecutionContext.global
//      val slackClientConfig = slackapi.Config(personalToken = personalToken)
//      slackapi.Client.withConfig(slackClientConfig).dndInfo().foreach { info =>
//        println(s"dnd info: $info")
//        System.exit(0)
//      }
//
//    case _ =>
//      Console.err.println("Unknown error")
//      System.exit(2)
//  }
}
