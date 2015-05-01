package com.skligys.cardboardcreeper;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class MainActivity extends Activity {
  private GLSurfaceView view;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    checkForOpenGlEs20Support();

    DisplayMetrics displayMetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

    view = new MainView(this, displayMetrics.density);
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
}
