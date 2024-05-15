package com.example.laco;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Base64;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ReceiveImageTask implements Callable<Void> {

  private final MainActivity mainActivity;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private Future<Void> future;

  private Socket socket;
  private BufferedReader reader;

  private final String serverIp;
  private final int serverPort;

  public ReceiveImageTask(String serverIp, int serverPort, MainActivity mainActivity) {
    this.serverIp = serverIp;
    this.serverPort = serverPort;
    this.mainActivity = mainActivity;

    executor.submit(() -> {
      try {
        InetAddress address = InetAddress.getByName(serverIp);
        socket = new Socket();
        socket.connect(new InetSocketAddress(address, serverPort), 5000);
        Log.i("ReceiveImageTask", "Connected to server at " + serverIp + ":" + serverPort);

        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      } catch (IOException e) {
        Log.e("ReceiveImageTask", "Error connecting to server", e);
      }
    });
  }

  @Override
  public Void call() {
    try {
      Log.i("ReceiveImageTask", "Connected to server at " + serverIp + ":" + serverPort);

      String line;
      while (true) {
        try {
          line = reader.readLine();
        } catch (IOException e) {
          Log.e("ReceiveImageTask", "Error reading from server", e);
          continue;
        }
        if (line == null) {
          continue;
        }
        if (line.equals("END")) {
          continue;
        }
        byte[] receivedBytes = Base64.getDecoder().decode(line);
        Log.i("ReceiveImageTask", "Received " + receivedBytes.length + " bytes from server");
        Bitmap bitmap = BitmapFactory.decodeByteArray(receivedBytes, 0, receivedBytes.length);
        mainActivity.runOnUiThread(() -> mainActivity.updateImage(bitmap));
      }
    } catch (Exception e) {
      Log.e("ReceiveImageTask", "Error in call method", e);
    }
    return null;
  }

  public void execute() {
    future = executor.submit(this);
  }

  public void cancel() {
    if (future != null) {
      future.cancel(true);
    }
  }

  public void shutdown() {
    executor.shutdown();
  }
}
