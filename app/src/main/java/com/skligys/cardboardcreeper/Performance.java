package com.skligys.cardboardcreeper;

import android.os.SystemClock;

class Performance {
  private static final long FPS_INTERVAL = 5 * 1000;  // 5 seconds
  static final float[] FPS_THRESHOLDS = { 10.0f, 20.0f, 30.0f, 40.0f, 50.0f, 60.0f };

  private long prevFrameTimestamp = -1L;
  private long currFrameTimestamp;
  private long fpsStartTimestamp = -1L;
  private int frameCount = 0;
  private float minFps = Float.MAX_VALUE;
  private float maxFps = 0.0f;
  private float[] fpsAboveThresholdCounts = new float[FPS_THRESHOLDS.length];

  private long physicsStartTimestamp = 0L;
  private long physicsSpent = 0L;

  private long renderStartTimestamp = 0L;
  private long renderSpent = 0L;

  private final Object chunkLoadLock = new Object();
  private int chunkLoadCount = 0;
  private long chunkLoadStartTimestamp = 0L;
  private long chunkLoadSpent = 0L;

  private final Object chunkUnloadLock = new Object();
  private int chunkUnloadCount = 0;
  private long chunkUnloadTimestamp = 0L;
  private long chunkUnloadSpent = 0L;

  /**
   * Returns interval in seconds since the last tick, if any.  If this is the first tick,
   * returns a negative number.  Updates internal data to compute FPS.
   */
  float startFrame() {
    currFrameTimestamp = SystemClock.uptimeMillis();
    if (prevFrameTimestamp < 0L) {
      prevFrameTimestamp = currFrameTimestamp;
      fpsStartTimestamp = currFrameTimestamp;
      return -1.0f;
    }

    float secondsPassed = (currFrameTimestamp - prevFrameTimestamp) * 0.001f;
    frameCount++;
    float momentaryFps = 1.0f / secondsPassed;
    maxFps = Math.max(maxFps, momentaryFps);
    minFps = Math.min(minFps, momentaryFps);

    for (int i = 0; i < fpsAboveThresholdCounts.length; ++i) {
      if (momentaryFps >= FPS_THRESHOLDS[i]) {
        ++fpsAboveThresholdCounts[i];
      }
    }

    return secondsPassed;
  }

  boolean hasStats() {
    return currFrameTimestamp - fpsStartTimestamp >= FPS_INTERVAL;
  }

  float fps() {
    return frameCount * 1000.0f / (currFrameTimestamp - fpsStartTimestamp);
  }

  float minFps() {
    return minFps;
  }

  float maxFps() {
    return maxFps;
  }

  /**
   * Returns percentages of frames with momentary FPS values at or above each of the
   * {@code }FPS_THRESHOLDS}.
   */
  float[] fpsPercentages() {
    float[] result = new float[fpsAboveThresholdCounts.length];
    for (int i = 0; i < result.length; ++i) {
      result[i] = fpsAboveThresholdCounts[i] * 100.0f / frameCount;
    }
    return result;
  }

  void startPhysics() {
    physicsStartTimestamp = SystemClock.uptimeMillis();
  }

  void endPhysics() {
    physicsSpent += SystemClock.uptimeMillis() - physicsStartTimestamp;
    physicsStartTimestamp = 0L;
  }

  public int physicsSpent() {
    return (int) (physicsSpent / frameCount);
  }

  void startRendering() {
    renderStartTimestamp = SystemClock.uptimeMillis();
  }

  void endRendering() {
    renderSpent += SystemClock.uptimeMillis() - renderStartTimestamp;
    renderStartTimestamp = 0L;
  }

  public int renderSpent() {
    return (int) (renderSpent / frameCount);
  }

  void endFrame() {
    prevFrameTimestamp = currFrameTimestamp;
    // After the frame is done with stats, reset computed values until more data is available.
    if (currFrameTimestamp - fpsStartTimestamp >= FPS_INTERVAL) {
      fpsStartTimestamp = currFrameTimestamp;
      frameCount = 0;
      maxFps = 0.0f;
      minFps = Float.MAX_VALUE;
      for (int i = 0; i < fpsAboveThresholdCounts.length; ++i) {
        fpsAboveThresholdCounts[i] = 0;
      }
      physicsSpent = 0L;
      renderSpent = 0L;
      chunkLoadCount = 0;
      chunkLoadSpent = 0L;
      chunkUnloadCount = 0;
      chunkUnloadSpent = 0L;
    }
  }

  void startChunkLoad() {
    synchronized (chunkLoadLock) {
      chunkLoadStartTimestamp = SystemClock.uptimeMillis();
    }
  }

  void endChunkLoad() {
    synchronized (chunkLoadLock) {
      ++chunkLoadCount;
      chunkLoadSpent += SystemClock.uptimeMillis() - chunkLoadStartTimestamp;
      chunkLoadStartTimestamp = 0L;
    }
  }

  public int chunkLoadCount() {
    synchronized (chunkLoadLock) {
      return chunkLoadCount;
    }
  }

  public int chunkLoadSpent() {
    synchronized (chunkLoadLock) {
      return chunkLoadCount != 0 ? (int) (chunkLoadSpent / chunkLoadCount) : 0;
    }
  }

  void startChunkUnload() {
    synchronized (chunkUnloadLock) {
      chunkUnloadTimestamp = SystemClock.uptimeMillis();
    }
  }

  void endChunkUnload() {
    synchronized (chunkUnloadLock) {
      ++chunkUnloadCount;
      chunkUnloadSpent += SystemClock.uptimeMillis() - chunkUnloadTimestamp;
      chunkUnloadTimestamp = 0L;
    }
  }

  public int chunkUnloadCount() {
    synchronized (chunkUnloadLock) {
      return chunkUnloadCount;
    }
  }

  public int chunkUnloadSpent() {
    synchronized (chunkUnloadLock) {
      return chunkUnloadCount != 0 ? (int) (chunkUnloadSpent / chunkUnloadCount) : 0;
    }
  }
}
