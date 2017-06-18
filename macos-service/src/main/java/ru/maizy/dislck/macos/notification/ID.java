package ru.maizy.dislck.macos.notification;

/*
 * Copyright 2000-2013 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0
 *
 * Port of com.intellij.ui.mac.foundation.ID
 * from https://github.com/JetBrains/intellij-community
 * commit: 559143fd9468ec9d27d0749a1f4fe642a5e2b31c
 */

import com.sun.jna.NativeLong;

public class ID extends NativeLong {

  public ID() {
  }

  public ID(long peer) {
    super(peer);
  }

  public static final ID NIL = new ID(0);
}
