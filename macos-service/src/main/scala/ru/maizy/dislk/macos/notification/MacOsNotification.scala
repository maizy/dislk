package ru.maizy.dislk.macos.notification

/**
 * Copyright 2000-2017 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0
 *
 * Port of IntellijIDEA com.intellij.ui.MountainLionNotifications
 * commit: 559143fd9468ec9d27d0749a1f4fe642a5e2b31c
 */

object MacOsNotification {

  def notify(title: String, description: String): Unit = {

    // StringUtil.stripHtml(title, true).replace("%", "%%")
    def strip(v: String) = v.replace("%", "%%")

    val titleStripped = strip(title)
    val descriptionStripped = strip(description)

    val notification = Foundation.invoke(Foundation.getObjcClass("NSUserNotification"), "new")
    Foundation.invoke(notification, "setTitle:", Foundation.nsString(titleStripped))
    Foundation.invoke(
      notification,
      "setInformativeText:",
      Foundation.nsString(descriptionStripped))

    val center = Foundation.invoke(
      Foundation.getObjcClass("NSUserNotificationCenter"),
      "defaultUserNotificationCenter"
    )
    Foundation.invoke(center, "deliverNotification:", notification)
    ()
  }

}
