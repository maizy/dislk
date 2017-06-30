package ru.maizy.dislk.app.watcher

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.util.concurrent.{ BlockingQueue, Executors, TimeUnit }
import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration.{ Duration, DurationInt }
import ru.maizy.dislk.slackapi.Client

class SnoozeWatcher(val queue: BlockingQueue[Event], slackClient: Client)(implicit ec: ExecutionContext)
  extends Runnable {

  private var snoozed = false
  private val timeout: Duration = 3.seconds
  private val scheduler = Executors.newScheduledThreadPool(1)

  override def run(): Unit = {
    slackClient.dndInfo() map { initInfo =>
      snoozed = initInfo.snoozeEnabled

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
  }

  private def check()(implicit ec: ExecutionContext): Future[Seq[Event]] = {
    slackClient.dndInfo().map { dndInfo =>
      val events = mutable.ListBuffer.empty[Event]
      if (!snoozed && dndInfo.snoozeEnabled) {
        snoozed = true
        dndInfo.snoozeEndtime match {
          case Some(ends) => events += SnoozeBegin(ends)
          // TODO: typesafe.logging
          case _ => Console.err.println("Snooze endtime unknown, skip")
        }
      } else if(snoozed && !dndInfo.snoozeEnabled) {
        snoozed = false
        events += SnoozeFinish
      }
      events.toList
    }
  }
}
