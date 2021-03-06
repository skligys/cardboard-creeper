package com.skligys.cardboardcreeper;

import com.skligys.cardboardcreeper.model.Block;
import com.skligys.cardboardcreeper.model.Chunk;
import com.skligys.cardboardcreeper.model.Point3;

import java.util.HashSet;
import java.util.Set;

class Steve {
  private static final float STEVE_EYE_LEVEL = 1.62f;  // meters from feet.
  private static final float STEVE_HITBOX_HEIGHT = 1.8f;  // meters from feet.
  private static final float STEVE_HITBOX_WIDTH = 0.6f;  // meters

  private final Eye eye;
  private boolean walking = false;
  /** Speed in axis y direction (up), in m/s. */
  private float verticalSpeed = 0.0f;

  private final Object currentChunkLock = new Object();
  private Chunk currentChunk;

  /** Create Steve based on block he is standing on. */
  Steve(Block block) {
    /**
     * Initially, the eye is located at (block.x, block.z) in xz plane, at height block.y + 2.12
     * (feet to eye 1.62 + 0.5 displacement from block the feet are on).
     */
    eye = new Eye(block.x, block.y + 0.50001f + STEVE_EYE_LEVEL, block.z);
    currentChunk = new Chunk(block);
  }

  void walk(boolean start) {
    walking = start;
  }

  float verticalSpeed() {
    return verticalSpeed;
  }

  void setVerticalSpeed(float verticalSpeed) {
    this.verticalSpeed = verticalSpeed;
  }

  Chunk currentChunk() {
    synchronized(currentChunkLock) {
      return currentChunk;
    }
  }

  public void setCurrentChunk(Chunk chunk) {
    synchronized(currentChunkLock) {
      this.currentChunk = chunk;
    }
  }

  void rotate(float dx, float dy) {
    eye.rotate(dx, dy);
  }

  private static final Point3 ZERO_VECTOR = new Point3(0.0f, 0.0f, 0.0f);

  Point3 position() {
    return eye.position();
  }

  void setPosition(Point3 eyePosition) {
    eye.setPosition(eyePosition);
  }

  Point3 motionVector() {
    if (!walking) {
      return ZERO_VECTOR;
    }

    float xAngle = eye.rotation().x - 90.0f;
    return new Point3(Floats.cos(xAngle), 0.0f, Floats.sin(xAngle));
  }

  /**
   * Given Steve's eye position, returns a set of blocks corresponding to all 8 corners of his
   * hitbox.
   */
  Set<Block> hitboxCornerBlocks(Point3 eyePosition) {
    Hitbox hit = hitbox(eyePosition);

    Set<Block> result = new HashSet<Block>();
    result.add(new Block(hit.minX, hit.minY, hit.minZ));
    result.add(new Block(hit.maxX, hit.minY, hit.minZ));
    result.add(new Block(hit.minX, hit.maxY, hit.minZ));
    result.add(new Block(hit.maxX, hit.maxY, hit.minZ));
    result.add(new Block(hit.minX, hit.minY, hit.maxZ));
    result.add(new Block(hit.maxX, hit.minY, hit.maxZ));
    result.add(new Block(hit.minX, hit.maxY, hit.maxZ));
    result.add(new Block(hit.maxX, hit.maxY, hit.maxZ));
    return result;
  }

  /** Given Steve's eye position, returns his hitbox. */
  Hitbox hitbox(Point3 eyePosition) {
    float minX = eyePosition.x - STEVE_HITBOX_WIDTH / 2.0f;
    float maxX = minX + STEVE_HITBOX_WIDTH;
    float minY = eyePosition.y - STEVE_EYE_LEVEL;
    float maxY = minY + STEVE_HITBOX_HEIGHT;
    float minZ = eyePosition.z - STEVE_HITBOX_WIDTH / 2.0f;
    float maxZ = minZ + STEVE_HITBOX_WIDTH;

    return new Hitbox(minX, minY, minZ, maxX, maxY, maxZ);
  }

  /**
   * Given Steve's eye position, returns a set of blocks corresponding to 4 points around his knees.
   */
  Set<Block> kneeBlocks(Point3 eyePosition) {
    Hitbox hit = hitbox(eyePosition);
    float kneeY = 0.5f * (hit.minY + hit.maxY);

    Set<Block> result = new HashSet<Block>();
    result.add(new Block(hit.minX, kneeY, hit.minZ));
    result.add(new Block(hit.maxX, kneeY, hit.minZ));
    result.add(new Block(hit.minX, kneeY, hit.maxZ));
    result.add(new Block(hit.maxX, kneeY, hit.maxZ));
    return result;
  }

  /**
   * Given Steve's eye position, returns a set of blocks corresponding to 4 points around his head.
   */
  Set<Block> headBlocks(Point3 eyePosition) {
    Hitbox hit = hitbox(eyePosition);

    Set<Block> result = new HashSet<Block>();
    result.add(new Block(hit.minX, hit.maxY, hit.minZ));
    result.add(new Block(hit.maxX, hit.maxY, hit.minZ));
    result.add(new Block(hit.minX, hit.maxY, hit.maxZ));
    result.add(new Block(hit.maxX, hit.maxY, hit.maxZ));
    return result;
  }

  public float[] viewMatrix() {
    return eye.viewMatrix();
  }
}
