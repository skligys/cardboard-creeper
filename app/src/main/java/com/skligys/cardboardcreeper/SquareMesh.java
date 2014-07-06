package com.skligys.cardboardcreeper;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.os.SystemClock;
import android.util.Log;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Set;

class SquareMesh {
  private static final String TAG = "SquareMesh";

  private final FloatBuffer vertexBuffer;
  private final ShortBuffer drawListBuffer;
  private final FloatBuffer textureCoordBuffer;

  // Initialized during surface creation.
  private int program;
  private int textureData;
  private int mvpMatrixHandle;
  private int positionHandle;
  private int textureCoordHandle;

  SquareMesh(Set<Block> blocks) {
    long start = SystemClock.uptimeMillis();

    // SK: Debug.
    Log.i(TAG, "-----------------------------------------------------------------");
    Log.i(TAG, "Processing " + blocks.size() + " blocks...");

    VertexIndexTextureList vitList = new VertexIndexTextureList();
    int squaresAdded = 0;
    for (Block block : blocks) {
      // Only add faces that are not between two blocks and thus invisible.
      if (!blocks.contains(new Block(block.x, block.y + 1, block.z))) {
        addTopFace(vitList, block);
        ++squaresAdded;
      }

      if (!blocks.contains(new Block(block.x, block.y, block.z + 1))) {
        addFrontFace(vitList, block);
        ++squaresAdded;
      }
      if (!blocks.contains(new Block(block.x - 1, block.y, block.z))) {
        addLeftFace(vitList, block);
        ++squaresAdded;
      }
      if (!blocks.contains(new Block(block.x + 1, block.y, block.z))) {
        addRightFace(vitList, block);
        ++squaresAdded;
      }
      if (!blocks.contains(new Block(block.x, block.y, block.z - 1))) {
        addBackFace(vitList, block);
        ++squaresAdded;
      }

      if (!blocks.contains(new Block(block.x, block.y - 1, block.z))) {
        addBottomFace(vitList, block);
        ++squaresAdded;
      }
    }

    vertexBuffer = GlHelper.createFloatBuffer(vitList.vertexArray());
    drawListBuffer = GlHelper.createShortBuffer(vitList.indexArray());
    textureCoordBuffer = GlHelper.createFloatBuffer(vitList.textureCoordArray());

    Log.i(TAG, "Squares: " + squaresAdded +
        ", vertex buffer: " + vertexBuffer.limit() +
        ", draw list buffer: " + drawListBuffer.limit() +
        ", texture coordinate buffer: " + textureCoordBuffer.limit());
    Log.i(TAG, "Spent " + (SystemClock.uptimeMillis() - start) + "ms");
  }

  // OpenGL coordinates:
  //        ^ y
  //        |     x
  //        +--->
  //   z   /
  //      v
  private static final Point3 TOP_FACE[] = {
      new Point3(-0.5f, 0.5f, 0.5f),  // front left
      new Point3(0.5f, 0.5f, 0.5f),  // front right
      new Point3(0.5f, 0.5f, -0.5f),  // rear right
      new Point3(-0.5f, 0.5f, -0.5f)  // rear left
  };

  private static final short[] FACE_DRAW_LIST_IDXS = {
      0, 1, 3,
      3, 1, 2,
  };

  // Flip top and bottom since bitmaps are loaded upside down.
  private static final float[] TOP_FACE_TEXTURE_COORDS = {
      0.0f, 1.0f,
      0.5f, 1.0f,
      0.5f, 0.5f,
      0.0f, 0.5f,
  };

  private void addTopFace(VertexIndexTextureList vitList, Block block) {
    vitList.addFace(block, TOP_FACE, FACE_DRAW_LIST_IDXS, TOP_FACE_TEXTURE_COORDS);
  }

  private static final Point3 FRONT_FACE[] = {
      new Point3(-0.5f, -0.5f, 0.5f),  // bottom left
      new Point3(0.5f, -0.5f, 0.5f),  // bottom right
      new Point3(0.5f, 0.5f, 0.5f),  // top right
      new Point3(-0.5f, 0.5f, 0.5f)  // top left
  };

  // Flip top and bottom since bitmaps are loaded upside down.
  private static final float[] SIDE_FACE_TEXTURE_COORDS = {
      0.5f, 1.0f,
      1.0f, 1.0f,
      1.0f, 0.5f,
      0.5f, 0.5f,
  };

  private void addFrontFace(VertexIndexTextureList vitList, Block block) {
    vitList.addFace(block, FRONT_FACE, FACE_DRAW_LIST_IDXS, SIDE_FACE_TEXTURE_COORDS);
  }

  private static final Point3 LEFT_FACE[] = {
      new Point3(-0.5f, -0.5f, -0.5f),  // rear bottom
      new Point3(-0.5f, -0.5f, 0.5f),  // front bottom
      new Point3(-0.5f, 0.5f, 0.5f),  // front top
      new Point3(-0.5f, 0.5f, -0.5f)  // rear top
  };

  private void addLeftFace(VertexIndexTextureList vitList, Block block) {
    vitList.addFace(block, LEFT_FACE, FACE_DRAW_LIST_IDXS, SIDE_FACE_TEXTURE_COORDS);
  }

  private static final Point3 RIGHT_FACE[] = {
      new Point3(0.5f, -0.5f, 0.5f),  // front bottom
      new Point3(0.5f, -0.5f, -0.5f),  // rear bottom
      new Point3(0.5f, 0.5f, -0.5f),  // rear top
      new Point3(0.5f, 0.5f, 0.5f)  // front top
  };

  private void addRightFace(VertexIndexTextureList vitList, Block block) {
    vitList.addFace(block, RIGHT_FACE, FACE_DRAW_LIST_IDXS, SIDE_FACE_TEXTURE_COORDS);
  }

  private static final Point3 BACK_FACE[] = {
      new Point3(0.5f, -0.5f, -0.5f),  // bottom right
      new Point3(-0.5f, -0.5f, -0.5f),  // bottom left
      new Point3(-0.5f, 0.5f, -0.5f),  // top left
      new Point3(0.5f, 0.5f, -0.5f)  // top right
  };

  private void addBackFace(VertexIndexTextureList vitList, Block block) {
    vitList.addFace(block, BACK_FACE, FACE_DRAW_LIST_IDXS, SIDE_FACE_TEXTURE_COORDS);
  }

  private static final Point3 BOTTOM_FACE[] = {
      new Point3(-0.5f, -0.5f, -0.5f),  // rear left
      new Point3(0.5f, -0.5f, -0.5f),  // rear right
      new Point3(0.5f, -0.5f, 0.5f),  // front right
      new Point3(-0.5f, -0.5f, 0.5f)  // front left
  };

  // Flip top and bottom since bitmaps are loaded upside down.
  private static final float[] BOTTOM_FACE_TEXTURE_COORDS = {
      0.0f, 0.5f,
      0.5f, 0.5f,
      0.5f, 0.0f,
      0.0f, 0.0f,
  };

  private void addBottomFace(VertexIndexTextureList vitList, Block block) {
    vitList.addFace(block, BOTTOM_FACE, FACE_DRAW_LIST_IDXS, BOTTOM_FACE_TEXTURE_COORDS);
  }

  private static final String VERTEX_SHADER_GLSL =
      "uniform mat4 mvpMatrix;\n" +
      "attribute vec4 position;\n" +
      "attribute vec2 textureCoord;\n" +
      "varying vec2 shared_textureCoord;\n" +
      "\n" +
      "void main() {\n" +
      "  gl_Position = mvpMatrix * position;\n" +
      "  shared_textureCoord = textureCoord;\n" +
      "}";

  private static final String FRAGMENT_SHADER_GLSL =
      "precision mediump float;\n" +
      "uniform sampler2D texture;\n" +
      "varying vec2 shared_textureCoord;\n" +
      "\n" +
      "void main() {\n" +
      "  gl_FragColor = texture2D(texture, shared_textureCoord);\n" +
      "}\n";

  void surfaceCreated(Resources resources) {
    program = GlHelper.linkProgram(VERTEX_SHADER_GLSL, FRAGMENT_SHADER_GLSL);
    GLES20.glUseProgram(program);

    mvpMatrixHandle = GLES20.glGetUniformLocation(program, "mvpMatrix");

    positionHandle = GLES20.glGetAttribLocation(program, "position");
    GLES20.glEnableVertexAttribArray(positionHandle);

    textureCoordHandle = GLES20.glGetAttribLocation(program, "textureCoord");
    GLES20.glEnableVertexAttribArray(textureCoordHandle);

    textureData = GlHelper.loadTexture(resources, R.drawable.atlas);
    int textureHandle = GLES20.glGetUniformLocation(program, "texture");
    GLES20.glUniform1i(textureHandle, 0);
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureData);
  }

  void draw(float[] viewProjectionMatrix) {
    GLES20.glUseProgram(program);

    // Since model matrix is identity, MVP matrix is the same as VP matrix.
    GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, viewProjectionMatrix, 0);

    // Draw all squares.
    if (drawListBuffer.limit() > 0) {
      GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
      GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureCoordBuffer);

      GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawListBuffer.limit(), GLES20.GL_UNSIGNED_SHORT,
          drawListBuffer);
    }
  }
}
