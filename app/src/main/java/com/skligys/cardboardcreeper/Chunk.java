package com.skligys.cardboardcreeper;

class Chunk extends Point3Int {
  /** Blocks per side of a chunk. */
  private static final int CHUNK_SIZE = 16;

  Chunk(int x, int y, int z) {
    super(x, y, z);
  }

  Chunk(Block block) {
    this(block.x / CHUNK_SIZE, block.y / CHUNK_SIZE, block.z / CHUNK_SIZE);
  }

  Chunk(Point3 position) {
    this(new Block(position));
  }

  public Chunk plus(Chunk chunk) {
    return new Chunk(x + chunk.x, y + chunk.y, z + chunk.z);
  }

  @Override public String toString() {
    return "Chunk(" + x + ", " + y + ", " + z + ')';
  }
}
