package com.skligys.cardboardcreeper;

import android.content.res.Resources;
import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/** Holds blocks. */
class World {
  // A hill on the left.
  private static final short[] BLOCKS = {
      -6, 0, 0,
      -5, 0, 0,
      -4, 0, 0,
      -3, 0, 0,
      -2, 0, 0,
      -1, 0, 0,
      0, 0, 0,
      1, 0, 0,
      2, 0, 0,
      3, 0, 0,
      4, 0, 0,
      5, 0, 0,
      6, 0, 0,

      -6, 0, -1,
      -5, 0, -1,
      -4, 0, -1,
      -3, 0, -1,
      -2, 0, -1,
      -1, 0, -1,
      0, 0, -1,
      1, 0, -1,
      2, 0, -1,
      3, 0, -1,
      4, 0, -1,
      5, 0, -1,
      6, 0, -1,

      -6, 0, -2,
      -5, 0, -2,
      -4, 0, -2,
      -3, 0, -2,
      -2, 0, -2,
      -1, 0, -2,
      0, 0, -2,
      1, 0, -2,
      2, 0, -2,
      3, 0, -2,
      4, 0, -2,
      5, 0, -2,
      6, 0, -2,

      -6, 0, -3,
      -5, 0, -3,
      -4, 0, -3,
      -3, 0, -3,
      -2, 0, -3,
      -1, 0, -3,
      0, 0, -3,
      1, 0, -3,
      2, 0, -3,
      3, 0, -3,
      4, 0, -3,
      5, 0, -3,
      6, 0, -3,

      -7, 1, -4,
      -6, 1, -4,
      -5, 1, -4,
      -4, 1, -4,
      -3, 1, -4,
      -2, 1, -4,
      -1, 1, -4,
      0, 1, -4,
      1, 0, -4,
      2, 0, -4,
      3, 0, -4,
      4, 0, -4,
      5, 0, -4,
      6, 0, -4,

      -7, 2, -5,
      -6, 2, -5,
      -5, 2, -5,
      -4, 2, -5,
      -3, 2, -5,
      -2, 2, -5,
      -1, 2, -5,
      0, 2, -5,
      1, 1, -5,
      2, 0, -5,
      3, 0, -5,
      4, 0, -5,
      5, 0, -5,
      6, 0, -5,

      -9, 3, -6,
      -8, 3, -6,
      -7, 3, -6,
      -6, 3, -6,
      -5, 3, -6,
      -4, 3, -6,
      -3, 3, -6,
      -2, 3, -6,
      -1, 3, -6,
      0, 2, -6,
      1, 1, -6,
      2, 0, -6,
      3, 0, -6,
      4, 0, -6,
      5, 0, -6,
      6, 0, -6,

      -11, 4, -7,
      -10, 4, -7,
      -9, 4, -7,
      -8, 4, -7,
      -7, 4, -7,
      -6, 4, -7,
      -5, 4, -7,
      -4, 4, -7,
      -3, 4, -7,
      -2, 4, -7,
      -1, 3, -7,
      0, 2, -7,
      1, 1, -7,
      2, 0, -7,
      3, 0, -7,
      4, 0, -7,
      5, 0, -7,
      6, 0, -7,
  };

  private final Cube cube;
  /** Center points of blocks. */
  private final List<Point3> blocks = new ArrayList<Point3>();
  private final Eye eye = new Eye();
  private final float[] viewProjectionMatrix = new float[16];
  private final TickInterval tickInterval;

  World() {
    cube = new Cube();

    if (BLOCKS.length % 3 != 0) {
      ExceptionHelper.failIllegalArgument(
          "Blocks should contain triples of coords but length was: %d", BLOCKS.length);
    }
    for (int i = 0; i < BLOCKS.length; i += 3) {
      blocks.add(new Point3(BLOCKS[i], BLOCKS[i + 1], BLOCKS[i + 2]));
    }
    tickInterval = new TickInterval();
  }

  public void surfaceCreated(Resources resources) {
    cube.surfaceCreated(resources);
  }

  public void draw(float[] projectionMatrix) {
    float dt = tickInterval.tick();
    float fps = tickInterval.fps();
    if (fps > 0.0f) {
      Log.i("World", "FPS: " + fps);
    }

    Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, eye.viewMatrix(), 0);
    for (Point3 block : blocks) {
      cube.draw(viewProjectionMatrix, block.x, block.y, block.z);
    }
  }

  public void drag(float dx, float dy) {
    eye.rotate(dx, dy);
  }
}
