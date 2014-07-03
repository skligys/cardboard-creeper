package com.skligys.cardboardcreeper;

import android.os.SystemClock;

class TickInterval {
  private static final long FPS_INTERVAL = 5 * 1000;  // 5 seconds

  private long prevTimestamp = -1L;
  private long fpsStartTimestamp = -1L;
  private int frameCount;
  private float fps = 0.0f;

  /**
   * Returns interval in seconds since the last tick, if any.  If this is the first tick,
   * returns a negative number.  Updates internal data to compute FPS.
   */
  float tick() {
    long now = SystemClock.uptimeMillis();
    if (prevTimestamp < 0L) {
      prevTimestamp = now;
      fpsStartTimestamp = now;
      frameCount = 0;
      return -1.0f;
    }
    float result = (now - prevTimestamp) * 0.001f;
    frameCount++;

    // Update state for FPS calculation when enough data.
    if (now - fpsStartTimestamp >= FPS_INTERVAL) {
      fps = frameCount * 1000.0f / (now - fpsStartTimestamp);
      fpsStartTimestamp = now;
      frameCount = 0;
    }

    prevTimestamp = now;
    return result;
  }

  /** After each call, resets the computed FPS back to zero until more data is available. */
  float fps() {
    float result = fps;
    fps = 0.0f;
    return result;
  }
}
