package ru.maizy.dislk.app.ui

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.awt._
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.BlockingQueue
import javax.swing._
import scala.concurrent.{ ExecutionContext, Future, Promise }


class UIDispatcher(val eventQueue: BlockingQueue[UIEvent])(implicit ex: ExecutionContext) extends SwingUtils {

  import UIDispatcher._

  var dndInfoItem: Option[MenuItem] = None

  def start(): Unit = {
    val uiEventConsumer = new Runnable {
      override def run(): Unit = {
        while (true) {
          try {
            val event = eventQueue.take()
            event match {
              case Init =>
                initUi().failed.foreach { e =>
                  initAppError(s"Unable to init UI: $e")
                }

              case CriticalError(message) =>
                initAppError(message)

              case SetDnd(ends) =>
                val today = LocalDate.now()
                val formattedEnds = if (ends.toLocalDate.equals(today)) {
                  ends.format(THIS_DAY_FORMAT)
                } else {
                  ends.format(OTHER_DAY_FORMAT)
                }
                updateDndMode(s"In DND mode until $formattedEnds")

              case UnsetDnd =>
                updateDndMode(NOT_IN_DND_MODE)

              case Quit =>
                System.exit(0)

              case _ =>

            }
          } catch {
            case _: InterruptedException =>
          }
        }
      }
    }

    new Thread(uiEventConsumer, "ui-event-listener").start()
    ()
  }

  private def initUi(): Future[Unit] = {
    val promise = Promise[Unit]()
    if (!SystemTray.isSupported) {
      SwingUtilities.invokeLater(() => {
        val message = "SystemTray is not supported"
        eventQueue.add(CriticalError(message))
        promise.failure(new Exception(message))
      })
    } else {
      SwingUtilities.invokeLater(() => {
        buildUi()
        promise.success(())
      })
    }
    promise.future
  }

  private def initAppError(message: String): Future[Unit] = {
    val promise = Promise[Unit]()
    SwingUtilities.invokeLater(() => {
      JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE)
      System.exit(2)
      promise.success(()) // currently never run :)
    })
    promise.future
  }

  private def updateDndMode(status: String): Unit = {
    dndInfoItem.foreach { item =>
      item.setLabel(status)
    }
  }

  private def buildUi(): Unit = {
    val popup = new PopupMenu
    val dislkIconImage = createImage("icons/dnd.png", "tray icon")
    val trayIcon = new TrayIcon(dislkIconImage)
    val tray = SystemTray.getSystemTray

    val aboutItem: MenuItem = new MenuItem("About")
    val quitItem: MenuItem = new MenuItem("Quit")

    val dndInfoMenuItem: MenuItem = new MenuItem(NOT_IN_DND_MODE)
    dndInfoMenuItem.setEnabled(false)
    dndInfoItem = Some(dndInfoMenuItem)

    popup.add(aboutItem)
    popup.add(dndInfoMenuItem)
    popup.addSeparator()
    popup.add(quitItem)

    trayIcon.setPopupMenu(popup)
    try {
      tray.add(trayIcon)
    } catch {
      case _: AWTException =>
        eventQueue.add(CriticalError("TrayIcon could not be added."))
    }

    val icon = new ImageIcon(dislkIconImage)
    aboutItem.addActionListener { _ =>
      JOptionPane.showMessageDialog(
        null,
        "diSlack © 2017 Nikita Kovalev, maizy.ru\n" +
          "Icons made by Freepik from www.flaticon.com is licensed by CC 3.0 BY",
        "",
        JOptionPane.INFORMATION_MESSAGE,
        icon
      )
    }

    quitItem.addActionListener{ _ =>
      tray.remove(trayIcon)
      eventQueue.add(Quit)
      ()
    }
  }
}

object UIDispatcher {
  final private val NOT_IN_DND_MODE = "Not in DND mode"
  final private val THIS_DAY_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
  final private val OTHER_DAY_FORMAT = DateTimeFormatter.ofPattern("dd MMM HH:mm")
}
