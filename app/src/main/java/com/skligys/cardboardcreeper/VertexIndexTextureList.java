package com.skligys.cardboardcreeper;

import java.util.ArrayList;
import java.util.List;

class VertexIndexTextureList {
  // Here vertices are represented as 3 consecutive Floats, thus the length of the inner list is
  // 3 times the number of vertices.
  private final List<List<Float>> coords;
  private final List<List<Short>> indices;
  private final List<List<Float>> textureCoords;

  VertexIndexTextureList() {
    this.coords = new ArrayList<List<Float>>();
    this.coords.add(new ArrayList<Float>());

    this.indices = new ArrayList<List<Short>>();
    this.indices.add(new ArrayList<Short>());

    this.textureCoords = new ArrayList<List<Float>>();
    this.textureCoords.add(new ArrayList<Float>());
  }

  void addFace(Block block, Point3[] vertices, short[] drawListIdxs, float[] texCoords) {
    ensureHasSpace(4);

    List<Float> lastCoords = lastCoords();
    List<Short> lastIndices = lastIndices();
    List<Float> lastTextureCoords = lastTextureCoords();

    Point3 p = block.toPoint3();
    short[] faceIndices = {
        add(lastCoords, p.plus(vertices[0])),
        add(lastCoords, p.plus(vertices[1])),
        add(lastCoords, p.plus(vertices[2])),
        add(lastCoords, p.plus(vertices[3]))
    };
    for (int indexIndex : drawListIdxs) {
      lastIndices.add(faceIndices[indexIndex]);
    }
    for (float textureCoord : texCoords) {
      lastTextureCoords.add(textureCoord);
    }
  }

  private static final int MAX_UNSIGNED_SHORT = 65535;

  private void ensureHasSpace(int elementCount) {
    int vertexCount = lastCoords().size() / 3;
    // Overflowing signed short into unsigned short is fine, will not do comparisons or arithmetic.
    if (vertexCount + elementCount <= MAX_UNSIGNED_SHORT) {
      return;
    }

    // No more space without overflowing short indices, start new lists.
    this.coords.add(new ArrayList<Float>());
    this.indices.add(new ArrayList<Short>());
    this.textureCoords.add(new ArrayList<Float>());
  }

  private List<Float> lastCoords() {
    return coords.get(coords.size() - 1);
  }

  private List<Short> lastIndices() {
    return indices.get(indices.size() - 1);
  }

  private List<Float> lastTextureCoords() {
    return textureCoords.get(textureCoords.size() - 1);
  }

  /** Adds the coordinate triple to the list and then returns the vertex's index. */
  private static short add(List<Float> lastCoords, Point3 vertex) {
    int vertexCount = lastCoords.size() / 3;
    // Overflowing signed short into unsigned short is fine, will not do comparisons or arithmetic.
    if (vertexCount > MAX_UNSIGNED_SHORT) {
      throw new IllegalStateException("Too many elements");
    }
    lastCoords.add(vertex.x);
    lastCoords.add(vertex.y);
    lastCoords.add(vertex.z);
    return (short) vertexCount;
  }

  static class VertexIndexTextureArray {
    final float[] vertexArray;
    final short[] indexArray;
    final float[] textureCoordArray;

    VertexIndexTextureArray(float[] vertexArray, short[] indexArray, float[] textureCoordArray) {
      this.vertexArray = vertexArray;
      this.indexArray = indexArray;
      this.textureCoordArray = textureCoordArray;
    }
  }

  List<VertexIndexTextureArray> vertexIndexTextureArrays() {
    if (coords.size() != indices.size() || coords.size() != textureCoords.size()) {
      throw new IllegalStateException("Vertex, index and texture coordinate lists should equal length");
    }

    List<VertexIndexTextureArray> result = new ArrayList<VertexIndexTextureArray>();
    for (int i = 0; i < coords.size(); ++i) {
      VertexIndexTextureArray vita = new VertexIndexTextureArray(
          toFloatArray(coords.get(i)),
          toShortArray(indices.get(i)),
          toFloatArray(textureCoords.get(i)));
      result.add(vita);
    }
    return result;
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
