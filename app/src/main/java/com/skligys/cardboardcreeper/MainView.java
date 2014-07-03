package com.skligys.cardboardcreeper;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

class MainView extends GLSurfaceView {
  final float screenDensity;
  final GlRenderer renderer;
  float prevX;
  float prevY;

  public MainView(Context context, float screenDensity){
    super(context);

    this.screenDensity = screenDensity;
    setEGLContextClientVersion(2);

    this.renderer = new GlRenderer(this.getResources());
    setRenderer(this.renderer);
  }

  @Override
  public boolean onTouchEvent(MotionEvent e) {
    float x = e.getX();
    float y = e.getY();
    // Only get drag events and pass x and y differences to the eye.
    if (e.getAction() == MotionEvent.ACTION_MOVE) {
      final float dx = (x - prevX) / screenDensity;
      final float dy = (y - prevY) / screenDensity;

      queueEvent(new Runnable() {
        @Override public void run() {
          renderer.drag(dx, dy);
        }
      });

    }

    prevX = x;
    prevY = y;
    return true;
  }
}
