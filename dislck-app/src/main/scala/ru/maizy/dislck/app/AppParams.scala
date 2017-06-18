package ru.maizy.dislck.app

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

// TODO: settings UI & system settings storage
case class AppConfig(personalToken: Option[String] = None)

object AppParams extends {

  private val parser = new scopt.OptionParser[AppConfig]("dislck") {
    opt[String]('t', "token")
      .action{(value, opts) => opts.copy(personalToken = Some(value))}
      .required()
      .text("Personal token from https://api.slack.com/custom-integrations/legacy-tokens")
  }

  def parse(args: Seq[String]): Either[String, AppConfig] = {
    parser.parse(args, AppConfig()) match {
      case Some(c) if c.personalToken.isDefined => Right(c)
      case _ => Left("Wrong app config")
    }
  }

}
