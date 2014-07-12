package com.skligys.cardboardcreeper.perlin;

import com.skligys.cardboardcreeper.model.Block;
import com.skligys.cardboardcreeper.model.Chunk;

import net.royawesome.jlibnoise.module.Module;
import net.royawesome.jlibnoise.module.combiner.Add;
import net.royawesome.jlibnoise.module.combiner.Multiply;
import net.royawesome.jlibnoise.module.modifier.Clamp;
import net.royawesome.jlibnoise.module.modifier.ScalePoint;

import java.util.ArrayList;
import java.util.List;

public class Generator {
  private static final int MIN_FOREST_HILLS_Y = 46;
  private static final int MAX_FOREST_HILLS_Y = 100;
  private static final int SEA_LEVEL = 63;

  private final int seed;

  public Generator(int seed) {
    this.seed = seed;
  }

  public static int minElevation() {
    return MIN_FOREST_HILLS_Y;
  }

  public static int maxElevation() {
    return MAX_FOREST_HILLS_Y;
  }

  /** Generates blocks for a single chunk. */
  public List<Block> generateChunk(Chunk chunk) {
    int xOffset = chunk.x * Chunk.CHUNK_SIZE;
    int yOffset = chunk.y * Chunk.CHUNK_SIZE;
    int zOffset = chunk.z * Chunk.CHUNK_SIZE;
    float[][][] noise = noise(Chunk.CHUNK_SIZE, Chunk.CHUNK_SIZE, Chunk.CHUNK_SIZE, 4,
        xOffset, yOffset, zOffset);

    float minElevation = MIN_FOREST_HILLS_Y;
    float maxElevation = MAX_FOREST_HILLS_Y;
    float height = 0.5f * (maxElevation - minElevation);

    List<Block> result = new ArrayList<Block>();
    for (int x = 0; x < Chunk.CHUNK_SIZE; ++x) {
      for (int y = 0; y < Chunk.CHUNK_SIZE; ++y) {
        for (int z = 0; z < Chunk.CHUNK_SIZE; ++z) {
          float noiseValue = noise[x][y][z] - (y + yOffset - minElevation - height) / height;
          if (noiseValue >= 0.0f) {
            result.add(new Block(x + xOffset, y + yOffset, z + zOffset));
          }
        }
      }
    }
    return result;
  }

  /** Generates 3d noise for a single chunk with given size and offset. */
  private float[][][] noise(int xSize, int ySize, int zSize, int samplingRate,
      int xOffset, int yOffset, int zOffset) {
    if (samplingRate <= 0) {
      throw new IllegalArgumentException();
    }
    if (xSize % samplingRate != 0 || ySize % samplingRate != 0 || zSize % samplingRate != 0) {
      throw new IllegalArgumentException();
    }

    Module noiseGenerator = createGenerator(seed);
    float[][][] result = new float[xSize + 1][ySize + 1][zSize + 1];
    // Generate noise at sampling points.
    for (int x = 0; x <= xSize; x += samplingRate) {
      for (int y = 0; y <= ySize; y += samplingRate) {
        for (int z = 0; z <= zSize; z += samplingRate) {
          result[x][y][z] = (float) noiseGenerator.GetValue(x + xOffset, y + yOffset, z + zOffset);
        }
      }
    }
    // Interpolate between sampling points.  Do not fill in x == xSize, y == ySize, z == zSize,
    // nout used anyway.
    for (int x = 0; x < xSize; ++x) {
      for (int y = 0; y < ySize; ++y) {
        for (int z = 0; z < zSize; ++z) {
          if (samplingPoint(x, y, z, samplingRate)) {
            continue;
          }

          int sx0 = (x / samplingRate) * samplingRate;
          int sx1 = sx0 + samplingRate;
          int sy0 = (y / samplingRate) * samplingRate;
          int sy1 = sy0 + samplingRate;
          int sz0 = (z / samplingRate) * samplingRate;
          int sz1 = sz0 + samplingRate;

          result[x][y][z] = triLerp(x, y, z,
              result[sx0][sy0][sz0], result[sx0][sy1][sz0],
              result[sx0][sy0][sz1], result[sx0][sy1][sz1],
              result[sx1][sy0][sz0], result[sx1][sy1][sz0],
              result[sx1][sy0][sz1], result[sx1][sy1][sz1],
              sx0, sx1, sy0, sy1, sz0, sz1);
        }
      }
    }
    return result;
  }

  /**
   * Computes the value at x,y,z using trilinear interpolation.
   *
   * @param x the X coord of the value to interpolate
   * @param y the Y coord of the value to interpolate
   * @param z the Z coord of the value to interpolate
   * @param q000 the first known value (x1, y1, z1)
   * @param q001 the second known value (x1, y2, z1)
   * @param q010 the third known value (x1, y1, z2)
   * @param q011 the fourth known value (x1, y2, z2)
   * @param q100 the fifth known value (x2, y1, z1)
   * @param q101 the sixth known value (x2, y2, z1)
   * @param q110 the seventh known value (x2, y1, z2)
   * @param q111 the eighth known value (x2, y2, z2)
   * @param x1 the X coord of q000, q001, q010 and q011
   * @param x2 the X coord of q100, q101, q110 and q111
   * @param y1 the Y coord of q000, q010, q100 and q110
   * @param y2 the Y coord of q001, q011, q101 and q111
   * @param z1 the Z coord of q000, q001, q100 and q101
   * @param z2 the Z coord of q010, q011, q110 and q111
   * @return the interpolated value
   */
  private static float triLerp(float x, float y, float z,
      float q000, float q001, float q010, float q011, float q100, float q101, float q110, float q111,
      float x1, float x2, float y1, float y2, float z1, float z2) {
    float q00 = lerp(x, x1, x2, q000, q100);
    float q01 = lerp(x, x1, x2, q010, q110);
    float q10 = lerp(x, x1, x2, q001, q101);
    float q11 = lerp(x, x1, x2, q011, q111);
    float q0 = lerp(y, y1, y2, q00, q10);
    float q1 = lerp(y, y1, y2, q01, q11);
    return lerp(z, z1, z2, q0, q1);
  }

  /**
   * Computes the value at x using linear interpolation.
   *
   * @param x the X coord of the value to interpolate
   * @param x1 the X coord of q0
   * @param x2 the X coord of q1
   * @param q0 the first known value (x1)
   * @param q1 the second known value (x2)
   * @return the interpolated value
   */
  private static float lerp(float x, float x1, float x2, float q0, float q1) {
    return ((x2 - x) / (x2 - x1)) * q0 + ((x - x1) / (x2 - x1)) * q1;
  }

  private static boolean samplingPoint(int x, int y, int z, int samplingRate) {
    return x % samplingRate == 0 && y % samplingRate == 0 && z % samplingRate == 0;
  }

  private static Module createGenerator(int seed) {
    Module elevation = PerlinNoise.builder()
        .withSeed(seed * 23)
        .withFrequency(0.2)
        .withPersistence(0.7)
        .build();
    Module roughness = PerlinNoise.builder()
        .withSeed(seed * 29)
        .withFrequency(0.53)
        .withPersistence(0.9)
        .build();
    Module detail = PerlinNoise.builder()
        .withSeed(seed * 17)
        .withFrequency(0.7)
        .withPersistence(0.7)
        .build();

    Module combined = add(elevation, multiply(roughness, detail));
    Module scaled = scale(combined, 0.06, 0.06, 0.06);

    Module turbulent = Turbulence.builder()
        .withSource(scaled)
        .withSeed(seed * 53)
        .withFrequency(0.01)
        .withPower(8.0)
        .withRoughness(1)
        .build();
    return clamp(turbulent, -1.0, 1.0);
  }

  private static Module add(Module source1, Module source2) {
    Add result = new Add();
    result.SetSourceModule(0, source1);
    result.SetSourceModule(1, source2);
    return result;
  }

  private static Module multiply(Module source1, Module source2) {
    Multiply result = new Multiply();
    result.SetSourceModule(0, source1);
    result.SetSourceModule(1, source2);
    return result;
  }

  private static Module scale(Module source, double xScale, double yScale, double zScale) {
    ScalePoint result = new ScalePoint();
    result.SetSourceModule(0, source);
    result.setxScale(xScale);
    result.setyScale(yScale);
    result.setzScale(zScale);
    return result;
  }

  private static Module clamp(Module source, double min, double max) {
    Clamp result = new Clamp();
    result.SetSourceModule(0, source);
    result.setLowerBound(min);
    result.setUpperBound(max);
    return result;
  }
}
