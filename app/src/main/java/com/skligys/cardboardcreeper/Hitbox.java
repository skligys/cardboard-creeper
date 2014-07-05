package com.skligys.cardboardcreeper;

class Hitbox {
  final float minX;
  final float minY;
  final float minZ;
  final float maxX;
  final float maxY;
  final float maxZ;

  Hitbox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    this.minX = minX;
    this.minY = minY;
    this.minZ = minZ;
    this.maxX = maxX;
    this.maxY = maxY;
    this.maxZ = maxZ;
  }
}
