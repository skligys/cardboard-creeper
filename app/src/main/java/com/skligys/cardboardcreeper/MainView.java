package com.skligys.cardboardcreeper;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.KeyEvent;
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

    // To make sure we get key notifications while scrolling around by touch.
    setFocusableInTouchMode(true);
  }

  @Override
  public boolean onTouchEvent(MotionEvent e) {
    float x = e.getX();
    float y = e.getY();
    // Only get drag events and pass x and y differences to the eye.
    if (e.getAction() == MotionEvent.ACTION_MOVE) {
      final float dx = (x - prevX) / screenDensity;
      final float dy = (y - prevY) / screenDensity;

      // This callback runs in UI thread while GL rendering runs in its own thread.
      // queueEvent() posts asynchronous requests to GL thread.
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

  @Override
  /** Volume up key means "walk forward" to simulate Cardboard's single button. */
  public boolean dispatchKeyEvent(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.KEYCODE_VOLUME_UP:
        processVolumeUp(e.getAction(), e.getRepeatCount());
        return true;
      default:
        return super.dispatchKeyEvent(e);
    }
  }

  private void processVolumeUp(int action, int repeatCount) {
    // On long press, we receive a sequence of ACTION_DOWN, ignore all after the first one.
    if (repeatCount > 0) {
      return;
    }

    switch (action) {
      case KeyEvent.ACTION_DOWN:
        // This callback runs in UI thread while GL rendering runs in its own thread.
        // queueEvent() posts asynchronous requests to GL thread.
        queueEvent(new Runnable() {
          @Override public void run() {
            renderer.walk(true);
          }
        });
        break;
      case KeyEvent.ACTION_UP:
        queueEvent(new Runnable() {
          @Override public void run() {
            renderer.walk(false);
          }
        });
        break;
    }
  }
}
