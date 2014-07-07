package com.skligys.cardboardcreeper;

import android.os.SystemClock;

class Performance {
  private static final long FPS_INTERVAL = 5 * 1000;  // 5 seconds

  private long prevTimestamp = -1L;
  private long fpsStartTimestamp = -1L;
  private int frameCount;
  private float fps = 0.0f;
  private float minFps = Float.MAX_VALUE;
  private float maxFps = 0.0f;
  private boolean reset = false;

  private long physicsTimestamp = 0L;
  private long physicsSpent = 0L;
  private long chunkLoadTimestamp = 0L;
  private long chunkLoadSpent = 0L;
  private long renderTimestamp = 0L;
  private long renderSpent = 0L;

  /**
   * Returns interval in seconds since the last tick, if any.  If this is the first tick,
   * returns a negative number.  Updates internal data to compute FPS.
   */
  float tick() {
    if (reset) {
      throw new IllegalStateException();
    }

    long now = SystemClock.uptimeMillis();
    if (prevTimestamp < 0L) {
      prevTimestamp = now;
      fpsStartTimestamp = now;
      frameCount = 0;
      return -1.0f;
    }
    float secondsPassed = (now - prevTimestamp) * 0.001f;
    frameCount++;
    float momentaryFps = 1.0f / secondsPassed;
    maxFps = Math.max(maxFps, momentaryFps);
    minFps = Math.min(minFps, momentaryFps);

    // Update state for FPS calculation when enough data.
    if (now - fpsStartTimestamp >= FPS_INTERVAL) {
      fps = frameCount * 1000.0f / (now - fpsStartTimestamp);
      fpsStartTimestamp = now;
      reset = true;
    }

    prevTimestamp = now;
    return secondsPassed;
  }

  float fps() {
    return fps;
  }

  float minFps() {
    return minFps;
  }

  float maxFps() {
    return maxFps;
  }

  void startPhysics() {
    physicsTimestamp = SystemClock.uptimeMillis();
  }

  void endPhysics() {
    physicsSpent += SystemClock.uptimeMillis() - physicsTimestamp;
    physicsTimestamp = 0L;
  }

  public int physicsSpent() {
    int spent = (int) (physicsSpent / frameCount);
    physicsSpent = 0L;
    return spent;
  }

  void startChunkLoad() {
    chunkLoadTimestamp = SystemClock.uptimeMillis();
  }

  void endChunkLoad() {
    chunkLoadSpent += SystemClock.uptimeMillis() - chunkLoadTimestamp;
    chunkLoadTimestamp = 0L;
  }

  public int chunkLoadSpent() {
    int spent = (int) (chunkLoadSpent / frameCount);
    chunkLoadSpent = 0L;
    return spent;
  }

  void startRendering() {
    renderTimestamp = SystemClock.uptimeMillis();
  }

  void endRendering() {
    renderSpent += SystemClock.uptimeMillis() - renderTimestamp;
    renderTimestamp = 0L;
  }

  public int renderSpent() {
    int spent = (int) (renderSpent / frameCount);
    renderSpent = 0L;
    return spent;
  }

  /** After the frame is done with stats, reset computed values until more data is available. */
  void done() {
    if (reset) {
      reset = false;
      fps = 0.0f;
      minFps = Float.MAX_VALUE;
      maxFps = 0.0f;
      frameCount = 0;
    }
  }
}
