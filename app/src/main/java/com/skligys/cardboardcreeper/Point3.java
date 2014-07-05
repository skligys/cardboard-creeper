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

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Point3 point3 = (Point3) o;
    return (Float.compare(x, point3.x) == 0) &&
        (Float.compare(y, point3.y) == 0) &&
        (Float.compare(z, point3.z) == 0);
  }

  @Override public int hashCode() {
    return 31 * 31 * (x != +0.0f ? Float.floatToIntBits(x) : 0) +
        31 * (y != +0.0f ? Float.floatToIntBits(y) : 0) +
        (z != +0.0f ? Float.floatToIntBits(z) : 0);
  }

  @Override public String toString() {
    return "Point3{x=" + x + ", y=" + y + ", z=" + z + '}';
  }
}
