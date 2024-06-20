package com.example.networklogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (isOnline(context)) {
            Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
            // Handle connected event
        } else {
            Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show();
            // Handle disconnected event
        }
    }

    public boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
