package com.shuqiang.captain.timer;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import androidx.annotation.Nullable;

// 背景和顶部状态绘制：季节、昼夜、天气氛围、天气小图标和半圆日月。
final class TimerBackgroundPainter {
    private static final int[] DAY_TOP = {0xFFEAF8F1, 0xFFE9F6FF, 0xFFFFF3DD, 0xFFEAF2FF};
    private static final int[] DAY_BOTTOM = {0xFFFFFBF4, 0xFFFFF8E8, 0xFFFFF2EB, 0xFFF8FBFF};
    private static final int[] NIGHT_TOP = {0xFF102433, 0xFF0E2438, 0xFF231C2D, 0xFF111827};
    private static final int[] NIGHT_BOTTOM = {0xFF182B2B, 0xFF102033, 0xFF2A2027, 0xFF172033};

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();
    private final float density;

    TimerBackgroundPainter(float density) {
        this.density = density;
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
    }

    void drawBackground(Canvas canvas, int width, int height, TimerTimeFormatter.TimeInfo timeInfo,
                        boolean day, @Nullable TimerWeatherInfo weatherInfo, long elapsedMs) {
        int season = Math.max(0, Math.min(3, timeInfo.season));
        paint.setShader(new LinearGradient(0, 0, width, height,
                day ? DAY_TOP[season] : NIGHT_TOP[season],
                day ? DAY_BOTTOM[season] : NIGHT_BOTTOM[season],
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(null);
        drawTimeGlow(canvas, width, height, timeInfo, day);
        drawWeatherAtmosphere(canvas, width, height, day, weatherInfo, elapsedMs);
        drawSeasonalWave(canvas, width, height, timeInfo, day);
    }

    void drawWeatherIcon(Canvas canvas, String condition, float x, float centerY, boolean day) {
        if ("晴".equals(condition)) {
            drawSunIcon(canvas, x, centerY, dp(6), day);
        } else if ("雾".equals(condition)) {
            drawFogIcon(canvas, x, centerY, day);
        } else if ("雨".equals(condition)) {
            drawCloudIcon(canvas, x, centerY - dp(1), day);
            drawRainLines(canvas, x, centerY + dp(8), day);
        } else if ("雪".equals(condition)) {
            drawCloudIcon(canvas, x, centerY - dp(1), day);
            drawSnowDots(canvas, x, centerY + dp(8), day);
        } else if ("雷雨".equals(condition)) {
            drawCloudIcon(canvas, x, centerY - dp(1), day);
            drawLightning(canvas, x, centerY + dp(8), day);
        } else {
            drawCloudIcon(canvas, x, centerY, day);
        }
    }

    void drawHorizonBody(Canvas canvas, float x, float centerY, boolean day) {
        float radius = dp(8.5f);
        float horizonY = centerY + dp(3);
        strokePaint.setStrokeWidth(dp(1.2f));
        strokePaint.setColor(day ? 0x997A5D35 : 0x99D8E3FF);
        canvas.drawLine(x - dp(13), horizonY, x + dp(13), horizonY, strokePaint);
        canvas.save();
        canvas.clipRect(x - radius - dp(1), centerY - radius - dp(2), x + radius + dp(1), horizonY);
        if (day) {
            paint.setShader(new RadialGradient(x - dp(3), centerY - dp(4), radius * 1.8f,
                    0xFFFFF4A8, 0xFFFF8A25, Shader.TileMode.CLAMP));
            canvas.drawCircle(x, centerY, radius, paint);
            paint.setShader(null);
            paint.setColor(0x66FFFFFF);
            canvas.drawCircle(x - dp(3), centerY - dp(4), dp(2.2f), paint);
        } else {
            paint.setShader(new RadialGradient(x - dp(2), centerY - dp(4), radius * 1.7f,
                    0xFFF8FBFF, 0xFF94A4D8, Shader.TileMode.CLAMP));
            canvas.drawCircle(x, centerY, radius, paint);
            paint.setShader(null);
            paint.setColor(0xCC29305A);
            canvas.drawCircle(x + dp(4), centerY - dp(2), radius * 0.86f, paint);
        }
        canvas.restore();
    }

    private void drawTimeGlow(Canvas canvas, int width, int height, TimerTimeFormatter.TimeInfo timeInfo, boolean day) {
        float hourRatio = (timeInfo.hour * 60f + timeInfo.minute) / (24f * 60f);
        float cx = width * (0.18f + 0.64f * hourRatio);
        float cy = height * (day ? 0.28f : 0.2f);
        paint.setShader(new RadialGradient(cx, cy, Math.max(width, height) * 0.38f,
                day ? 0x26FFD36D : 0x223B6FCF, 0x00000000, Shader.TileMode.CLAMP));
        canvas.drawCircle(cx, cy, Math.max(width, height) * 0.38f, paint);
        paint.setShader(null);
    }

    private void drawWeatherAtmosphere(Canvas canvas, int width, int height, boolean day,
                                       @Nullable TimerWeatherInfo weatherInfo, long elapsedMs) {
        String condition = weatherInfo == null ? "" : weatherInfo.getCondition();
        if ("多云".equals(condition) || "雾".equals(condition)) {
            drawCloudLayer(canvas, width, height, day, "雾".equals(condition));
        } else if ("雨".equals(condition) || "雷雨".equals(condition)) {
            drawRainAtmosphere(canvas, width, height, day, elapsedMs, "雷雨".equals(condition));
        } else if ("雪".equals(condition)) {
            drawSnowAtmosphere(canvas, width, height, day, elapsedMs);
        } else if ("晴".equals(condition)) {
            paint.setShader(new RadialGradient(width * 0.74f, height * 0.22f, width * 0.28f,
                    day ? 0x2BFFE29D : 0x1A9FCBFF, 0x00000000, Shader.TileMode.CLAMP));
            canvas.drawCircle(width * 0.74f, height * 0.22f, width * 0.28f, paint);
            paint.setShader(null);
        }
    }

    private void drawSeasonalWave(Canvas canvas, int width, int height, TimerTimeFormatter.TimeInfo timeInfo, boolean day) {
        float hourRatio = (timeInfo.hour * 60f + timeInfo.minute) / (24f * 60f);
        paint.setColor(day ? 0x35FFFFFF : 0x18FFFFFF);
        path.reset();
        float baseY = height * (0.72f + 0.08f * (float) Math.sin(hourRatio * Math.PI * 2f));
        path.moveTo(0, baseY);
        path.cubicTo(width * 0.24f, baseY - dp(36),
                width * 0.72f, baseY + dp(28), width, baseY - dp(8));
        path.lineTo(width, height);
        path.lineTo(0, height);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawSunIcon(Canvas canvas, float x, float y, float radius, boolean day) {
        strokePaint.setColor(day ? 0xCCF59B22 : 0xCCD5ECFF);
        strokePaint.setStrokeWidth(dp(1.2f));
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4d;
            canvas.drawLine(x + (float) Math.cos(angle) * (radius + dp(3)),
                    y + (float) Math.sin(angle) * (radius + dp(3)),
                    x + (float) Math.cos(angle) * (radius + dp(6)),
                    y + (float) Math.sin(angle) * (radius + dp(6)), strokePaint);
        }
        paint.setColor(day ? 0xFFFFB442 : 0xFFEAF6FF);
        canvas.drawCircle(x, y, radius, paint);
    }

    private void drawCloudIcon(Canvas canvas, float x, float y, boolean day) {
        paint.setColor(day ? 0xD8FFFFFF : 0xCCDCEBFF);
        canvas.drawCircle(x - dp(6), y + dp(2), dp(5), paint);
        canvas.drawCircle(x, y - dp(1), dp(7), paint);
        canvas.drawCircle(x + dp(7), y + dp(2), dp(5.5f), paint);
        canvas.drawRoundRect(x - dp(12), y + dp(2), x + dp(13), y + dp(8), dp(4), dp(4), paint);
        strokePaint.setColor(day ? 0x556C7D88 : 0x668EA8C5);
        strokePaint.setStrokeWidth(dp(1));
        canvas.drawLine(x - dp(11), y + dp(8), x + dp(12), y + dp(8), strokePaint);
    }

    private void drawFogIcon(Canvas canvas, float x, float y, boolean day) {
        strokePaint.setColor(day ? 0xAA8596A2 : 0xAACEDEF6);
        strokePaint.setStrokeWidth(dp(1.4f));
        for (int i = 0; i < 3; i++) {
            float yy = y - dp(5) + i * dp(5);
            canvas.drawLine(x - dp(12), yy, x + dp(12), yy, strokePaint);
        }
    }

    private void drawRainLines(Canvas canvas, float x, float y, boolean day) {
        strokePaint.setColor(day ? 0xAA4CA7D8 : 0xCC8CD8FF);
        strokePaint.setStrokeWidth(dp(1.2f));
        for (int i = -1; i <= 1; i++) {
            float xx = x + i * dp(6);
            canvas.drawLine(xx + dp(1), y - dp(2), xx - dp(2), y + dp(5), strokePaint);
        }
    }

    private void drawSnowDots(Canvas canvas, float x, float y, boolean day) {
        paint.setColor(day ? 0xCCDFF7FF : 0xEEF8FBFF);
        for (int i = -1; i <= 1; i++) {
            canvas.drawCircle(x + i * dp(6), y + Math.abs(i) * dp(2), dp(1.7f), paint);
        }
    }

    private void drawLightning(Canvas canvas, float x, float y, boolean day) {
        paint.setColor(day ? 0xFFE2A330 : 0xFFFFE589);
        path.reset();
        path.moveTo(x - dp(2), y - dp(6));
        path.lineTo(x + dp(4), y - dp(6));
        path.lineTo(x, y + dp(1));
        path.lineTo(x + dp(5), y + dp(1));
        path.lineTo(x - dp(3), y + dp(10));
        path.lineTo(x, y + dp(3));
        path.lineTo(x - dp(5), y + dp(3));
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawCloudLayer(Canvas canvas, int width, int height, boolean day, boolean fog) {
        paint.setColor(day ? (fog ? 0x42FFFFFF : 0x2EFFFFFF) : (fog ? 0x2A9FB5D0 : 0x1E9FB5D0));
        for (int i = 0; i < 3; i++) {
            float y = height * (0.24f + i * 0.16f);
            rect.set(width * (0.06f + i * 0.12f), y,
                    width * (0.62f + i * 0.08f), y + dp(fog ? 34 : 22));
            canvas.drawOval(rect, paint);
        }
    }

    private void drawRainAtmosphere(Canvas canvas, int width, int height, boolean day,
                                    long elapsedMs, boolean thunder) {
        if (thunder) {
            paint.setColor(day ? 0x14EAD7A0 : 0x183F69FF);
            canvas.drawRect(0, 0, width, height, paint);
        }
        strokePaint.setColor(day ? 0x334CA7D8 : 0x448CD8FF);
        strokePaint.setStrokeWidth(dp(1));
        float phase = (elapsedMs % 1800L) / 1800f;
        for (int i = 0; i < 22; i++) {
            float x = (i * 73 + phase * width * 0.28f) % width;
            float y = (i * 47 + phase * height * 0.46f) % height;
            canvas.drawLine(x, y, x - dp(5), y + dp(13), strokePaint);
        }
    }

    private void drawSnowAtmosphere(Canvas canvas, int width, int height, boolean day, long elapsedMs) {
        paint.setColor(day ? 0x55FFFFFF : 0x66EAF6FF);
        float phase = (elapsedMs % 3200L) / 3200f;
        for (int i = 0; i < 24; i++) {
            float x = (i * 61 + phase * width * 0.18f) % width;
            float y = (i * 43 + phase * height * 0.38f) % height;
            canvas.drawCircle(x, y, dp(1.1f + (i % 3) * 0.35f), paint);
        }
    }

    private float dp(float value) {
        return value * density;
    }
}
