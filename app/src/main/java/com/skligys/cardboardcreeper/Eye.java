package com.skligys.cardboardcreeper;

import android.opengl.Matrix;

import com.skligys.cardboardcreeper.model.Point2;
import com.skligys.cardboardcreeper.model.Point3;

class Eye {
  /** Eye position. */
  private Point3 position;

  /**
   * Eye rotation.  x is rotation in degrees in the horizontal plane starting from negative z axis
   * counter-clockwise.  Unbounded but periodic with period 360.0f.  y is rotation in degrees
   * from the horizontal plane up.  The range is from -90.0f (looking straight down) to 90.0f
   * (looking straight up).
   */
  private Point2 rotation = new Point2(0.0f, 0.0f);

  private final float[] viewMatrix = new float[16];

  Eye(float x, float y, float z) {
    this.position = new Point3(x, y, z);
    computeViewMatrix();
  }

  float[] viewMatrix() {
    return viewMatrix;
  }

  Point3 position() {
    return position;
  }

  void setPosition(Point3 xyz) {
    position = xyz;
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
