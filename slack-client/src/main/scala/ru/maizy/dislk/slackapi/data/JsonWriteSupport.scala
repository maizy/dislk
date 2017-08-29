package ru.maizy.dislk.slackapi.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

trait JsonWriteSupport[T] {
  def toJson: String
}
