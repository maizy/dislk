package ru.maizy.dislck.slackapi.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

trait JsonSupport[T] {
  def parse(value: String): T
  def parse(raw: Array[Byte]): T = parse(new String(raw))
}
