package com.example.networklogger;

public class ConnectionEvent {
    public long timestamp;
    public boolean isConnected;

    public ConnectionEvent(long timestamp, boolean isConnected) {
        this.timestamp = timestamp;
        this.isConnected = isConnected;
    }
}

