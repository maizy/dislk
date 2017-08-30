package ru.maizy.dislk.app.watcher

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime

sealed trait Event
sealed trait StartUpEvent extends Event

case class SnoozeActivatedOnStartUp(ends: ZonedDateTime) extends Event with StartUpEvent
case object SnoozeDeactivatedOnStartUp extends Event with StartUpEvent
case class SnoozeEndtimeChanged(newEnd: ZonedDateTime) extends Event
case object SnoozeDeactivatedByUser extends Event
case class SnoozeBegin(ends: ZonedDateTime) extends Event
case object SnoozeFinish extends Event
