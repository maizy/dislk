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
import com.typesafe.scalalogging.LazyLogging
import ru.maizy.dislk.slackapi.Client

class SnoozeWatcher(val queue: BlockingQueue[Event], slackClient: Client)(implicit ec: ExecutionContext)
  extends Runnable
  with LazyLogging
{

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
              logger.debug("check snooze status in slack")
              check()
                .map { events =>
                  if (events.nonEmpty) {
                    events.foreach(queue.add)
                  }
                  reschedule()
                }
                .failed.foreach { e =>
                  logger.warn(s"Error on updating snooze status: $e")
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
      logger.debug(s"DND info: $dndInfo")
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
          case _ => logger.warn("Snooze endtime unknown, skip")
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
