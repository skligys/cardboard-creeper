package com.skligys.cardboardcreeper;

import java.util.HashSet;
import java.util.Set;

class Physics {
  private static final float STEVE_WALKING_SPEED = 4.317f;  // m/s

  void updateEyePosition(Steve steve, float dt, Set<Point3Int> blocks) {
    Point3 dxyz = steve.motionVector().times(dt * STEVE_WALKING_SPEED);
    Point3 newPosition = steve.position().plus(dxyz);
    Point3 adjustedPosition = collisionAdjust(steve, newPosition, blocks);
    steve.setPosition(adjustedPosition);
  }

  /**
   * Checks the player's eye position for collisions with any blocks in the world, each block pushes
   * the position out.  This may not free the player if he is stuck between blocks.
   */
  private Point3 collisionAdjust(Steve steve, Point3 eyePosition, Set<Point3Int> blocks) {
    Set<Point3Int> collidingBlocks = new HashSet<Point3Int>();
    for (Point3Int block : steve.hitboxCornerBlocks(eyePosition)) {
      if (blocks.contains(block)) {
        collidingBlocks.add(block);
      }
    }
    if (collidingBlocks.isEmpty()) {
      return eyePosition;
    }

    for (Point3Int collidingBlock : collidingBlocks) {
      eyePosition = pushOut(steve, collidingBlock, eyePosition);
    }
    return eyePosition;
  }

  /**
   * The non-pushed out dimensions have to overlap at least this much for the pushing out to
   * happen.  This is done to prevent Steve hitting a block's corner and then getting pushed
   * sideways into the neighboring block.  Push-outs should be always perpendicular to the face
   * being hit, across many blocks.
   */
  private static final float OVERLAP_THRESHOLD = 0.25f;

  private Point3 pushOut(Steve steve, Point3Int block, Point3 eyePosition) {
    Hitbox hit = steve.hitbox(eyePosition);

    float overlapX = Math.min(block.x + 0.5f, hit.maxX) - Math.max(block.x - 0.5f, hit.minX);
    if (overlapX < 0.0f) {
      throw new IllegalStateException();
    }

    float overlapY = Math.min(block.y + 0.5f, hit.maxY) - Math.max(block.y - 0.5f, hit.minY);
    if (overlapY < 0.0f) {
      throw new IllegalStateException();
    }

    float overlapZ = Math.min(block.z + 0.5f, hit.maxZ) - Math.max(block.z - 0.5f, hit.minZ);
    if (overlapZ < 0.0f) {
      throw new IllegalStateException();
    }

    // Push out the smallest overlap, if the others are above a threshold.
    if (overlapX <= overlapY && overlapX <= overlapZ) {
      if (overlapX > 0.0f && overlapY >= OVERLAP_THRESHOLD && overlapZ >= OVERLAP_THRESHOLD) {
        eyePosition = pushOutX(block, eyePosition, hit.minX, hit.maxX);
      }
    } else if (overlapY <= overlapX && overlapY <= overlapZ) {
      if (overlapY > 0.0f && overlapX >= OVERLAP_THRESHOLD && overlapZ >= OVERLAP_THRESHOLD) {
        eyePosition = pushOutY(block, eyePosition, hit.minY, hit.maxY);
      }
    } else {  // overlapZ <= overlapX && overlapZ <= overlapY
      if (overlapZ > 0.0f && overlapX >= OVERLAP_THRESHOLD && overlapY >= OVERLAP_THRESHOLD) {
        eyePosition = pushOutZ(block, eyePosition, hit.minZ, hit.maxZ);
      }
    }

    return eyePosition;
  }

  private Point3 pushOutX(Point3Int block, Point3 p, float min, float max) {
    float mid = 0.5f * (min + max);
    if (mid < block.x) {
      float overlap = max - (block.x - 0.5f);
      return new Point3(p.x - overlap, p.y, p.z);
    } else {
      float overlap = (block.x + 0.5f) - min;
      return new Point3(p.x + overlap, p.y, p.z);
    }
  }

  private Point3 pushOutY(Point3Int block, Point3 p, float min, float max) {
    float mid = 0.5f * (min + max);
    if (mid < block.y) {
      float overlap = max - (block.y - 0.5f);
      return new Point3(p.x, p.y - overlap, p.z);
    } else {
      float overlap = (block.y + 0.5f) - min;
      return new Point3(p.x, p.y + overlap, p.z);
    }
  }

  private Point3 pushOutZ(Point3Int block, Point3 p, float min, float max) {
    float mid = 0.5f * (min + max);
    if (mid < block.z) {
      float overlap = max - (block.z - 0.5f);
      return new Point3(p.x, p.y, p.z - overlap);
    } else {
      float overlap = (block.z + 0.5f) - min;
      return new Point3(p.x, p.y, p.z + overlap);
    }
  }
}
