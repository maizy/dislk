package ru.maizy.dislk.slackapi.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

trait JsonReadSupport[T] {
  def parseJson(value: String): T
  def parseJson(raw: Array[Byte]): T = parseJson(new String(raw))
}
