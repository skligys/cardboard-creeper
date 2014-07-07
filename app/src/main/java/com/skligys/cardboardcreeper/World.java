package com.skligys.cardboardcreeper;

import android.content.res.Resources;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

/** Holds a randomly generated hilly landscape of blocks and Steve. */
class World {
  private static final String TAG = "World";

  /**
   * Do several physics iterations per frame to avoid falling through the floor when dt is large.
   */
  private static final int PHYSICS_ITERATIONS_PER_FRAME = 5;

  // World size in x and z directions.
  private final int xSize;
  private final int zSize;

  /** All blocks in the world. */
  private final Set<Block> blocks = new HashSet<Block>();
  /**
   * Shown blocks only. A block may be not shown if it is either blocked on all sides by
   * neighboring blocks, or if the chunk it belongs to is not loaded.
   */
  private final Set<Block> shownBlocks = new HashSet<Block>();
  /** Maps chunk coordinates to a list of blocks inside the chunk. */
  private final Map<Chunk, List<Block>> chunkBlocks = new HashMap<Chunk, List<Block>>();

  /** OpenGL support for drawing grass blocks. */
  private final SquareMesh squareMesh;
  private final Performance performance = new Performance();
  private final Steve steve = new Steve();
  private final Physics physics = new Physics();

  /** Pre-allocated temporary matrix. */
  private final float[] viewProjectionMatrix = new float[16];

  private static class ChunkChange {
    private final Chunk beforeChunk;
    private final Chunk afterChunk;

    ChunkChange(Chunk beforeChunk, Chunk afterChunk) {
      this.beforeChunk = beforeChunk;
      this.afterChunk = afterChunk;
    }
  }
  private final Deque<ChunkChange> chunkChanges = new LinkedBlockingDeque<ChunkChange>();
  private final Thread chunkLoader;

  World(int xSize, int zSize) {
    this.xSize = xSize;
    this.zSize = zSize;
    randomHills();
    computeShownBlocks();
    // Pre-create the mesh out of only shownBlocks.
    squareMesh = new SquareMesh(shownBlocks);

    // Start the thread to load chunks in the background.
    chunkLoader = createChunkLoader();
    chunkLoader.start();
  }

  /** Fills in {@code blocks} and {@code chunkBlocks}. */
  private void randomHills() {
    clearBlocks();

    // Create a layer of grass everywhere.
    for (int x = -xSize / 2; x <= xSize / 2; ++x) {
      for (int z = -zSize / 2; z <= zSize / 2; ++z) {
        addBlock(new Block(x, 0, z));
        // 3 blocks high walls at the end of the world.
        if (x == -xSize / 2 || x == xSize / 2 || z == -zSize / 2 || z == zSize / 2) {
          for (int y = 1; y <= 3; ++y) {
            addBlock(new Block(x, y, z));
          }
        }
      }
    }

    // Random hills.  120 hills for 160x160 size.
    Random r = new Random();
    // Hill centers are not allowed within 10 blocks from the end of the world, so that hills
    // wouldn't allow jumping over the fence and into the infinite void.
    int hillCount = (xSize - 20) * (zSize - 20) / 163;
    for (int i = 0; i < hillCount; ++i) {
      int xCenter = randomInt(r, -xSize / 2 + 10, xSize / 2 - 10);
      int zCenter = randomInt(r, -zSize / 2 + 10, zSize / 2 - 10);
      int height = randomInt(r, 1, 6);
      int radius = randomInt(r, 5, 9);
      for (int y = 1; y <= height; ++y) {
        for (int x = xCenter - radius; x <= xCenter + radius; ++x) {
          for (int z = zCenter - radius; z <= zCenter + radius; ++z) {
            if (insideBounds(x, z) &&
                insideHill(x, z, xCenter, zCenter, radius) &&
                !insideCenterCylinder(x, z, 5)) {
              addBlock(new Block(x, y, z));
            }
          }
        }
        // Taper the hill as you go up.
        --radius;
      }
    }
  }

  private void clearBlocks() {
    blocks.clear();
    chunkBlocks.clear();
  }

  private void addBlock(Block block) {
    blocks.add(block);

    Chunk chunk = new Chunk(block);
    List<Block> blocksInChunk = chunkBlocks.get(chunk);
    if (blocksInChunk == null) {
      blocksInChunk = new ArrayList<Block>();
      chunkBlocks.put(chunk, blocksInChunk);
    }
    blocksInChunk.add(block);
  }

  private static int randomInt(Random r, int min, int max) {
    return r.nextInt(max - min + 1) + min;
  }

  private boolean insideBounds(int x, int z) {
    return x >= -xSize / 2 && x <= xSize / 2 && z >= -zSize / 2 && z <= zSize / 2;
  }

  private boolean insideHill(int x, int z, int xCenter, int zCenter, int radius) {
    return (x - xCenter) * (x - xCenter) + (z - zCenter) * (z - zCenter) <= radius * radius;
  }

  private boolean insideCenterCylinder(int x, int z, int radius) {
    return x * x + z * z <= radius * radius;
  }

  /**
   * Looks through all blocks and adds to {@code shownBlocks} only those that are exposed, i.e.
   * not completely surrounded on all sides.
   */
  private void computeShownBlocks() {
    synchronized(shownBlocks) {
      shownBlocks.clear();

      for (Block block : blocks) {
        if (chunkShown(new Chunk(block), steve.currentChunk()) && exposed(block)) {
          shownBlocks.add(block);
        }
      }
    }
  }

  private static final int SHOWN_CHUNK_RADIUS = 3;

  private static boolean chunkShown(Chunk chunk, Chunk current) {
    return chunkShown(chunk.x - current.x, chunk.y - current.y, chunk.z - current.z);
  }

  private static boolean chunkShown(int dx, int dy, int dz) {
    return dx * dx + dy * dy + dz * dz <= SHOWN_CHUNK_RADIUS * SHOWN_CHUNK_RADIUS;
  }

  /** Asynchronous chunk loader. */
  private Thread createChunkLoader() {
    Runnable runnable = new Runnable() {
      @Override public void run() {
        while (true) {
          ChunkChange first = chunkChanges.peekFirst();
          ChunkChange last = chunkChanges.peekLast();
          if (first != null && last != null) {
            chunkChanges.clear();
            Chunk before = first.beforeChunk;
            Chunk after = last.afterChunk;
            Log.i(TAG, "Moving from chunk: " + before + " to " + after + "...");
            long start = SystemClock.uptimeMillis();

            changeChunk(before, after);
            squareMesh.update(shownBlocks);

            long elapsed = SystemClock.uptimeMillis() - start;
            Log.i(TAG, "Loading chunks took " + elapsed + "ms");
          }

          SystemClock.sleep(1);
        }
      }
    };
    return new Thread(runnable);
  }

  private void changeChunk(Chunk beforeCurrChunk, Chunk afterCurrChunk) {
    Set<Chunk> beforeShownChunks = new HashSet<Chunk>();
    Set<Chunk> afterShownChunks = new HashSet<Chunk>();
    for (int dx = -SHOWN_CHUNK_RADIUS; dx <= SHOWN_CHUNK_RADIUS; ++dx) {
      for (int dy = -SHOWN_CHUNK_RADIUS; dy <= SHOWN_CHUNK_RADIUS; ++dy) {
        for (int dz = -SHOWN_CHUNK_RADIUS; dz <= SHOWN_CHUNK_RADIUS; ++dz) {
          if (!chunkShown(dx, dy, dz)) {
            continue;
          }
          beforeShownChunks.add(beforeCurrChunk.plus(new Chunk(dx, dy, dz)));
          afterShownChunks.add(afterCurrChunk.plus(new Chunk(dx, dy, dz)));
        }
      }
    }

    synchronized(shownBlocks) {
      // showChunks = afterShownChunks \ beforeShownChunks
      // hideChunks = beforeShownChunks \ afterShownChunks
      for (Chunk chunk : setDiff(afterShownChunks, beforeShownChunks)) {
        showChunk(chunk);
      }
      for (Chunk chunk : setDiff(beforeShownChunks, afterShownChunks)) {
        hideChunk(chunk);
      }
    }
  }

  private void showChunk(Chunk chunk) {
    for (Block block : chunkBlocks.get(chunk)) {
      if (!shownBlocks.contains(block) && exposed(block)) {
        shownBlocks.add(block);
      }
    }
  }

  private void hideChunk(Chunk chunk) {
    for (Block block : chunkBlocks.get(chunk)) {
      if (shownBlocks.contains(block)) {
        shownBlocks.remove(block);
      }
    }
  }

  private static <T> Set<T> setDiff(Set<T> s1, Set<T> s2) {
    Set<T> result = new HashSet<T>(s1);
    result.removeAll(s2);
    return result;
  }

  /**
   * Checks all 6 faces of the given block and returns true if at least one face is not covered
   * by another block in {@code blocks}.
   */
  private boolean exposed(Block block) {
    return !blocks.contains(new Block(block.x - 1, block.y, block.z)) ||
        !blocks.contains(new Block(block.x + 1, block.y, block.z)) ||
        !blocks.contains(new Block(block.x, block.y - 1, block.z)) ||
        !blocks.contains(new Block(block.x, block.y + 1, block.z)) ||
        !blocks.contains(new Block(block.x, block.y, block.z - 1)) ||
        !blocks.contains(new Block(block.x, block.y, block.z + 1));
  }

  void surfaceCreated(Resources resources) {
    squareMesh.surfaceCreated(resources);
  }

  void draw(float[] projectionMatrix) {
    // This has to be first to have up to date tick timestamp for FPS computation.
    float dt = Math.min(performance.tick(), 0.2f);

    performance.startPhysics();
    Point3 eyePosition = null;
    // Do several physics iterations per frame to avoid falling through the floor when dt is large.
    for (int i = 0; i < PHYSICS_ITERATIONS_PER_FRAME; ++i) {
      // Physics needs all blocks in the world to compute collisions.
      eyePosition = physics.updateEyePosition(steve, dt / PHYSICS_ITERATIONS_PER_FRAME, blocks);
    }
    performance.endPhysics();

    Chunk beforeChunk = steve.currentChunk();
    Chunk afterChunk = new Chunk(eyePosition);
    if (!afterChunk.equals(beforeChunk)) {
      chunkChanges.add(new ChunkChange(beforeChunk, afterChunk));
      steve.setCurrentChunk(afterChunk);
    }

    performance.startRendering();
    Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, steve.viewMatrix(), 0);
    squareMesh.draw(viewProjectionMatrix);
    performance.endRendering();

    float fps = performance.fps();
    if (fps > 0.0f) {
      Point3 position = steve.position();
      String status = String.format("%f FPS (%f-%f), (%f, %f, %f), %d / %d blocks, " +
          "physics: %dms, render: %dms",
          fps, performance.minFps(), performance.maxFps(),
          position.x, position.y, position.z, shownBlocks.size(), blocks.size(),
          performance.physicsSpent(), performance.renderSpent());
      Log.i(TAG, status);
    }
    performance.done();
  }

  void drag(float dx, float dy) {
    steve.rotate(dx, dy);
  }

  void walk(boolean start) {
    steve.walk(start);
  }
}
