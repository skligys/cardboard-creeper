package com.skligys.cardboardcreeper;

class Block extends Point3Int {
  Block(int x, int y, int z) {
    super(x, y, z);
  }

  Block(float x, float y, float z) {
    super(x, y, z);
  }

  public Block(Point3 p) {
    this(p.x, p.y, p.z);
  }

  @Override public String toString() {
    return "Block{x=" + x + ", y=" + y + ", z=" + z + '}';
  }
}
