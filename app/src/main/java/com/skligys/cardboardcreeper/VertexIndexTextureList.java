package com.skligys.cardboardcreeper;

import java.util.ArrayList;
import java.util.List;

class VertexIndexTextureList {
  // Here vertices are represented as 3 consecutive Floats, thus the length of the inner list is
  // 3 times the number of vertices.
  private final List<Float> coords = new ArrayList<Float>();
  private final List<Short> indices = new ArrayList<Short>();
  private final List<Float> textureCoords = new ArrayList<Float>();

  void addFace(Block block, Point3[] vertices, short[] drawListIdxs, float[] texCoords) {
    Point3 p = block.toPoint3();
    short[] faceIndices = {
        add(coords, p.plus(vertices[0])),
        add(coords, p.plus(vertices[1])),
        add(coords, p.plus(vertices[2])),
        add(coords, p.plus(vertices[3]))
    };
    for (int indexIndex : drawListIdxs) {
      indices.add(faceIndices[indexIndex]);
    }
    for (float textureCoord : texCoords) {
      textureCoords.add(textureCoord);
    }
  }

  private static final int MAX_UNSIGNED_SHORT = 65535;

  /** Adds the coordinate triple to the list and then returns the vertex's index. */
  private static short add(List<Float> coords, Point3 vertex) {
    int vertexCount = coords.size() / 3;
    // Overflowing signed short into unsigned short is fine, will not do comparisons or arithmetic.
    if (vertexCount > MAX_UNSIGNED_SHORT) {
      throw new IllegalStateException("Too many elements");
    }
    coords.add(vertex.x);
    coords.add(vertex.y);
    coords.add(vertex.z);
    return (short) vertexCount;
  }

  float[] getVertexArray() {
    return toFloatArray(coords);
  }

  short[] getIndexArray() {
    return toShortArray(indices);
  }

  float[] getTextureCoordArray() {
    return toFloatArray(textureCoords);
  }

  private static float[] toFloatArray(List<Float> list) {
    float[] result = new float[list.size()];
    for (int i = 0; i < result.length; ++i) {
      result[i] = list.get(i);
    }
    return result;
  }

  private static short[] toShortArray(List<Short> list) {
    short[] result = new short[list.size()];
    for (int i = 0; i < result.length; ++i) {
      result[i] = list.get(i);
    }
    return result;
  }
}
