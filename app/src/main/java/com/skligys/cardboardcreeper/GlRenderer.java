package com.skligys.cardboardcreeper;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class GlRenderer implements GLSurfaceView.Renderer {
  private final Resources resources;

  private final float[] projectionMatrix = new float[16];
  private final float[] viewMatrix = new float[16];
  private final float[] viewProjectionMatrix = new float[16];

  private final World world;

  GlRenderer(Resources resources) {
    this.resources = resources;
    world = new World();
  }

  @Override
  public void onSurfaceCreated(GL10 unused, EGLConfig config) {
    // Background color: sky.
    GLES20.glClearColor(0.5f, 0.69f, 1.0f, 1.0f);

    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    GLES20.glDepthFunc(GLES20.GL_LEQUAL);
    GLES20.glFrontFace(GLES20.GL_CCW);

    GLES20.glEnable(GLES20.GL_CULL_FACE);
    GLES20.glCullFace(GLES20.GL_BACK);

    // Stationary camera is located at (0, 0) height 2.1 (feet to eye 1.6 + 0.5 displacement from
    // block feet are on), looking in negative z axis direction.
    Matrix.setLookAtM(viewMatrix, 0,
        0.0f, 2.1f, 0.0f,
        0.0f, 2.1f, -1.0f,
        0.0f, 1.0f, 0.0f);

    // Notify shapes.
    world.surfaceCreated(resources);
  }

  @Override
  public void onSurfaceChanged(GL10 unused, int width, int height) {
    GLES20.glViewport(0, 0, width, height);

    float ratio = (float) width / height;
    Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 15);
    Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
  }

  @Override
  public void onDrawFrame(GL10 unused) {
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    // Draw shapes.
    world.draw(viewProjectionMatrix);
  }
}
