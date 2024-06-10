package com.example.laco;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CustomImageView extends androidx.appcompat.widget.AppCompatImageView {
    private static final String BASE_URL = "http://192.168.92.58:8080/";

    private float startX;
    private float startY;
    private float initialY;
    private boolean isDragEvent = false;
    private final Handler handler = new Handler();
    private static final int DOUBLE_CLICK_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();
    private long lastClickTime = 0;
    private int clickCount = 0;

    public CustomImageView(Context context) {
        super(context);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime <= DOUBLE_CLICK_TIMEOUT) {
                    clickCount++;
                } else {
                    clickCount = 1;
                }
                lastClickTime = currentTime;
                startX = event.getX();
                startY = event.getY();
                isDragEvent = true;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    initialY = event.getY(1);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (isDragEvent) {
                    float endX = event.getX();
                    float endY = event.getY();

                    if (isADrag(startX, endX, startY, endY)) {
//            handleDrag(startX, startY, endX, endY, BASE_URL + "drag", "Send drag");
                    } else if (System.currentTimeMillis() - lastClickTime
                            > ViewConfiguration.getLongPressTimeout()) {
                        handleTap(event, BASE_URL + "rightClick", "Send long press");
                    } else if (clickCount == 2) {
                        handleTap(event, BASE_URL + "doubleClick", "Send double click");
                        clickCount = 0;
                    } else {
                        handleTap(event, BASE_URL + "singleClick", "Send single click");
                    }

                    isDragEvent = false;
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (isDragEvent && isADrag(startX, event.getX(), startY, event.getY())) {
                    handler.removeCallbacksAndMessages(null);
                }
                break;


            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2) {
                    float currentY = event.getY(1);
                    int scrollAmount = (int) (initialY - currentY);
                    sendScrollAmount(scrollAmount);
                }

            case MotionEvent.ACTION_CANCEL:
                isDragEvent = false;
                handler.removeCallbacksAndMessages(null);
                break;
        }

        performClick();
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void handleTap(MotionEvent e, String url, String logTag) {
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

    }

    private void handleDrag(float startX, float startY, float endX, float endY, String url,
                            String logTag) {
        float scaleXStart = startX / getWidth();
        float scaleYStart = startY / getHeight();
        float scaleXEnd = endX / getWidth();
        float scaleYEnd = endY / getHeight();

        Log.d(logTag, "Drag from " + startX + "," + startY + " to " + endX + "," + endY);
        Log.d(logTag,
                "Scaled from " + scaleXStart + "," + scaleYStart + " to " + scaleXEnd + "," + scaleYEnd);

        new Thread(() -> {
            try {
                URL endpoint = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; utf-8");

                String jsonInputString =
                        "{\"startX\": " + scaleXStart + ", \"startY\": " + scaleYStart + ", \"endX\": "
                                + scaleXEnd + ", \"endY\": " + scaleYEnd + "}";
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

    private void sendScrollAmount(int amount) {
        new Thread(() -> {
            try {
                URL endpoint = new URL(BASE_URL + "scroll");
                HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; utf-8");

                String jsonInputString = "{\"amount\": " + amount + "}";
                Log.d("SendScrollAmount", "Sending " + jsonInputString);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                Log.d("SendScrollAmount", "Response code: " + responseCode);

                connection.disconnect();
            } catch (Exception ex) {
                Log.e("SendScrollAmount", "Error sending scroll amount", ex);
            }
        }).start();
    }
}
