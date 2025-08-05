package com.arcsoft.arcfacedemo.matiposserver;

import android.content.Context;
import android.util.Log;

import com.arcsoft.arcfacedemo.util.ConfigUtil;

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
    private static String authToken = null;
    private final WebSocketCallback callback; // nuevo
    private final Context context;

    public WebSocketManager(Context context, WebSocketCallback callback) {
        this.context = context;

        // Get URL From config
        String url = ConfigUtil.getUrlBaseFaceRepository(context);
        url = url.replace("http://", "ws://");
        url = url + "/ws";
        this.socketUrl = url;

        this.callback = callback;
        this.client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public void connect() {
        Request.Builder builder = new Request.Builder()
                .url(socketUrl);

        if (authToken != null) {
            builder.addHeader("Authorization", "Bearer " + authToken);
        }

        Request request = builder.build();

        MyWebSocketListener listener = new MyWebSocketListener(context,this, callback);
        webSocket = client.newWebSocket(request, listener);
    }

    public void reconnect() {
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
