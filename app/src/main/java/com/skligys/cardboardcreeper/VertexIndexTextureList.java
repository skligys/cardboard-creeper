package com.skligys.cardboardcreeper;

import java.util.ArrayList;
import java.util.List;

class VertexIndexTextureList {
  // Here vertices are represented as 3 consecutive Floats, thus the length of the list is
  // 3 times the number of vertices.
  private final List<Float> coords = new ArrayList<Float>();
  private final List<Short> indices = new ArrayList<Short>();
  private final List<Float> textureCoords = new ArrayList<Float>();

  void addFace(Point3Int block, Point3[] vertices, short[] drawListIdxs, float[] texCoords) {
    Point3 p = block.toPoint3();
    short[] faceIndices = {
        add(p.plus(vertices[0])),
        add(p.plus(vertices[1])),
        add(p.plus(vertices[2])),
        add(p.plus(vertices[3]))
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
  private short add(Point3 vertex) {
    int intIndex = coords.size() / 3;
    // Overflowing signed short into unsigned short is fine, will not do comparisons or arithmetic.
    if (intIndex > MAX_UNSIGNED_SHORT) {
      throw new IllegalStateException("Too many elements");
    }
    coords.add(vertex.x);
    coords.add(vertex.y);
    coords.add(vertex.z);
    return (short) intIndex;
  }

  float[] vertexArray() {
    float[] result = new float[coords.size()];
    for (int i = 0; i < result.length; ++i) {
      result[i] = coords.get(i);
    }
    return result;
  }

  short[] indexArray() {
    short[] result = new short[indices.size()];
    for (int i = 0; i < result.length; ++i) {
      result[i] = indices.get(i);
    }
    return result;
  }

  float[] textureCoordArray() {
    float[] result = new float[textureCoords.size()];
    for (int i = 0; i < result.length; ++i) {
      result[i] = textureCoords.get(i);
    }
    return result;
  }
}
