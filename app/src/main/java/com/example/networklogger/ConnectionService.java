package com.example.networklogger;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ConnectionService extends Service {

    private final IBinder binder = new LocalBinder();
    private BroadcastReceiver networkReceiver;
    private long connectedStartTime;
    private long totalConnectedTime;
    private List<ConnectionEvent> connectionEvents = new ArrayList<>();

    public class LocalBinder extends Binder {
        ConnectionService getService() {
            return ConnectionService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        networkReceiver = new NetworkChangeReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isConnected = isOnline(context);
                long currentTime = System.currentTimeMillis();
                connectionEvents.add(new ConnectionEvent(currentTime, isConnected));
                if (isConnected) {
                    connectedStartTime = currentTime;
                } else {
                    totalConnectedTime += currentTime - connectedStartTime;
                }
            }
        };
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkReceiver, filter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkReceiver);
    }

    public long getTotalConnectedTime() {
        return totalConnectedTime;
    }

    public List<ConnectionEvent> getConnectionEventsForDate(int year, int month, int day) {
        Calendar startOfDay = Calendar.getInstance();
        startOfDay.set(year, month, day, 0, 0, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);

        Calendar endOfDay = Calendar.getInstance();
        endOfDay.set(year, month, day, 23, 59, 59);
        endOfDay.set(Calendar.MILLISECOND, 999);

        List<ConnectionEvent> eventsForDate = new ArrayList<>();
        for (ConnectionEvent event : connectionEvents) {
            if (event.timestamp >= startOfDay.getTimeInMillis() && event.timestamp <= endOfDay.getTimeInMillis()) {
                eventsForDate.add(event);
            }
        }
        return eventsForDate;
    }
}
