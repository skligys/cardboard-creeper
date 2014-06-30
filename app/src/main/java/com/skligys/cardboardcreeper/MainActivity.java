package com.skligys.cardboardcreeper;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class MainActivity extends Activity {
  private GLSurfaceView view;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    checkForOpenGlEs20Support();

    view = new MainView(this);
    setContentView(view);
  }

  @Override
  protected void onPause() {
    super.onPause();
    view.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    view.onResume();
  }

  private void checkForOpenGlEs20Support() {
    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
    boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
    if (!supportsEs2) {
      throw new RuntimeException("Your device does not support OpenGL ES 2.0");
    }
  }

  private static class MainView extends GLSurfaceView {
    public MainView(Context context){
      super(context);
      setEGLContextClientVersion(2);
      setRenderer(new GlRenderer(this.getResources()));
    }
  }
}
