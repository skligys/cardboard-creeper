package com.skligys.cardboardcreeper;

import android.opengl.Matrix;

class Eye {
  /**
   * Eye position: initially, the eye is located at (0, 0) height 2.1 (feet to eye 1.6 +
   * 0.5 displacement from block the feet are on).
   */
  private final Point3 position = new Point3(0.0f, 2.1f, 0.0f);

  /**
   * Eye rotation.  x is rotation in degrees in the horizontal plane starting from negative z axis
   * counter-clockwise.  Unbounded but periodic with period 360.0f.  y is rotation in degrees
   * from the horizontal plane up.  The range is from -90.0f (looking straight down) to 90.0f
   * (looking straight up).
   */
  private Point2 rotation = new Point2(0.0f, 0.0f);

  private final float[] viewMatrix = new float[16];

  Eye() {
    computeViewMatrix();
  }

  float[] viewMatrix() {
    return viewMatrix;
  }

  private static final float ROTATION_SPEED = 0.15f;

  public void rotate(float dx, float dy) {
    float newX = rotation.x + dx * ROTATION_SPEED;
    float newY = clamp(rotation.y + dy * ROTATION_SPEED, -90.0f, 90.0f);
    rotation = new Point2(newX, newY);

    computeViewMatrix();
  }

  private static float clamp(float value, float min, float max) {
    return Math.max(min, Math.min(max, value));
  }

//  private static final float PI = 3.14159265358979323846f;
//  private static final float DEGREES_TO_RADIANS = 180.0f / PI;

  private void computeViewMatrix() {
    Matrix.setIdentityM(viewMatrix, 0);

    Matrix.rotateM(viewMatrix, 0, rotation.x, 0.0f, 1.0f, 0.0f);

// TODO: Figure out vertical eye rotation.  The following bobs up and down violently.
//    float xRadians = rotation.x * DEGREES_TO_RADIANS;
//    Matrix.rotateM(viewMatrix, 0, -rotation.y,
//        (float) Math.cos(xRadians), 0.0f, (float) Math.sin(xRadians));

    Matrix.translateM(viewMatrix, 0, -position.x, -position.y, -position.z);
  }
}
