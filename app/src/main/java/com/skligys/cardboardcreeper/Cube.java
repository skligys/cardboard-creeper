package com.skligys.cardboardcreeper;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

class Cube {
  private final float[] modelMatrix = new float[16];
  private final float[] mvpMatrix = new float[16];
  private int mvpMatrixHandle;

  private final FloatBuffer vertexBuffer;
  private final ShortBuffer drawListBuffer;
  private final FloatBuffer faceBuffer;

  private int program;

  Cube() {
    // OpenGL coordinates:
    //        ^ y
    //        |     x
    //        +--->
    //   z   /
    //      v
    float[] vertices = {
        1.0f,  1.0f,  1.0f,    -1.0f,  1.0f,  1.0f,    -1.0f, -1.0f,  1.0f,     1.0f, -1.0f,  1.0f,
        1.0f,  1.0f,  1.0f,     1.0f, -1.0f,  1.0f,     1.0f, -1.0f, -1.0f,     1.0f,  1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,    -1.0f, -1.0f, -1.0f,    -1.0f,  1.0f, -1.0f,     1.0f,  1.0f, -1.0f,
       -1.0f,  1.0f,  1.0f,    -1.0f,  1.0f, -1.0f,    -1.0f, -1.0f, -1.0f,    -1.0f, -1.0f,  1.0f,
        1.0f,  1.0f,  1.0f,     1.0f,  1.0f, -1.0f,    -1.0f,  1.0f, -1.0f,    -1.0f,  1.0f,  1.0f,
        1.0f, -1.0f,  1.0f,    -1.0f, -1.0f,  1.0f,    -1.0f, -1.0f, -1.0f,     1.0f, -1.0f, -1.0f
    };
    short[] drawListIndices = {
        0,  1,  2,     0,  2,  3,
        4,  5,  6,     4,  6,  7,
        8,  9, 10,     8, 10, 11,
        12, 13, 14,    12, 14, 15,
        16, 17, 18,    16, 18, 19,
        20, 21, 22,    20, 22, 23
    };
    float[] faces = {
        1.0f,  1.0f,  1.0f,    -1.0f,  1.0f,  1.0f,    -1.0f, -1.0f,  1.0f,     1.0f, -1.0f,  1.0f,
        1.0f,  1.0f,  1.0f,     1.0f, -1.0f,  1.0f,     1.0f, -1.0f, -1.0f,     1.0f,  1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,    -1.0f, -1.0f, -1.0f,    -1.0f,  1.0f, -1.0f,     1.0f,  1.0f, -1.0f,
       -1.0f,  1.0f,  1.0f,    -1.0f,  1.0f, -1.0f,    -1.0f, -1.0f, -1.0f,    -1.0f, -1.0f,  1.0f,
        1.0f,  1.0f,  1.0f,     1.0f,  1.0f, -1.0f,    -1.0f,  1.0f, -1.0f,    -1.0f,  1.0f,  1.0f,
        1.0f, -1.0f,  1.0f,    -1.0f, -1.0f,  1.0f,    -1.0f, -1.0f, -1.0f,     1.0f, -1.0f, -1.0f,
    };

    vertexBuffer = GlHelper.createFloatBuffer(vertices);
    drawListBuffer = GlHelper.createShortBuffer(drawListIndices);
    faceBuffer = GlHelper.createFloatBuffer(faces);
  }

  private static final String VERTEX_SHADER_GLSL =
      "uniform mat4 mvpMatrix;\n" +
      "attribute vec4 position;\n" +
      "attribute vec3 faces;\n" +
      "varying vec3 shared_faces;\n" +
      "\n" +
      "void main() {\n" +
      "  gl_Position = mvpMatrix * position;\n" +
      "  shared_faces = faces;\n" +
      "}";

  private static final String FRAGMENT_SHADER_GLSL =
      "precision mediump float;\n" +
      "uniform samplerCube texture;\n" +
      "varying vec3 shared_faces;\n" +
      "\n" +
      "void main() {\n" +
      "  gl_FragColor = textureCube(texture, shared_faces);\n" +
      "}\n";

  void surfaceCreated(Resources resources) {
    program = GlHelper.linkProgram(VERTEX_SHADER_GLSL, FRAGMENT_SHADER_GLSL);
    GLES20.glUseProgram(program);

    mvpMatrixHandle = GLES20.glGetUniformLocation(program, "mvpMatrix");

    int facesHandle = GLES20.glGetAttribLocation(program, "faces");
    GLES20.glVertexAttribPointer(facesHandle, 3, GLES20.GL_FLOAT, false, 0, faceBuffer);
    GLES20.glEnableVertexAttribArray(facesHandle);

    int positionHandle = GLES20.glGetAttribLocation(program, "position");
    GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
    GLES20.glEnableVertexAttribArray(positionHandle);

    int m_textureId = GlHelper.loadCubeTextureTopBottomSides(resources,
        R.drawable.grass_top, R.drawable.dirt, R.drawable.grass_side);
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, m_textureId);

    int textureHandle = GLES20.glGetUniformLocation(program, "texture");
    GLES20.glUniform1i(textureHandle, 0);
  }

  void draw(float[] viewProjectionMatrix) {
    GLES20.glUseProgram(program);

    float angle = (SystemClock.uptimeMillis() / 15) % 360;
    Matrix.setRotateM(modelMatrix, 0, angle, 1.0f, 1.0f, 1.0f);
    Matrix.multiplyMM(mvpMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);

    GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
    GLES20.glDrawElements(GLES20.GL_TRIANGLES, 36, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
  }
}
