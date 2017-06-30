package ru.maizy.dislk.macos.notification;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/*
 * Copyright 2000-2015 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0
 *
 * Port of com.intellij.ui.mac.foundation.Foundation
 * from https://github.com/JetBrains/intellij-community
 * commit: 559143fd9468ec9d27d0749a1f4fe642a5e2b31c
 */

public class Foundation {

  private static final FoundationLibrary nativeFoundationLibrary;

  static {
    System.setProperty("jna.encoding", "UTF8");
    Map<String, Object> foundationOptions = new HashMap<>();
    nativeFoundationLibrary = Native.loadLibrary("Foundation", FoundationLibrary.class, foundationOptions);
  }

  public static ID getObjcClass(String className) {
    return nativeFoundationLibrary.objc_getClass(className);
  }

  public static Pointer createSelector(String s) {
    return nativeFoundationLibrary.sel_registerName(s);
  }

  public static ID invoke(final ID id, final Pointer selector, Object... args) {
    return nativeFoundationLibrary.objc_msgSend(id, selector, args);
  }

  public static ID invoke(final String cls, final String selector, Object... args) {
    return invoke(getObjcClass(cls), createSelector(selector), args);
  }

  public static ID invoke(final ID id, final String selector, Object... args) {
    return invoke(id, createSelector(selector), args);
  }

  public static ID nsString(String s) {
    // Use a byte[] rather than letting jna do the String -> char* marshalling itself.
    // Turns out about 10% quicker for long strings.
    try {
      if (s.isEmpty()) {
        return invoke("NSString", "string");
      }

      byte[] utf16Bytes = s.getBytes("UTF-16LE");
      return invoke(
          invoke(
              invoke("NSString", "alloc"),
              "initWithBytes:length:encoding:",
              utf16Bytes,
              utf16Bytes.length,
              convertCFEncodingToNS(FoundationLibrary.kCFStringEncodingUTF16LE)
          ),
          "autorelease"
      );
    }
    catch (UnsupportedEncodingException x) {
      throw new RuntimeException(x);
    }
  }

  private static long convertCFEncodingToNS(long cfEncoding) {
    // trim to C-type limits
    return nativeFoundationLibrary.CFStringConvertEncodingToNSStringEncoding(cfEncoding) & 0xffffffffffL;
  }

}
