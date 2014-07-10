package com.skligys.cardboardcreeper.model;

/** Immutable 2d coordinates of a point. */
public class Point2 {
  public final float x;
  public final float y;

  public Point2(float x, float y) {
    this.x = x;
    this.y = y;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Point2 point2 = (Point2) o;
    return (Float.compare(x, point2.x) == 0) && (Float.compare(y, point2.y) == 0);
  }

  @Override public int hashCode() {
    return 31 * (x != +0.0f ? Float.floatToIntBits(x) : 0) +
        (y != +0.0f ? Float.floatToIntBits(y) : 0);
  }

  @Override public String toString() {
    return "Point2{x=" + x + ", y=" + y + '}';
  }
}
