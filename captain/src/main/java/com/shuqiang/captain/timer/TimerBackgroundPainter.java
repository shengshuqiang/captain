package com.shuqiang.captain.timer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import captain.R;

// 背景和顶部状态绘制：季节、昼夜、天气氛围、天气小图标和半圆日月。
final class TimerBackgroundPainter {
    private final int[] dayTop;
    private final int[] dayBottom;
    private final int[] nightTop;
    private final int[] nightBottom;

    private final Context context;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();
    private final float density;

    TimerBackgroundPainter(Context context, float density) {
        this.context = context.getApplicationContext();
        this.density = density;
        // 季节渐变端点从资源读取后组装为 int[]，保持原有视觉效果不变。
        dayTop = new int[]{
                color(R.color.captain_timer_bg_top_spring_day),
                color(R.color.captain_timer_bg_top_summer_day),
                color(R.color.captain_timer_bg_top_autumn_day),
                color(R.color.captain_timer_bg_top_winter_day)};
        dayBottom = new int[]{
                color(R.color.captain_timer_bg_bottom_spring_day),
                color(R.color.captain_timer_bg_bottom_summer_day),
                color(R.color.captain_timer_bg_bottom_autumn_day),
                color(R.color.captain_timer_bg_bottom_winter_day)};
        nightTop = new int[]{
                color(R.color.captain_timer_bg_top_spring_night),
                color(R.color.captain_timer_bg_top_summer_night),
                color(R.color.captain_timer_bg_top_autumn_night),
                color(R.color.captain_timer_bg_top_winter_night)};
        nightBottom = new int[]{
                color(R.color.captain_timer_bg_bottom_spring_night),
                color(R.color.captain_timer_bg_bottom_summer_night),
                color(R.color.captain_timer_bg_bottom_autumn_night),
                color(R.color.captain_timer_bg_bottom_winter_night)};
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
    }

    private int color(int resId) {
        return ContextCompat.getColor(context, resId);
    }

    void drawBackground(Canvas canvas, int width, int height, TimerTimeFormatter.TimeInfo timeInfo,
                        boolean day, @Nullable TimerWeatherInfo weatherInfo, long elapsedMs) {
        int season = Math.max(0, Math.min(3, timeInfo.season));
        paint.setShader(new LinearGradient(0, 0, width, height,
                day ? dayTop[season] : nightTop[season],
                day ? dayBottom[season] : nightBottom[season],
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
        strokePaint.setColor(color(day ? R.color.captain_timer_horizon_line_day : R.color.captain_timer_horizon_line_night));
        canvas.drawLine(x - dp(13), horizonY, x + dp(13), horizonY, strokePaint);
        canvas.save();
        canvas.clipRect(x - radius - dp(1), centerY - radius - dp(2), x + radius + dp(1), horizonY);
        if (day) {
            paint.setShader(new RadialGradient(x - dp(3), centerY - dp(4), radius * 1.8f,
                    color(R.color.captain_timer_sun_core_day), color(R.color.captain_timer_sun_edge_day),
                    Shader.TileMode.CLAMP));
            canvas.drawCircle(x, centerY, radius, paint);
            paint.setShader(null);
            paint.setColor(color(R.color.captain_timer_sun_highlight_day));
            canvas.drawCircle(x - dp(3), centerY - dp(4), dp(2.2f), paint);
        } else {
            paint.setShader(new RadialGradient(x - dp(2), centerY - dp(4), radius * 1.7f,
                    color(R.color.captain_timer_moon_core_night), color(R.color.captain_timer_moon_edge_night),
                    Shader.TileMode.CLAMP));
            canvas.drawCircle(x, centerY, radius, paint);
            paint.setShader(null);
            paint.setColor(color(R.color.captain_timer_moon_shadow_night));
            canvas.drawCircle(x + dp(4), centerY - dp(2), radius * 0.86f, paint);
        }
        canvas.restore();
    }

    private void drawTimeGlow(Canvas canvas, int width, int height, TimerTimeFormatter.TimeInfo timeInfo, boolean day) {
        float hourRatio = (timeInfo.hour * 60f + timeInfo.minute) / (24f * 60f);
        float cx = width * (0.18f + 0.64f * hourRatio);
        float cy = height * (day ? 0.28f : 0.2f);
        paint.setShader(new RadialGradient(cx, cy, Math.max(width, height) * 0.38f,
                color(day ? R.color.captain_timer_glow_day : R.color.captain_timer_glow_night),
                color(R.color.captain_timer_glow_transparent), Shader.TileMode.CLAMP));
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
                    color(day ? R.color.captain_timer_sunny_atmosphere_day : R.color.captain_timer_sunny_atmosphere_night),
                    color(R.color.captain_timer_glow_transparent), Shader.TileMode.CLAMP));
            canvas.drawCircle(width * 0.74f, height * 0.22f, width * 0.28f, paint);
            paint.setShader(null);
        }
    }

    private void drawSeasonalWave(Canvas canvas, int width, int height, TimerTimeFormatter.TimeInfo timeInfo, boolean day) {
        float hourRatio = (timeInfo.hour * 60f + timeInfo.minute) / (24f * 60f);
        paint.setColor(color(day ? R.color.captain_timer_wave_day : R.color.captain_timer_wave_night));
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
        strokePaint.setColor(color(day ? R.color.captain_timer_icon_sun_ray_day : R.color.captain_timer_icon_sun_ray_night));
        strokePaint.setStrokeWidth(dp(1.2f));
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4d;
            canvas.drawLine(x + (float) Math.cos(angle) * (radius + dp(3)),
                    y + (float) Math.sin(angle) * (radius + dp(3)),
                    x + (float) Math.cos(angle) * (radius + dp(6)),
                    y + (float) Math.sin(angle) * (radius + dp(6)), strokePaint);
        }
        paint.setColor(color(day ? R.color.captain_timer_icon_sun_body_day : R.color.captain_timer_icon_sun_body_night));
        canvas.drawCircle(x, y, radius, paint);
    }

    private void drawCloudIcon(Canvas canvas, float x, float y, boolean day) {
        paint.setColor(color(day ? R.color.captain_timer_icon_cloud_body_day : R.color.captain_timer_icon_cloud_body_night));
        canvas.drawCircle(x - dp(6), y + dp(2), dp(5), paint);
        canvas.drawCircle(x, y - dp(1), dp(7), paint);
        canvas.drawCircle(x + dp(7), y + dp(2), dp(5.5f), paint);
        canvas.drawRoundRect(x - dp(12), y + dp(2), x + dp(13), y + dp(8), dp(4), dp(4), paint);
        strokePaint.setColor(color(day ? R.color.captain_timer_icon_cloud_line_day : R.color.captain_timer_icon_cloud_line_night));
        strokePaint.setStrokeWidth(dp(1));
        canvas.drawLine(x - dp(11), y + dp(8), x + dp(12), y + dp(8), strokePaint);
    }

    private void drawFogIcon(Canvas canvas, float x, float y, boolean day) {
        strokePaint.setColor(color(day ? R.color.captain_timer_icon_fog_day : R.color.captain_timer_icon_fog_night));
        strokePaint.setStrokeWidth(dp(1.4f));
        for (int i = 0; i < 3; i++) {
            float yy = y - dp(5) + i * dp(5);
            canvas.drawLine(x - dp(12), yy, x + dp(12), yy, strokePaint);
        }
    }

    private void drawRainLines(Canvas canvas, float x, float y, boolean day) {
        strokePaint.setColor(color(day ? R.color.captain_timer_icon_rain_day : R.color.captain_timer_icon_rain_night));
        strokePaint.setStrokeWidth(dp(1.2f));
        for (int i = -1; i <= 1; i++) {
            float xx = x + i * dp(6);
            canvas.drawLine(xx + dp(1), y - dp(2), xx - dp(2), y + dp(5), strokePaint);
        }
    }

    private void drawSnowDots(Canvas canvas, float x, float y, boolean day) {
        paint.setColor(color(day ? R.color.captain_timer_icon_snow_day : R.color.captain_timer_icon_snow_night));
        for (int i = -1; i <= 1; i++) {
            canvas.drawCircle(x + i * dp(6), y + Math.abs(i) * dp(2), dp(1.7f), paint);
        }
    }

    private void drawLightning(Canvas canvas, float x, float y, boolean day) {
        paint.setColor(color(day ? R.color.captain_timer_icon_lightning_day : R.color.captain_timer_icon_lightning_night));
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
        paint.setColor(color(day
                ? (fog ? R.color.captain_timer_cloud_layer_fog_day : R.color.captain_timer_cloud_layer_plain_day)
                : (fog ? R.color.captain_timer_cloud_layer_fog_night : R.color.captain_timer_cloud_layer_plain_night)));
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
            paint.setColor(color(day ? R.color.captain_timer_thunder_mask_day : R.color.captain_timer_thunder_mask_night));
            canvas.drawRect(0, 0, width, height, paint);
        }
        strokePaint.setColor(color(day ? R.color.captain_timer_rain_streak_day : R.color.captain_timer_rain_streak_night));
        strokePaint.setStrokeWidth(dp(1));
        float phase = (elapsedMs % 1800L) / 1800f;
        for (int i = 0; i < 22; i++) {
            float x = (i * 73 + phase * width * 0.28f) % width;
            float y = (i * 47 + phase * height * 0.46f) % height;
            canvas.drawLine(x, y, x - dp(5), y + dp(13), strokePaint);
        }
    }

    private void drawSnowAtmosphere(Canvas canvas, int width, int height, boolean day, long elapsedMs) {
        paint.setColor(color(day ? R.color.captain_timer_snow_flake_day : R.color.captain_timer_snow_flake_night));
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
