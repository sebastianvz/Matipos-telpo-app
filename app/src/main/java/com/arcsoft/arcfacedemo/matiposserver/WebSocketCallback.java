package com.arcsoft.arcfacedemo.matiposserver;

public interface WebSocketCallback {
    void onMessageReceived(String message);
    void onConnectionOpened();
    void onConnectionClosed();
    void onError(Throwable t);
}
