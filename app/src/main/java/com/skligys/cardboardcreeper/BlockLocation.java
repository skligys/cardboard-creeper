package com.skligys.cardboardcreeper;

/** Immutable OpenGL coordinates where the block's center resides. */
class BlockLocation {
  // OpenGL coordinates of the center.
  final float x;
  final float y;
  final float z;

  public BlockLocation(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
}
