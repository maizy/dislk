package ru.maizy.dislck.app

import scala.concurrent.ExecutionContext
import ru.maizy.dislck.slackapi

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

object AppLauncher extends App {

  AppParams.parse(args.toSeq) match {
    case Left(error) =>
      Console.err.println(error)
      System.exit(1)

    case Right(AppConfig(Some(personalToken))) =>
      implicit val ec = ExecutionContext.global
      val slackClientConfig = slackapi.Config(personalToken = personalToken)
      slackapi.Client.withConfig(slackClientConfig).dndInfo().foreach { info =>
        println(s"dnd info: $info")
        System.exit(0)
      }

    case _ =>
      Console.err.println("Unknown error")
      System.exit(2)
  }
}
