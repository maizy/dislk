package ru.maizy.dislk.app.ui

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime

sealed trait UIEvent

case class SetDnd(ends: ZonedDateTime) extends UIEvent
case object Init extends UIEvent
case class CriticalError(message: String) extends UIEvent
case object UnsetDnd extends UIEvent
case object Quit extends UIEvent
