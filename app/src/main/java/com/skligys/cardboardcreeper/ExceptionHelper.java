package com.skligys.cardboardcreeper;

class ExceptionHelper {
  private ExceptionHelper() {}  // No instantiation.

  static void fail(String format, Object... params) {
    throw new RuntimeException(String.format(format, params));
  }

  static void failIllegalArgument(String format, Object... params) {
    throw new IllegalArgumentException(String.format(format, params));
  }
}
