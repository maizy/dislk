package ru.maizy.dislk.app.watcher

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime

sealed trait Event
case class SnoozeBegin(ends: ZonedDateTime) extends Event
case object SnoozeFinish extends Event
