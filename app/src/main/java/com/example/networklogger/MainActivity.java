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
            isBound = false;
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

        handler.post(updateTimeTask); // Start the update task
    }

    private Runnable updateTimeTask = new Runnable() {
        public void run() {
            updateNetworkStatus();
            updateTotalConnectedTime();
            simulateDateSelection(); // Simulate date selection every second
            handler.postDelayed(this, 1000); // Refresh every second
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
    }

    private void updateTotalConnectedTime() {
        if (isBound && connectionService != null) {
            long totalConnectedTimeMillis = connectionService.getTotalConnectedTime();
            String formattedTime = formatMillisToTime(totalConnectedTimeMillis);
            connectedTime.setText("Total Connected Time: " + formattedTime);
        }
    }

    private String formatMillisToTime(long millis) {
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (millis % (1000 * 60)) / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void updateHeartbeatWave(int year, int month, int dayOfMonth) {
        if (isBound && connectionService != null) {
            List<ConnectionEvent> events = connectionService.getConnectionEventsForDate(year, month, dayOfMonth);
            heartbeatWaveView.setConnectionEvents(events);
        }
    }

    private void simulateDateSelection() {
        // Get current date
        Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH);

        // Set selected date programmatically
        calendarView.setDate(currentDate.getTimeInMillis(), true, true);
        updateHeartbeatWave(year, month, dayOfMonth); // Update heartbeat wave for the selected date
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        handler.removeCallbacks(updateTimeTask); // Remove callback when activity is destroyed
    }
}
