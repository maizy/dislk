package ru.maizy.dislck.app

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.concurrent.ExecutionContext
import ru.maizy.dislck.app.ui.UIDispatcher
import ru.maizy.dislck.slackapi

object AppLauncher extends App {

  if (!System.getProperty("os.name").toLowerCase.startsWith("mac os x")) {
    throw new Exception("only macOS supported for now")
  }

  val ui = new UIDispatcher
  ui.initUi()

  AppParams.parse(args.toSeq) match {
    case Left(error) =>
      Console.err.println(error)
      System.exit(1)

    case Right(AppConfig(Some(personalToken))) =>
      implicit val ec = ExecutionContext.global
      val slackClientConfig = slackapi.Config(personalToken = personalToken)
      slackapi.Client.withConfig(slackClientConfig).dndInfo().foreach { info =>
        println(s"dnd info: $info")
      }

    case _ =>
      Console.err.println("Unknown error")
      System.exit(2)
  }

}
