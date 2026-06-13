package com.shuqiang.captain.timer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

// 横屏计时器频道主画布：时间为前景，水滴石穿和昼夜时令为背景。
public final class TimerChannelView extends View {
    private static final int[] DAY_TOP = {0xFFEAF8F1, 0xFFE9F6FF, 0xFFFFF3DD, 0xFFEAF2FF};
    private static final int[] DAY_BOTTOM = {0xFFFFFBF4, 0xFFFFF8E8, 0xFFFFF2EB, 0xFFF8FBFF};
    private static final int[] NIGHT_TOP = {0xFF102433, 0xFF0E2438, 0xFF231C2D, 0xFF111827};
    private static final int[] NIGHT_BOTTOM = {0xFF182B2B, 0xFF102033, 0xFF2A2027, 0xFF172033};

    private final TimerTimeFormatter timeFormatter = new TimerTimeFormatter();
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint secondaryTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF stoneRect = new RectF();
    private final Path seasonalPath = new Path();

    private TimerTimeFormatter.TimeInfo timeInfo;
    private TimerWeatherInfo weatherInfo;
    private boolean running;
    private long startElapsedMs;
    private long lastSecond = Long.MIN_VALUE;
    private float density;

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
        setKeepScreenOn(true);
        textPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        secondaryTextPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        startElapsedMs = SystemClock.elapsedRealtime();
        postInvalidateOnAnimation();
    }

    public void stop() {
        running = false;
    }

    public void setWeatherInfo(@Nullable TimerWeatherInfo weatherInfo) {
        this.weatherInfo = weatherInfo;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        updateTimeInfo();
        if (timeInfo == null) {
            return;
        }
        boolean day = resolveDayMode();
        float secondProgress = (System.currentTimeMillis() % 1000L) / 1000f;
        drawBackground(canvas, day);
        drawWaterAndStone(canvas, day, secondProgress);
        drawTopLabels(canvas, day);
        drawCenterTime(canvas, day);
        drawWeather(canvas, day);
        if (running) {
            postInvalidateOnAnimation();
        }
    }

    private void updateTimeInfo() {
        long now = System.currentTimeMillis();
        long second = now / 1000L;
        if (second != lastSecond || timeInfo == null) {
            lastSecond = second;
            timeInfo = timeFormatter.format(now);
        }
    }

    private boolean resolveDayMode() {
        if (weatherInfo != null && weatherInfo.isDay() != null) {
            return weatherInfo.isDay();
        }
        return timeInfo.hour >= 6 && timeInfo.hour < 18;
    }

    private void drawBackground(Canvas canvas, boolean day) {
        int season = Math.max(0, Math.min(3, timeInfo.season));
        int top = day ? DAY_TOP[season] : NIGHT_TOP[season];
        int bottom = day ? DAY_BOTTOM[season] : NIGHT_BOTTOM[season];
        backgroundPaint.setShader(new LinearGradient(0, 0, getWidth(), getHeight(),
                top, bottom, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        backgroundPaint.setShader(null);

        float hourRatio = (timeInfo.hour * 60f + timeInfo.minute) / (24f * 60f);
        int accent = day ? 0x35FFFFFF : 0x18FFFFFF;
        shapePaint.setColor(accent);
        seasonalPath.reset();
        float baseY = getHeight() * (0.72f + 0.08f * (float) Math.sin(hourRatio * Math.PI * 2f));
        seasonalPath.moveTo(0, baseY);
        seasonalPath.cubicTo(getWidth() * 0.24f, baseY - dp(36),
                getWidth() * 0.72f, baseY + dp(28), getWidth(), baseY - dp(8));
        seasonalPath.lineTo(getWidth(), getHeight());
        seasonalPath.lineTo(0, getHeight());
        seasonalPath.close();
        canvas.drawPath(seasonalPath, shapePaint);
    }

    private void drawWaterAndStone(Canvas canvas, boolean day, float secondProgress) {
        float w = getWidth();
        float h = getHeight();
        float stoneCenterY = h * 0.77f;
        float stoneWidth = Math.min(w * 0.34f, dp(520));
        float stoneHeight = Math.min(h * 0.12f, dp(110));
        stoneRect.set(w / 2f - stoneWidth / 2f, stoneCenterY - stoneHeight / 2f,
                w / 2f + stoneWidth / 2f, stoneCenterY + stoneHeight / 2f);
        shapePaint.setShader(new RadialGradient(w / 2f, stoneCenterY - stoneHeight * 0.2f,
                stoneWidth * 0.55f,
                day ? 0xFFE9E0D8 : 0xFF58606B,
                day ? 0xFFC6B7AC : 0xFF303742,
                Shader.TileMode.CLAMP));
        canvas.drawOval(stoneRect, shapePaint);
        shapePaint.setShader(null);

        drawStoneMarks(canvas, day, stoneCenterY, stoneWidth, stoneHeight);
        drawFallingDrop(canvas, day, secondProgress, stoneCenterY);
    }

    private void drawStoneMarks(Canvas canvas, boolean day, float centerY, float stoneWidth, float stoneHeight) {
        float traceStrength = 0.28f + timeInfo.second / 60f * 0.46f;
        int traceColor = day ? Color.argb((int) (110 * traceStrength), 103, 87, 76)
                : Color.argb((int) (150 * traceStrength), 185, 198, 210);
        strokePaint.setColor(traceColor);
        strokePaint.setStrokeWidth(dp(1.5f + traceStrength * 2f));
        float cx = getWidth() / 2f;
        for (int i = -2; i <= 2; i++) {
            float offset = i * stoneWidth * 0.035f;
            canvas.drawLine(cx + offset, centerY - stoneHeight * 0.24f,
                    cx + offset * 0.4f, centerY + stoneHeight * 0.16f, strokePaint);
        }

        int holes = Math.min(5, timeInfo.minute / 10);
        shapePaint.setColor(day ? 0x665F5048 : 0x8845505A);
        for (int i = 0; i < holes; i++) {
            float x = cx + (i - 2) * stoneWidth * 0.11f;
            canvas.drawOval(x - dp(8), centerY - dp(4), x + dp(8), centerY + dp(5), shapePaint);
        }
    }

    private void drawFallingDrop(Canvas canvas, boolean day, float progress, float stoneY) {
        float impactStart = 0.84f;
        float fallProgress = Math.min(progress / impactStart, 1f);
        float x = getWidth() / 2f + (float) Math.sin(progress * Math.PI * 2f) * dp(8);
        float startY = getHeight() * 0.18f;
        float endY = stoneY - dp(28);
        float y = startY + (endY - startY) * easeIn(fallProgress);
        shapePaint.setColor(day ? 0xAA4CA7D8 : 0xB88CD8FF);
        canvas.drawCircle(x, y, dp(7), shapePaint);
        shapePaint.setColor(day ? 0x664CA7D8 : 0x668CD8FF);
        canvas.drawOval(x - dp(4), y - dp(14), x + dp(4), y + dp(6), shapePaint);

        if (progress >= impactStart) {
            float ripple = (progress - impactStart) / (1f - impactStart);
            strokePaint.setColor(day ? Color.argb((int) ((1f - ripple) * 130), 74, 167, 216)
                    : Color.argb((int) ((1f - ripple) * 160), 140, 216, 255));
            strokePaint.setStrokeWidth(dp(1.6f));
            float radius = dp(10 + ripple * 36);
            canvas.drawCircle(getWidth() / 2f, stoneY - dp(8), radius, strokePaint);
        }
    }

    private void drawTopLabels(Canvas canvas, boolean day) {
        secondaryTextPaint.setTextSize(dp(16));
        secondaryTextPaint.setColor(day ? 0xCC5E5568 : 0xCCDFE8F2);
        String lunar = timeInfo.lunarText + " · " + timeInfo.solarTerm;
        canvas.drawText(lunar, dp(28), dp(34), secondaryTextPaint);

        secondaryTextPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(timeInfo.solarText, getWidth() - dp(28), dp(34), secondaryTextPaint);
        secondaryTextPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawCenterTime(Canvas canvas, boolean day) {
        float textSize = Math.min(getWidth() * 0.16f, getHeight() * 0.28f);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(day ? 0xEE241C2D : 0xF2F8FBFF);
        textPaint.setShadowLayer(dp(4), 0, dp(2), day ? 0x22FFFFFF : 0x55000000);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline = getHeight() * 0.48f - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText(timeInfo.timeText, getWidth() / 2f, baseline, textPaint);
        textPaint.clearShadowLayer();
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawWeather(Canvas canvas, boolean day) {
        String text = weatherInfo == null ? "本地时间" : weatherInfo.getDisplayText();
        secondaryTextPaint.setTextSize(dp(14));
        secondaryTextPaint.setColor(day ? 0xAA5E5568 : 0xAADFE8F2);
        secondaryTextPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(text, getWidth() - dp(30), getHeight() - dp(28), secondaryTextPaint);
        secondaryTextPaint.setTextAlign(Paint.Align.LEFT);
    }

    private float easeIn(float value) {
        return value * value;
    }

    private float dp(float value) {
        return value * density;
    }
}
