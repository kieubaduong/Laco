package com.example.laco;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

  private ImageView imageView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    imageView = findViewById(R.id.imageView);

    new ReceiveImageTask("192.168.1.8", 5657, this).execute();
  }

  public void updateImage(Bitmap bitmap) {
    imageView.setImageBitmap(bitmap);
  }
}