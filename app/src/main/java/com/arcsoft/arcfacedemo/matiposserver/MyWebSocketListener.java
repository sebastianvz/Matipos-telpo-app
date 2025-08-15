package com.arcsoft.arcfacedemo.matiposserver;

import static android.provider.Settings.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MyWebSocketListener extends WebSocketListener {

    private final Context context;
    private final WebSocketManager manager;
    private final WebSocketCallback callback;

    public MyWebSocketListener(Context context, WebSocketManager manager, WebSocketCallback callback) {
        this.context = context;
        this.manager = manager;
        this.callback = callback;
    }

    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
        @SuppressLint("HardwareIds") String message = "{\"serial\": \"" + Secure.getString(
                context.getContentResolver(),
                Secure.ANDROID_ID
        ) + "\"}";
        webSocket.send(message);
        if (callback != null) callback.onConnectionOpened();
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
        if (callback != null) callback.onMessageReceived(text);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, @NonNull String reason) {
        if (callback != null) callback.onConnectionClosed();
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
        if (callback != null) callback.onError(t);
        manager.reconnect();
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
    }
}

