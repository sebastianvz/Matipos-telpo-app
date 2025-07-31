package com.arcsoft.arcfacedemo.matiposserver;

import android.util.Log;

import androidx.annotation.NonNull;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MyWebSocketListener extends WebSocketListener {

    private final WebSocketManager manager;
    private final WebSocketCallback callback;

    public MyWebSocketListener(WebSocketManager manager, WebSocketCallback callback) {
        this.manager = manager;
        this.callback = callback;
    }

    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
        Log.d("WebSocket", "Conexión abierta");
        if (callback != null) callback.onConnectionOpened();
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
        Log.d("WebSocket", "Mensaje recibido: " + text);
        if (callback != null) callback.onMessageReceived(text);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, @NonNull String reason) {
        Log.d("WebSocket", "Cerrando: " + code + " / " + reason);
        if (callback != null) callback.onConnectionClosed();
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
        Log.e("WebSocket", "Error: " + t.getMessage(), t);
        if (callback != null) callback.onError(t);
        manager.reconnect();
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        Log.d("WebSocket", "Conexión cerrada: " + code + " / " + reason);
    }
}

