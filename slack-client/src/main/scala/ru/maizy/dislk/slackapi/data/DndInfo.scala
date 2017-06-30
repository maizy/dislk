package ru.maizy.dislk.slackapi.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.time.format.DateTimeFormatter
import java.time.{ Instant, ZoneId, ZonedDateTime }
import pushka.annotation._
import pushka.json._

class Timestamp(val timestamp: Int) extends AnyVal

@pushka case class DndInfo(
    @key("snooze_enabled") snoozeEnabled: Boolean,
    @key("snooze_endtime") snoozeEndtimeTimestamp: Option[Long] = None
) {

  def snoozeEndtime: Option[ZonedDateTime] = snoozeEndtimeTimestamp.map { time =>
    ZonedDateTime.ofInstant(
      Instant.ofEpochSecond(time),
      ZoneId.systemDefault()
    )
  }

  override def toString: String =
    s"DndInfo(snooze: enabled=$snoozeEnabled, " +
    s"end=${snoozeEndtime.map(_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)).getOrElse("-")}})"
}

object DndInfo extends JsonSupport[DndInfo] {
  override def parse(value: String): DndInfo = read[DndInfo](value)
}


// example json
//
//  {
//      "ok": true,
//      "dnd_enabled": true,
//      "next_dnd_start_ts": 1450416600,
//      "next_dnd_end_ts": 1450452600,
//      "snooze_enabled": true,
//      "snooze_endtime": 1450416600,
//      "snooze_remaining": 1196
//  }
