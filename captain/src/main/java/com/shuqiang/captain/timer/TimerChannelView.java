package com.shuqiang.captain.timer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import captain.R;

import java.util.List;
import java.util.Locale;

// 横屏计时器频道主画布：正计时为前景，天气、昼夜和水滴石穿为背景。
public final class TimerChannelView extends View {
    private static final int CONTROL_NONE = 0;
    private static final int CONTROL_LEFT = 1;
    private static final int CONTROL_RIGHT = 2;

    private final TimerTimeFormatter timeFormatter = new TimerTimeFormatter();
    private final TimerSession timerSession = new TimerSession();
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint secondaryTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint.FontMetrics timeMetrics = new Paint.FontMetrics();
    private final RectF leftButtonBounds = new RectF();
    private final RectF rightButtonBounds = new RectF();

    private TimerBackgroundPainter backgroundPainter;
    private TimerTopInfoPainter topInfoPainter;
    private TimerWaterPainter waterPainter;
    private TimerControlPainter controlPainter;
    private TimerTimeFormatter.TimeInfo timeInfo;
    private TimerWeatherInfo weatherInfo;
    private boolean rendering;
    private long lastSecond = Long.MIN_VALUE;
    private float density;
    private float timeTextRightX;
    private float timeTextHalfWidth;
    private float timeBaseline;
    private int pressedControl = CONTROL_NONE;

    public TimerChannelView(Context context) {
        super(context);
        init();
    }

    public TimerChannelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        density = getResources().getDisplayMetrics().density;
        Context context = getContext();
        backgroundPainter = new TimerBackgroundPainter(context, density);
        topInfoPainter = new TimerTopInfoPainter(context, density);
        waterPainter = new TimerWaterPainter(context, density);
        controlPainter = new TimerControlPainter(context, density);
        setKeepScreenOn(true);
        setClickable(true);
        textPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        secondaryTextPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
    }

    public void start() {
        if (rendering) {
            return;
        }
        rendering = true;
        postInvalidateOnAnimation();
    }

    public void stop() {
        rendering = false;
    }

    public void setWeatherInfo(@Nullable TimerWeatherInfo weatherInfo) {
        this.weatherInfo = weatherInfo;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long nowWallMs = System.currentTimeMillis();
        long nowElapsedMs = SystemClock.elapsedRealtime();
        updateTimeInfo(nowWallMs);
        if (timeInfo == null) {
            return;
        }
        boolean day = resolveDayMode();
        long elapsedMs = timerSession.elapsedMs(nowElapsedMs);
        float secondProgress = (elapsedMs % 1000L) / 1000f;
        backgroundPainter.drawBackground(canvas, getWidth(), getHeight(), timeInfo, day, weatherInfo, elapsedMs);
        topInfoPainter.drawTopLabels(canvas, getWidth(), timeInfo, weatherInfo, day, backgroundPainter);
        drawCenterTimer(canvas, day, elapsedMs);
        waterPainter.drawWaterAndStone(canvas, getWidth(), getHeight(), day, secondProgress,
                timeTextRightX, timeBaseline, timeMetrics, elapsedMs);
        drawRecords(canvas, day);
        drawControlButtons(canvas, day);
        if (rendering) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            pressedControl = hitControl(event.getX(), event.getY());
            return pressedControl != CONTROL_NONE || super.onTouchEvent(event);
        }
        if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            pressedControl = CONTROL_NONE;
            return true;
        }
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return super.onTouchEvent(event);
        }
        float x = event.getX();
        float y = event.getY();
        int releasedControl = hitControl(x, y);
        if (releasedControl == CONTROL_NONE || releasedControl != pressedControl) {
            pressedControl = CONTROL_NONE;
            return true;
        }
        pressedControl = CONTROL_NONE;
        long nowElapsedMs = SystemClock.elapsedRealtime();
        long nowWallMs = System.currentTimeMillis();
        if (releasedControl == CONTROL_LEFT) {
            if (timerSession.isRunning()) {
                timerSession.addRecord(nowElapsedMs, nowWallMs);
            } else {
                timerSession.reset(nowElapsedMs);
            }
            performClick();
            invalidate();
            return true;
        }
        if (releasedControl == CONTROL_RIGHT) {
            if (timerSession.isRunning()) {
                timerSession.pause(nowElapsedMs);
            } else {
                timerSession.resume(nowElapsedMs);
            }
            performClick();
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    private int hitControl(float x, float y) {
        if (leftButtonBounds.contains(x, y)) {
            return CONTROL_LEFT;
        }
        if (rightButtonBounds.contains(x, y)) {
            return CONTROL_RIGHT;
        }
        return CONTROL_NONE;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void updateTimeInfo(long nowWallMs) {
        long second = nowWallMs / 1000L;
        if (second != lastSecond || timeInfo == null) {
            lastSecond = second;
            timeInfo = timeFormatter.format(nowWallMs);
        }
    }

    private boolean resolveDayMode() {
        if (weatherInfo != null && weatherInfo.isDay() != null) {
            return weatherInfo.isDay();
        }
        return timeInfo.hour >= 6 && timeInfo.hour < 18;
    }

    private void drawCenterTimer(Canvas canvas, boolean day, long elapsedMs) {
        String elapsedText = timeFormatter.formatElapsed(elapsedMs);
        float textSize = Math.min(getWidth() * 0.155f, getHeight() * 0.26f);
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(ContextCompat.getColor(getContext(),
                day ? R.color.captain_timer_center_text_day : R.color.captain_timer_center_text_night));
        textPaint.setShadowLayer(dp(4), 0, dp(2), ContextCompat.getColor(getContext(),
                day ? R.color.captain_timer_center_shadow_day : R.color.captain_timer_center_shadow_night));
        textPaint.getFontMetrics(timeMetrics);
        timeBaseline = getHeight() * 0.45f - (timeMetrics.ascent + timeMetrics.descent) / 2f;
        canvas.drawText(elapsedText, getWidth() / 2f, timeBaseline, textPaint);
        timeTextHalfWidth = textPaint.measureText(elapsedText) / 2f;
        timeTextRightX = getWidth() / 2f + timeTextHalfWidth;
        textPaint.clearShadowLayer();
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawRecords(Canvas canvas, boolean day) {
        List<TimerLapRecord> records = timerSession.getVisibleRecords();
        secondaryTextPaint.setTypeface(Typeface.MONOSPACE);
        secondaryTextPaint.setTextAlign(Paint.Align.CENTER);
        secondaryTextPaint.setTextSize(dp(14));
        secondaryTextPaint.setColor(ContextCompat.getColor(getContext(),
                day ? R.color.captain_timer_record_text_day : R.color.captain_timer_record_text_night));
        float y = timeBaseline + dp(50);
        for (TimerLapRecord record : records) {
            String line = String.format(Locale.US, "%02d  %s  %s",
                    record.number,
                    timeFormatter.formatClockTime(record.wallTimeMs),
                    timeFormatter.formatCompactElapsed(record.elapsedMs));
            canvas.drawText(line, getWidth() / 2f, y, secondaryTextPaint);
            y += dp(19);
        }
        secondaryTextPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        secondaryTextPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawControlButtons(Canvas canvas, boolean day) {
        float size = Math.max(dp(48), Math.min(dp(62), getHeight() * 0.13f));
        float cy = getHeight() * 0.45f;
        float half = size / 2f;
        float sideGap = dp(58);
        float leftCx = Math.max(dp(66), getWidth() / 2f - timeTextHalfWidth - sideGap);
        float rightCx = Math.min(getWidth() - dp(66), getWidth() / 2f + timeTextHalfWidth + sideGap);
        leftButtonBounds.set(leftCx - half, cy - half, leftCx + half, cy + half);
        rightButtonBounds.set(rightCx - half, cy - half, rightCx + half, cy + half);
        controlPainter.drawGlassButton(canvas, leftButtonBounds, day,
                timerSession.isRunning() ? TimerControlPainter.ICON_STOPWATCH : TimerControlPainter.ICON_REFRESH);
        controlPainter.drawGlassButton(canvas, rightButtonBounds, day,
                timerSession.isRunning() ? TimerControlPainter.ICON_PAUSE : TimerControlPainter.ICON_PLAY);
    }

    private float dp(float value) {
        return value * density;
    }
}
