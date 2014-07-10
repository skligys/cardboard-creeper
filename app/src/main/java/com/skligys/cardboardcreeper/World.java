package com.skligys.cardboardcreeper;

import android.content.res.Resources;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.skligys.cardboardcreeper.model.Block;
import com.skligys.cardboardcreeper.model.Chunk;
import com.skligys.cardboardcreeper.model.Point3;
import com.skligys.cardboardcreeper.perlin.Generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
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

  /** Perlin 3d noise based world generator. */
  private final Generator generator;
  /** All blocks in the world. */
  private final Set<Block> blocks = new HashSet<Block>();
  /** Maps chunk coordinates to a list of blocks inside the chunk. */
  private final Map<Chunk, List<Block>> chunkBlocks = new HashMap<Chunk, List<Block>>();

  /** OpenGL support for drawing grass blocks. */
  private final SquareMesh squareMesh = new SquareMesh();
  private final Performance performance = new Performance();
  private final Steve steve;
  private final Physics physics = new Physics();

  /** Pre-allocated temporary matrix. */
  private final float[] viewProjectionMatrix = new float[16];

  private static interface ChunkChange {}

  private static class ChunkLoad implements ChunkChange {
    private final Chunk chunk;

    ChunkLoad(Chunk chunk) {
      this.chunk = chunk;
    }
  }

  private static class ChunkUnload implements ChunkChange {
    private final Chunk chunk;

    ChunkUnload(Chunk chunk) {
      this.chunk = chunk;
    }
  }

  private final BlockingDeque<ChunkChange> chunkChanges = new LinkedBlockingDeque<ChunkChange>();
  private final Thread chunkLoader;

  World(int xSize, int zSize) {
    this.xSize = xSize;
    this.zSize = zSize;

    long start = SystemClock.uptimeMillis();
    generator = new Generator(new Random().nextInt());
    perlinHills();
    steve = new Steve(startPosition());
    Chunk currChunk = steve.currentChunk();
    long elapsed = SystemClock.uptimeMillis() - start;
    Log.i(TAG, "Generated " + chunkBlocks.keySet().size() + " chunks in " + elapsed + "ms");

    // Pre-load a single center chunk with shown blocks.
    squareMesh.load(currChunk, shownBlocks(chunkBlocks.get(currChunk)), blocks);

    // Start the thread to load chunks in the background.
    chunkLoader = createChunkLoader();
    chunkLoader.start();

    // Schedule neighboring chunks to load in the background.
    Set<Chunk> chunksToLoad = neighboringChunks(currChunk);
    chunksToLoad.remove(currChunk);
    for (Chunk chunk : chunksToLoad) {
      chunkChanges.add(new ChunkLoad(chunk));
    }
  }

  /** Adds blocks generated based on 3d Perlin noise. */
  // TODO: Generate and load chunks on demand.
  private void perlinHills() {
    clearBlocks();

    int minXChunk = -xSize / (2 * Chunk.CHUNK_SIZE);
    int maxXChunk = -minXChunk;
    int minZChunk = -zSize / (2 * Chunk.CHUNK_SIZE);
    int maxZChunk = -minZChunk;
    int minYChunk = Generator.minElevation() / Chunk.CHUNK_SIZE;
    int maxYChunk = (Generator.maxElevation() + Chunk.CHUNK_SIZE - 1) / Chunk.CHUNK_SIZE;
    for (int x = minXChunk; x <= maxXChunk; ++x) {
      for (int y = minYChunk; y <= maxYChunk; ++y) {
        for (int z = minZChunk; z <= maxZChunk; ++z) {
          for (Block block: generator.generateChunk(new Chunk(x, y, z))) {
            addBlock(block);
          }
        }
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

  /**
   * Looks through chunks with xz == (0, 0), finds the highest block with xz coordinate (0, 0)
   * and returns it.
   */
  private Block startPosition() {
    int maxY = highestSolidY(0, 0);
    return new Block(0, maxY, 0);
  }

  /** Given (x,z) coordinates, finds and returns the highest y so that (x,y,z) is a solid block. */
  private int highestSolidY(int x, int z) {
    int maxY = Generator.minElevation();
    int chunkX = x / Chunk.CHUNK_SIZE;
    int chunkZ = z / Chunk.CHUNK_SIZE;
    for (Chunk chunk : chunkBlocks.keySet()) {
      if (chunk.x != chunkX || chunk.z != chunkZ) {
        continue;
      }
      for (Block block : chunkBlocks.get(chunk)) {
        if (block.x != x || block.z != z) {
          continue;
        }
        if (block.y > maxY) {
          maxY = block.y;
        }
      }
    }
    return maxY;
  }

  private List<Block> shownBlocks(List<Block> blocks) {
    List<Block> result = new ArrayList<Block>();
    for (Block block : blocks) {
      if (exposed(block)) {
        result.add(block);
      }
    }
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

  private static final int SHOWN_CHUNK_RADIUS = 3;

  /** Returns chunks within some radius of center, but only those containing any blocks. */
  private Set<Chunk> neighboringChunks(Chunk center) {
    Set<Chunk> result = new HashSet<Chunk>();
    for (int dx = -SHOWN_CHUNK_RADIUS; dx <= SHOWN_CHUNK_RADIUS; ++dx) {
      for (int dy = -SHOWN_CHUNK_RADIUS; dy <= SHOWN_CHUNK_RADIUS; ++dy) {
        for (int dz = -SHOWN_CHUNK_RADIUS; dz <= SHOWN_CHUNK_RADIUS; ++dz) {
          if (!chunkShown(dx, dy, dz)) {
            continue;
          }
          Chunk chunk = center.plus(new Chunk(dx, dy, dz));
          if (chunkBlocks.keySet().contains(chunk)) {
            result.add(chunk);
          }
        }
      }
    }
    return result;
  }

  private static boolean chunkShown(int dx, int dy, int dz) {
    return dx * dx + dy * dy + dz * dz <= SHOWN_CHUNK_RADIUS * SHOWN_CHUNK_RADIUS;
  }

  /** Asynchronous chunk loader. */
  private Thread createChunkLoader() {
    Runnable runnable = new Runnable() {
      @Override public void run() {
        while (true) {
          try {
            ChunkChange cc = chunkChanges.takeFirst();
            if (cc instanceof ChunkLoad) {
              ChunkLoad cl = (ChunkLoad) cc;
              squareMesh.load(cl.chunk, shownBlocks(chunkBlocks.get(cl.chunk)), blocks);
            } else if (cc instanceof ChunkUnload) {
              squareMesh.unload(((ChunkUnload) cc).chunk);
            } else {
              throw new RuntimeException("Unknown ChunkChange subtype: " + cc.getClass().getName());
            }
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }

          SystemClock.sleep(1);
        }
      }
    };
    return new Thread(runnable);
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
      queueChunkLoads(beforeChunk, afterChunk);
      steve.setCurrentChunk(afterChunk);
    }

    performance.startRendering();
    Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, steve.viewMatrix(), 0);
    squareMesh.draw(viewProjectionMatrix);
    performance.endRendering();

    float fps = performance.fps();
    if (fps > 0.0f) {
      Point3 position = steve.position();
      String status = String.format("%f FPS (%f-%f), " +
          "(%f, %f, %f), " +
          "%d / %d chunks, %d blocks, " +
          "physics: %dms, render: %dms",
          fps, performance.minFps(), performance.maxFps(),
          position.x, position.y, position.z,
          squareMesh.chunksLoaded(), chunkBlocks.keySet().size(), blocks.size(),
          performance.physicsSpent(), performance.renderSpent());
      Log.i(TAG, status);
    }
    performance.done();
  }

  private void queueChunkLoads(Chunk beforeChunk, Chunk afterChunk) {
    Set<Chunk> beforeShownChunks = neighboringChunks(beforeChunk);
    Set<Chunk> afterShownChunks = neighboringChunks(afterChunk);

    // chunksToLoad = afterShownChunks \ beforeShownChunks
    // chunksToUnload = beforeShownChunks \ afterShownChunks
    for (Chunk chunk : setDiff(afterShownChunks, beforeShownChunks)) {
      chunkChanges.add(new ChunkLoad(chunk));
    }
    for (Chunk chunk : setDiff(beforeShownChunks, afterShownChunks)) {
      chunkChanges.add(new ChunkUnload(chunk));
    }
  }

  private static <T> Set<T> setDiff(Set<T> s1, Set<T> s2) {
    Set<T> result = new HashSet<T>(s1);
    result.removeAll(s2);
    return result;
  }

  void drag(float dx, float dy) {
    steve.rotate(dx, dy);
  }

  void walk(boolean start) {
    steve.walk(start);
  }
}
