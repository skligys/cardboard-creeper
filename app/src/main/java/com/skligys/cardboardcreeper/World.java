package com.skligys.cardboardcreeper;

import android.content.res.Resources;
import android.opengl.Matrix;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

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

  /** OpenGL support for drawing grass blocks. */
  private final Cube cube = new Cube();
  /** Center points of blocks. */
  private final Set<Point3Int> blocks = new HashSet<Point3Int>();
  private final TickInterval tickInterval = new TickInterval();
  private final Steve steve = new Steve();
  private final Physics physics = new Physics();
  /** Pre-allocated temporary matrix. */
  private final float[] viewProjectionMatrix = new float[16];

  World() {
    if (BLOCKS.length % 3 != 0) {
      Exceptions.failIllegalArgument(
          "Blocks should contain triples of coordinates but length was: %d", BLOCKS.length);
    }
    for (int i = 0; i < BLOCKS.length; i += 3) {
      blocks.add(new Point3Int(BLOCKS[i], BLOCKS[i + 1], BLOCKS[i + 2]));
    }
  }

  void surfaceCreated(Resources resources) {
    cube.surfaceCreated(resources);
  }

  void draw(float[] projectionMatrix) {
    // This has to be first to have up to date tick timestamp for FPS computation.
    float dt = Math.min(tickInterval.tick(), 0.2f);
    float fps = tickInterval.fps();
    if (fps > 0.0f) {
      Log.i("World", "FPS: " + fps);
    }

    physics.updateEyePosition(steve, dt, blocks);

    Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, steve.viewMatrix(), 0);
    for (Point3Int block : blocks) {
      cube.draw(viewProjectionMatrix, block.x, block.y, block.z);
    }
  }

  void drag(float dx, float dy) {
    steve.rotate(dx, dy);
  }

  void walk(boolean start) {
    steve.walk(start);
  }
}
