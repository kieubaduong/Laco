package com.example.laco;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

  private CustomImageView imageView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    imageView = findViewById(R.id.imageView);

    new ReceiveImageTask("192.168.1.6", 5657, this).execute();

    imageView.post(() -> Log.d("ImageView Size",
        "Width: " + imageView.getWidth() + ", Height: " + imageView.getHeight()));
  }

  public void updateImage(Bitmap bitmap) {
    imageView.setImageBitmap(bitmap);
  }
}