package com.example.laco;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CustomImageView extends androidx.appcompat.widget.AppCompatImageView {

  private GestureDetector gestureDetector;
  private float startX;
  private float startY;
  private boolean isDragEvent = false;
  private boolean isLongPress = false;
  private Handler handler = new Handler();

  public CustomImageView(Context context) {
    super(context);
    init();
  }

  public CustomImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent e) {
        if (!isLongPress) {
          return handleTap(e, "http://192.168.1.6:8080/singleClick", "Send click");
        }
        isLongPress = false;
        return false;
      }

      @Override
      public boolean onDoubleTap(MotionEvent e) {
        if (!isLongPress) {
          return handleTap(e, "http://192.168.1.6:8080/doubleClick", "Send double click");
        }
        isLongPress = false;
        return false;
      }

      @Override
      public void onLongPress(MotionEvent e) {
        startX = e.getX();
        startY = e.getY();
        isDragEvent = true;

        // Delay the long press action
        handler.postDelayed(() -> {
          if (isDragEvent) {
            handleTap(e, "http://192.168.1.6:8080/rightClick", "Send right click");
            isLongPress = true;
          }
        }, ViewConfiguration.getLongPressTimeout());
      }
    });

    setOnTouchListener((v, event) -> {
      gestureDetector.onTouchEvent(event);
      if (event.getAction() == MotionEvent.ACTION_UP && isDragEvent) {
        float endX = event.getX();
        float endY = event.getY();

        if (isADrag(startX, endX, startY, endY)) {
          handleDrag(startX, startY, endX, endY, "http://192.168.1.6:8080/drag", "Send drag");
        }

        isDragEvent = false;
      }
      v.performClick();
      return true;
    });
  }

  private boolean handleTap(MotionEvent e, String url, String logTag) {
    float x = e.getX();
    float y = e.getY();

    float scaleX;

    float middle = getWidth() / 2f;
    if (x < middle) {
      scaleX = (x - (2136f - 1920f) / 2) / 1920f;
    } else {
      scaleX = 0.5f + (x - middle) / 1920;
    }

    float scaleY = y / getHeight();

    Log.d(logTag, "Image view: " + getWidth() + "x" + getHeight());
    Log.d(logTag,
        "Click at " + x + "x" + y + " (scaled to " + scaleX + "x" + scaleY + ")");

    new Thread(() -> {
      try {
        URL endpoint = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; utf-8");

        String jsonInputString = "{\"scaleX\": " + scaleX + ", \"scaleY\": " + scaleY + "}";
        Log.d(logTag, "Sending " + jsonInputString);

        try (OutputStream os = connection.getOutputStream()) {
          byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
          os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        Log.d(logTag, "Response code: " + responseCode);

        connection.disconnect();
      } catch (Exception ex) {
        Log.e(logTag, "Error sending click", ex);
      }
    }).start();

    return true;
  }

  private void handleDrag(float startX, float startY, float endX, float endY, String url, String logTag) {
    float scaleXStart = startX / getWidth();
    float scaleYStart = startY / getHeight();
    float scaleXEnd = endX / getWidth();
    float scaleYEnd = endY / getHeight();

    Log.d(logTag, "Drag from " + startX + "," + startY + " to " + endX + "," + endY);
    Log.d(logTag, "Scaled from " + scaleXStart + "," + scaleYStart + " to " + scaleXEnd + "," + scaleYEnd);

    new Thread(() -> {
      try {
        URL endpoint = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; utf-8");

        String jsonInputString = "{\"startX\": " + scaleXStart + ", \"startY\": " + scaleYStart + ", \"endX\": " + scaleXEnd + ", \"endY\": " + scaleYEnd + "}";
        Log.d(logTag, "Sending " + jsonInputString);

        try (OutputStream os = connection.getOutputStream()) {
          byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
          os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        Log.d(logTag, "Response code: " + responseCode);

        connection.disconnect();
      } catch (Exception ex) {
        Log.e(logTag, "Error sending drag", ex);
      }
    }).start();
  }

  private boolean isADrag(float startX, float endX, float startY, float endY) {
    // Calculate the distance moved
    float dx = Math.abs(endX - startX);
    float dy = Math.abs(endY - startY);
    // If the distance moved in either direction is significant enough, consider it a drag
    return dx > 10 || dy > 10;
  }
}
