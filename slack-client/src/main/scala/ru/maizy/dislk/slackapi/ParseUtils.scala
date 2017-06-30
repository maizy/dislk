package ru.maizy.dislk.slackapi

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.util.{ Failure, Success, Try }
import pushka.PushkaException

trait ParseUtils {

  @throws[ClientError]("if parsing throws PushkaException")
  def wrapParseException[T](parseF: => T): T = {
    Try(parseF) match {
      case Success(r) => r
      case Failure(e@PushkaException(error)) => throw ClientError(s"Unable to parse response json: $error", Some(e))
      case Failure(e) => throw e
    }
  }
}
