package com.skligys.cardboardcreeper;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class GlRenderer implements GLSurfaceView.Renderer {
  private static final String TAG = "GlRenderer";

  private final Resources resources;
  private final float[] projectionMatrix = new float[16];
  private final World world;

  GlRenderer(Resources resources) {
    this.resources = resources;
    Log.i(TAG, "-----------------------------------------------------------------");
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

    // Notify shapes.
    world.surfaceCreated(resources);
  }

  private static final float FIELD_OF_VIEW = (float) Math.toRadians(70.0f);  // radians
  private static final float NEAR_PLANE = 0.1f;
  private static final float FAR_PLANE = 60.0f;

  @Override
  public void onSurfaceChanged(GL10 unused, int width, int height) {
    GLES20.glViewport(0, 0, width, height);

    float aspect = (float) width / height;
    float top = NEAR_PLANE * (float) Math.tan(FIELD_OF_VIEW / 2.0f);
    float bottom = -top;
    float left = bottom * aspect;
    float right = -left;
    Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, NEAR_PLANE, FAR_PLANE);
  }

  @Override
  public void onDrawFrame(GL10 unused) {
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    // Draw shapes.
    world.draw(projectionMatrix);
  }

  void drag(float dx, float dy) {
    world.drag(dx, dy);
  }

  void walk(boolean start) {
    world.walk(start);
  }
}
