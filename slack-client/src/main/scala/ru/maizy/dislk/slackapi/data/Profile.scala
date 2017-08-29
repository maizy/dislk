package ru.maizy.dislk.slackapi.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import pushka.annotation._
import pushka.json._

@pushka case class Profile(
    @key("status_text") statusText: String,
    @key("status_emoji") statusEmoji: String
) extends JsonWriteSupport[Profile] {
  override def toJson: String = pushka.json.write(this)
}

// example json
//
//    {
//        "status_text": "riding a train",
//        "status_emoji": ":mountain_railway:"
//    }
