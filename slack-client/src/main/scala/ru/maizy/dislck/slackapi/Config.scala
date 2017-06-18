package ru.maizy.dislck.slackapi

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

class Config(val personalToken: String) {
  final val BASE_URL = "https://slack.com/api"
}

object Config {
  def apply(personalToken: String): Config = new Config(personalToken)
}
