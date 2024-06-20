package com.example.networklogger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HeartbeatWaveView extends View {
    private List<ConnectionEvent> connectionEvents;
    private Paint connectedPaint;
    private Paint disconnectedPaint;
    private Paint textPaint;
    private Paint backgroundPaint;

    public HeartbeatWaveView(Context context) {
        super(context);
        init();
    }

    public HeartbeatWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeartbeatWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        connectedPaint = new Paint();
        connectedPaint.setColor(getResources().getColor(android.R.color.holo_green_dark));
        connectedPaint.setStyle(Paint.Style.FILL);

        disconnectedPaint = new Paint();
        disconnectedPaint.setColor(getResources().getColor(android.R.color.holo_red_dark));
        disconnectedPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(getResources().getColor(android.R.color.white));
        textPaint.setTextSize(30);
        textPaint.setTextAlign(Paint.Align.CENTER);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(getResources().getColor(android.R.color.transparent));
    }

    public void setConnectionEvents(List<ConnectionEvent> events) {
        this.connectionEvents = events;
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (connectionEvents == null || connectionEvents.isEmpty()) {
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int eventCount = connectionEvents.size();

        float xInterval = (float) width / (eventCount - 1);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Draw square waves and time labels
        for (int i = 1; i < eventCount; i++) {
            ConnectionEvent prevEvent = connectionEvents.get(i - 1);
            ConnectionEvent event = connectionEvents.get(i);

            float startX = (i - 1) * xInterval;
            float startY = prevEvent.isConnected ? height / 4 : (3 * height / 4);
            float endX = i * xInterval;
            float endY = event.isConnected ? height / 4 : (3 * height / 4);

            // Draw square wave
            Paint paint = event.isConnected ? connectedPaint : disconnectedPaint;
            float squareWidth = endX - startX;
            RectF squareRect = new RectF(startX, startY, startX + squareWidth, startY + (height / 2));
            canvas.drawRect(squareRect, paint);

            // Draw time labels at the edges of the square wave
            long startTime = prevEvent.timestamp;
            long endTime = event.timestamp;

            String startTimeLabel = timeFormat.format(startTime);
            String endTimeLabel = timeFormat.format(endTime);

            canvas.drawText(startTimeLabel, startX, startY - 20, textPaint); // Draw at the start of the square wave
            canvas.drawText(endTimeLabel, endX, endY - 20, textPaint); // Draw at the end of the square wave
        }
    }
}
