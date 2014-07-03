package com.skligys.cardboardcreeper;

/** Immutable 3d coordinates of a point. */
class Point3 {
  final float x;
  final float y;
  final float z;

  Point3(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  Point3 plus(Point3 p2) {
    return new Point3(x + p2.x, y + p2.y, z + p2.z);
  }

  Point3 times(float mult) {
    return new Point3(mult * x, mult * y, mult * z);
  }
}
