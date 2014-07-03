package com.skligys.cardboardcreeper;

import android.opengl.Matrix;

class Eye {
  /**
   * Eye position: initially, the eye is located at (0, 0) height 2.1 (feet to eye 1.6 +
   * 0.5 displacement from block the feet are on).
   */
  private Point3 position = new Point3(0.0f, 2.1f, 0.0f);

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

  void move(Point3 dxyz) {
    position = position.plus(dxyz);
    computeViewMatrix();
  }

  Point2 rotation() {
    return rotation;
  }

  private static final float ROTATION_SPEED = 0.2f;

  void rotate(float dx, float dy) {
    float newX = rotation.x + dx * (-ROTATION_SPEED);
    float newY = clamp(rotation.y + dy * ROTATION_SPEED, -90.0f, 90.0f);
    rotation = new Point2(newX, newY);

    computeViewMatrix();
  }

  private static float clamp(float value, float min, float max) {
    return Math.max(min, Math.min(max, value));
  }

  private void computeViewMatrix() {
    float vert = Floats.cos(rotation.y);
    float dx = Floats.cos(rotation.x - 90.0f) * vert;
    float dy = Floats.sin(rotation.y);
    float dz = Floats.sin(rotation.x - 90.0f) * vert;
    Matrix.setLookAtM(viewMatrix, 0, position.x, position.y, position.z,
        position.x + dx, position.y + dy, position.z + dz, 0.0f, 1.0f, 0.0f);
  }
}
