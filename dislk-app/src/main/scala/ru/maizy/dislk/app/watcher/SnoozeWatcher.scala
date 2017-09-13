package ru.maizy.dislk.app.watcher

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import java.util.concurrent.{ BlockingQueue, Executors, TimeUnit }
import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration.{ Duration, DurationInt }
import ru.maizy.dislk.slackapi.Client

class SnoozeWatcher(val queue: BlockingQueue[Event], slackClient: Client)(implicit ec: ExecutionContext)
  extends Runnable {

  private var lastSnoozed = false
  private var mayBeLastEndTime: Option[ZonedDateTime] = None
  private val timeout: Duration = 3.seconds
  private val scheduler = Executors.newScheduledThreadPool(1)

  override def run(): Unit = {
    slackClient.dndInfo() map { initInfo =>
      lastSnoozed = initInfo.snoozeEnabled
      if (initInfo.snoozeEnabled) {
        initInfo.snoozeEndtime.foreach { ends =>
          queue.add(SnoozeActivatedOnStartUp(ends))
          mayBeLastEndTime = Some(ends)
        }
      } else {
        queue.add(SnoozeDeactivatedOnStartUp)
      }

      def reschedule(): Unit = {
        scheduler.schedule(
          new Runnable {
            override def run(): Unit = {
              Console.err.println("check snooze status in slack")
              check()
                .map { events =>
                  if (events.nonEmpty) {
                    events.foreach(queue.add)
                  }
                  reschedule()
                }
                .failed.foreach { e =>
                  Console.err.println(s"Error on updating snooze status: $e")
                  reschedule()
                }
              ()
            }
          },
          timeout.toMillis,
          TimeUnit.MILLISECONDS
        )
        ()
      }

      reschedule()
    }
    ()
  }

  private def check()(implicit ec: ExecutionContext): Future[Seq[Event]] = {
    slackClient.dndInfo().map { dndInfo =>
      val events = mutable.ListBuffer.empty[Event]
      if (dndInfo.snoozeEnabled) {
        dndInfo.snoozeEndtime match {
          case Some(ends) =>
            if (!lastSnoozed) {
              lastSnoozed = true
              mayBeLastEndTime = Some(ends)
              events += SnoozeBegin(ends)
            } else {
              mayBeLastEndTime foreach { lastEndTime =>
                if (!lastEndTime.equals(ends)) {
                  events += SnoozeEndtimeChanged(ends)
                  mayBeLastEndTime = Some(ends)
                }
              }
            }
          // TODO: typesafe.logging
          case _ => Console.err.println("Snooze endtime unknown, skip")
        }
      } else if(lastSnoozed && !dndInfo.snoozeEnabled) {
        lastSnoozed = false
        mayBeLastEndTime = None
        events += SnoozeFinish
      }
      events.toList
    }
  }
}
