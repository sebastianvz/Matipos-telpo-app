package com.arcsoft.arcfacedemo.matiposserver;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class WebSocketManager {

    private static final String TAG = "WebSocketManager";
    private static final int RECONNECT_DELAY_SECONDS = 5;

    private WebSocket webSocket;
    private final OkHttpClient client;
    private final String socketUrl;
    private final String authToken;
    private final WebSocketCallback callback; // nuevo

    public WebSocketManager(String socketUrl, String authToken, WebSocketCallback callback) {
        this.socketUrl = socketUrl;
        this.authToken = authToken;
        this.callback = callback;
        this.client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    public void connect() {
        Request request = new Request.Builder()
                .url(socketUrl)
                .addHeader("Authorization", "Bearer " + authToken)
                .build();

        MyWebSocketListener listener = new MyWebSocketListener(this, callback); // pasa el callback
        webSocket = client.newWebSocket(request, listener);
    }

    public void reconnect() {
        Log.d(TAG, "Reconectando en " + RECONNECT_DELAY_SECONDS + " segundos...");
        new Thread(() -> {
            try {
                Thread.sleep(RECONNECT_DELAY_SECONDS * 1000);
                connect();
            } catch (InterruptedException e) {
                Log.e(TAG, "Reconexión interrumpida", e);
            }
        }).start();
    }

    public void send(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        }
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, "App cerrada");
        }
    }
}
