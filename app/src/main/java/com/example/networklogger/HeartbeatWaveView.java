package com.example.networklogger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
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

    // Constants for zooming
    private static final float MIN_SCALE_FACTOR = 0.5f;
    private static final float MAX_SCALE_FACTOR = 2.0f;
    private float scaleFactor = 1.0f;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

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

        // Initialize gesture detectors for zooming
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        gestureDetector = new GestureDetector(getContext(), new GestureListener());
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
        float scaledXInterval = xInterval * scaleFactor;

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Draw scaled square waves and time labels
        for (int i = 1; i < eventCount; i++) {
            ConnectionEvent prevEvent = connectionEvents.get(i - 1);
            ConnectionEvent event = connectionEvents.get(i);

            float startX = (i - 1) * scaledXInterval;
            float startY = 0; // Start from top
            float endX = i * scaledXInterval;
            float endY = height; // Extend to bottom

            // Draw box
            Paint paint = event.isConnected ? connectedPaint : disconnectedPaint;
            RectF rect = new RectF(startX, startY, endX, endY);
            canvas.drawRect(rect, paint);

            // Draw time labels at the center of the box
            long startTime = prevEvent.timestamp;
            long endTime = event.timestamp;

            String startTimeLabel = timeFormat.format(startTime);
            String endTimeLabel = timeFormat.format(endTime);

            float textX = (startX + endX) / 2;
            float textY = height / 2;
            canvas.drawText(startTimeLabel, textX, textY, textPaint);
            canvas.drawText(endTimeLabel, textX, textY + textPaint.getTextSize(), textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(MIN_SCALE_FACTOR, Math.min(scaleFactor, MAX_SCALE_FACTOR));
            invalidate();
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            scaleFactor = 1.0f;
            invalidate();
            return true;
        }
    }
}
