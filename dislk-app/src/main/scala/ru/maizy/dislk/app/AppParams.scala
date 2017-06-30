package ru.maizy.dislk.app

import java.nio.file.{ Path, Paths }
import scala.io.Source
import pushka.annotation._
import pushka.json._
import pushka.PushkaException

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

// TODO: settings UI & system settings storage
@pushka @forceObject
case class AppConfig(
    @key("personal_token") personalToken: Option[String] = None
)

object AppConfig {
  final val CONFIG_PATH: Path = Paths.get(System.getProperty("user.home"), ".config", "dislk.json")

  def loadConfig(): Either[String, AppConfig] = {
    val file = CONFIG_PATH.toFile
    if (!file.exists) {
      Right(AppConfig())
    } else {
      val source = Source.fromFile(file)
      val content = try source.mkString finally source.close()
      try {
        Right(parseConfigJson(content))
      } catch {
        case e: PushkaException => Left(s"Unable to parse config: $e")
        case e: Throwable => Left(s"Unable to parse config, unknown error: $e")
      }
    }
  }

  @throws[PushkaException]
  private def parseConfigJson(content: String): AppConfig = {
    read[AppConfig](content)
  }

//  def saveConfig(config: AppConfig): Either[String, Unit] = ???
}
