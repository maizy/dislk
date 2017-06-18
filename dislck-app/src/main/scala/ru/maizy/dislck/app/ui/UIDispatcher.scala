package ru.maizy.dislck.app.ui

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.awt._
import java.net.URL
import javax.swing._
import scala.concurrent.{ Future, Promise }


class UIDispatcher {

  def initUi(): Future[Unit] = {
    val promise = Promise[Unit]()
    if (!SystemTray.isSupported) {
      // FIXME tmp
      promise.failure(new Exception("SystemTray is not supported"))
    } else {
      SwingUtilities.invokeLater(() => {
        buildUi()
        promise.success(())
      })
    }
    promise.future
  }

  private def buildUi(): Unit = {
    val popup = new PopupMenu
    val dislckIconImage = createImage("icons/dnd.png", "tray icon")
    val trayIcon = new TrayIcon(dislckIconImage)
    val tray = SystemTray.getSystemTray

    // Create a popup menu components
    val aboutItem: MenuItem = new MenuItem("About")
    val quitItem: MenuItem = new MenuItem("Quit")

    // Add components to popup menu
    popup.add(aboutItem)
    popup.addSeparator()
    popup.add(quitItem)
    trayIcon.setPopupMenu(popup)
    try {
      tray.add(trayIcon)
    } catch {
      case _: AWTException =>
        // FIXME exit here
        throw new Exception("TrayIcon could not be added.")
    }

    val icon = new ImageIcon(dislckIconImage)
    aboutItem.addActionListener { _ =>
      // FIXME not working, should we use other thread?
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
      // FIXME tmp
      tray.remove(trayIcon)
      System.exit(0)
    }
  }

  private def createImage(path: String, description: String): Image = {
    val imageURL: URL = getClass.getClassLoader.getResource(path)
    if (imageURL == null) {
      throw new Exception(s"Resource not found: $path")
    } else {
      new ImageIcon(imageURL, description).getImage
    }
  }
}
