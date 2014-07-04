package com.skligys.cardboardcreeper;

/** Immutable 3d integer coordinates of a point. */
class Point3Int {
  final int x;
  final int y;
  final int z;

  Point3Int(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  Point3Int plus(Point3Int p2) {
    return new Point3Int(x + p2.x, y + p2.y, z + p2.z);
  }

  Point3Int times(int mult) {
    return new Point3Int(mult * x, mult * y, mult * z);
  }

  Point3Int round() {
    return new Point3Int(Math.round(x), Math.round(y), Math.round(z));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Point3Int point3Int = (Point3Int) o;
    return (x == point3Int.x) && (y == point3Int.y) && (z == point3Int.z);
  }

  @Override
  public int hashCode() {
    return 31 * 31 * x + 31 * y + z;
  }

  @Override public String toString() {
    return "Point3Int{x=" + x + ", y=" + y + ", z=" + z + '}';
  }
}
