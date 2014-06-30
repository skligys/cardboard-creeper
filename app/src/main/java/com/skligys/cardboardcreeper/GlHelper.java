package com.skligys.cardboardcreeper;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

class GlHelper {
  private static final String TAG = "GlHelper";

  private GlHelper() {}  // No instantiation.

  private static final int FLOAT_SIZE_IN_BYTES = 4;

  static FloatBuffer createFloatBuffer(float[] from) {
    FloatBuffer result = ByteBuffer.allocateDirect(FLOAT_SIZE_IN_BYTES * from.length)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer();
    result.put(from)
        .position(0);
    return result;
  }

  private static final int SHORT_SIZE_IN_BYTES = 2;

  static ShortBuffer createShortBuffer(short[] from) {
    ShortBuffer result = ByteBuffer.allocateDirect(SHORT_SIZE_IN_BYTES * from.length)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer();
    result.put(from)
        .position(0);
    return result;
  }

  /**
   * @param type  Must be one of GLES20.GL_VERTEX_SHADER or GLES20.GL_FRAGMENT_SHADER).
   */
  private static int loadShader(int type, String glsl) {
    if (type != GLES20.GL_VERTEX_SHADER && type != GLES20.GL_FRAGMENT_SHADER) {
      failIllegalArgument("Unsupported shader type %d", type);
    }

    int shader = GLES20.glCreateShader(type);
    if (shader == 0) {
      fail("Failed to create a shader of type %d", type);
    }

    GLES20.glShaderSource(shader, glsl);
    GLES20.glCompileShader(shader);

    // Get compilation status.
    int[] status = new int[] { GLES20.GL_FALSE };
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
    if (status[0] == GLES20.GL_FALSE) {
      GLES20.glGetShaderiv(shader, GLES20.GL_INFO_LOG_LENGTH, status, 0);
      Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
      GLES20.glDeleteShader(shader);
      fail("Failed to compile a shader of type %d, status: %d", type, status[0]);
    }

    return shader;
  }

  static int linkProgram(String vertexShaderGlsl, String fragmentShaderGlsl) {
    int vertexShader = GlHelper.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderGlsl);
    int fragmentShader = GlHelper.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderGlsl);

    int program = GLES20.glCreateProgram();
    if (program == 0) {
      throw new RuntimeException("Failed to create a program");
    }

    GLES20.glAttachShader(program, vertexShader);
    GLES20.glAttachShader(program, fragmentShader);
    GLES20.glLinkProgram(program);

    // Get link status.
    int[] status = new int[] { GLES20.GL_FALSE };
    GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0);
    if (status[0] == GLES20.GL_FALSE) {
      GLES20.glGetProgramiv(program, GLES20.GL_INFO_LOG_LENGTH, status, 0);
      Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(program));
      GLES20.glDeleteProgram(program);
      throw new RuntimeException("Failed to link program");
    }

    GLES20.glDeleteShader(vertexShader);
    GLES20.glDeleteShader(fragmentShader);
    return program;
  }

  static int loadCubeTextureTopBottomSides(Resources resources,
      int topResourceId, int bottomResourceId, int sidesResourceId) {
    int textureHandles[] = new int[1];
    GLES20.glGenTextures(1, textureHandles, 0);
    if (textureHandles[0] == 0) {
      throw new RuntimeException("Failed to create a texture");
    }
    GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureHandles[0]);

    Bitmap topBitmap = loadBitmap(resources, topResourceId);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, topBitmap, 0);
    topBitmap.recycle();

    Bitmap bottomBitmap = loadBitmap(resources, bottomResourceId);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, bottomBitmap, 0);
    bottomBitmap.recycle();

    Bitmap sideBitmap = loadBitmap(resources, sidesResourceId);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, sideBitmap, 0);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, sideBitmap, 0);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, sideBitmap, 0);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, sideBitmap, 0);
    sideBitmap.recycle();

    GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_CUBE_MAP);

    GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP,
        GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP,
        GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP,
        GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP,
        GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

    return textureHandles[0];
  }

  static int loadCubeTexture(Resources resources, int resourceId) {
    int textureHandles[] = new int[1];
    GLES20.glGenTextures(1, textureHandles, 0);
    if (textureHandles[0] == 0) {
      throw new RuntimeException("Failed to create a texture");
    }
    GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureHandles[0]);

    Bitmap bitmap = loadBitmap(resources, resourceId);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, bitmap, 0);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, bitmap, 0);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, bitmap, 0);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, bitmap, 0);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, bitmap, 0);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, bitmap, 0);
    bitmap.recycle();

    GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_CUBE_MAP);

    GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP,
        GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP,
        GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP,
        GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP,
        GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

    return textureHandles[0];
  }

  private static Bitmap loadBitmap(Resources resources, int resourceId) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inScaled = false;   // No pre-scaling
    Bitmap result = BitmapFactory.decodeResource(resources, resourceId, options);
    if (result == null) {
      fail("Failed to decode bitmap from resource %d", resourceId);
    }
    return result;
  }

  private static void fail(String format, Object... params) {
    throw new RuntimeException(String.format(format, params));
  }

  private static void failIllegalArgument(String format, Object... params) {
    throw new IllegalArgumentException(String.format(format, params));
  }
}
