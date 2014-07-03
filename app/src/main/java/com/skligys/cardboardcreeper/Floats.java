package com.skligys.cardboardcreeper;

class Floats {
  private Floats() {}  // No instantiation.

  private static final float PI = 3.14159265358979323846f;
  private static final float DEGREES_TO_RADIANS = PI / 180.0f;

  static float sin(float degrees) {
    return (float) Math.sin(degrees * DEGREES_TO_RADIANS);
  }

  static float cos(float degrees) {
    return (float) Math.cos(degrees * DEGREES_TO_RADIANS);
  }
}
