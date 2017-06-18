package ru.maizy.dislck.slackapi.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */


import pushka.annotation._
import pushka.json._

@pushka case class ErrorDto(@key("error") errorCode: String, ok: Boolean)

object ErrorDto extends JsonSupport[ErrorDto] {
  def parse(value: String): ErrorDto = read[ErrorDto](value)
}

// example json
//  {
//      "error": "invalid_auth",
//      "ok": false
//  }
