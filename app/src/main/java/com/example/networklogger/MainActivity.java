package com.example.networklogger;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView networkStatus;
    private TextView connectedTime;
    private CalendarView calendarView;
    private ImageView statusImage;
    private HeartbeatWaveView heartbeatWaveView;
    private ConnectionService connectionService;
    private boolean isBound = false;
    private Handler handler = new Handler();
    private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ConnectionService.LocalBinder binder = (ConnectionService.LocalBinder) service;
            connectionService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound
                    = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        networkStatus = findViewById(R.id.network_status);
        connectedTime = findViewById(R.id.connected_time);
        calendarView = findViewById(R.id.calendar_view);
        statusImage = findViewById(R.id.status_image);
        heartbeatWaveView = findViewById(R.id.heartbeat_wave_view);

        Intent intent = new Intent(this, ConnectionService.class);
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                updateHeartbeatWave(year, month, dayOfMonth);
            }
        });

        handler.postDelayed(updateTimeTask, 1000);
    }

    private Runnable updateTimeTask = new Runnable() {
        public void run() {
            updateNetworkStatus();
            handler.postDelayed(this, 1000);
        }
    };

    private void updateNetworkStatus() {
        if (networkChangeReceiver.isOnline(this)) {
            networkStatus.setText("Network Status: Connected");
            networkStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            statusImage.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            networkStatus.setText("Network Status: Disconnected");
            networkStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            statusImage.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        if (isBound && connectionService != null) {
            long totalConnectedTime = connectionService.getTotalConnectedTime();
            connectedTime.setText("Total Connected Time: " + totalConnectedTime / 1000 + "s");
        }
    }

    private void updateHeartbeatWave(int year, int month, int dayOfMonth) {
        if (isBound && connectionService != null) {
            List<ConnectionEvent> events = connectionService.getConnectionEventsForDate(year, month, dayOfMonth);
            heartbeatWaveView.setConnectionEvents(events);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
}
