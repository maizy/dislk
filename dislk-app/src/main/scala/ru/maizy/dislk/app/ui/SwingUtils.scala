package ru.maizy.dislk.app.ui

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.awt._
import java.net.URL
import javax.swing._


trait SwingUtils {
  protected def createImage(path: String, description: String): Image = {
    val imageURL: URL = getClass.getClassLoader.getResource(path)
    if (imageURL == null) {
      throw new Exception(s"Resource not found: $path")
    } else {
      new ImageIcon(imageURL, description).getImage
    }
  }
}
