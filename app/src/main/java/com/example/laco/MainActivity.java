package com.example.laco;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

  private ImageView imageView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    imageView = findViewById(R.id.imageView);

    new ReceiveImageTask("192.168.1.9", 5657, this).execute();

    imageView.post(new Runnable() {
      @Override
      public void run() {
        Log.d("ImageView Size", "Width: " + imageView.getWidth() + ", Height: " + imageView.getHeight());
      }
    });

    imageView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          float x = event.getX();
          float y = event.getY();

          float scaleX;

          float middle = imageView.getWidth() / 2;
          if (x < middle) {
            scaleX = (x - (2136 - 1920) / 2) / 1920;
          }
          else {
            scaleX = 0.5f + (x - middle) / 1920;
          }

          float scaleY = y / imageView.getHeight();

          Log.d("Send click", "Image view: " + imageView.getWidth() + "x" + imageView.getHeight());
          Log.d("Send click", "Click at " + x + "x" + y + " (scaled to " + scaleX + "x" + scaleY + ")");

          new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                URL url = new URL("http://192.168.1.9:8080/");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; utf-8");

                String jsonInputString = "{\"scaleX\": " + scaleX + ", \"scaleY\": " + scaleY + "}";
                Log.d("Send click", "Sending " + jsonInputString);

                try (OutputStream os = connection.getOutputStream()) {
                  byte[] input = jsonInputString.getBytes("utf-8");
                  os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                Log.d("Send click", "Response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                  // Do something with the response (if needed)
                }

                connection.disconnect();
              } catch (Exception e) {
                Log.e("Send click", "Error sending click", e);
              }
            }
          }).start();

          return true;
        }

        return false;
      }
    });
  }

  public void updateImage(Bitmap bitmap) {
    imageView.setImageBitmap(bitmap);
  }
}