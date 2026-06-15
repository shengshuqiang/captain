package com.shuqiang.captain.timer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

// 水滴石穿绘制：水滴从秒数后方落下，落点在石面上形成柔和波纹。
final class TimerWaterPainter {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float density;

    TimerWaterPainter(float density) {
        this.density = density;
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    void drawWaterAndStone(Canvas canvas, int width, int height, boolean day, float progress,
                           float timeRightX, float timeBaseline, Paint.FontMetrics metrics,
                           long elapsedMs) {
        float stoneCenterY = height * 0.84f;
        float stoneWidth = Math.min(width * 0.34f, dp(520));
        float stoneHeight = Math.min(height * 0.12f, dp(110));
        rect.set(width / 2f - stoneWidth / 2f, stoneCenterY - stoneHeight / 2f,
                width / 2f + stoneWidth / 2f, stoneCenterY + stoneHeight / 2f);
        paint.setShader(new RadialGradient(width / 2f, stoneCenterY - stoneHeight * 0.24f,
                stoneWidth * 0.58f,
                day ? 0xFFE9E0D8 : 0xFF58606B,
                day ? 0xFFC2B2A5 : 0xFF303742,
                Shader.TileMode.CLAMP));
        canvas.drawOval(rect, paint);
        paint.setShader(null);
        drawStoneMarks(canvas, width / 2f, stoneCenterY, stoneWidth, stoneHeight, day, elapsedMs);
        drawFallingDrop(canvas, width, day, progress, timeRightX, timeBaseline, metrics, stoneCenterY);
    }

    private void drawStoneMarks(Canvas canvas, float centerX, float centerY, float stoneWidth,
                                float stoneHeight, boolean day, long elapsedMs) {
        float phase = (elapsedMs % 60000L) / 60000f;
        float traceStrength = 0.32f + phase * 0.42f;
        int traceColor = day ? Color.argb((int) (115 * traceStrength), 103, 87, 76)
                : Color.argb((int) (150 * traceStrength), 185, 198, 210);
        strokePaint.setColor(traceColor);
        strokePaint.setStrokeWidth(dp(1.5f + traceStrength * 2f));
        for (int i = -2; i <= 2; i++) {
            float offset = i * stoneWidth * 0.035f;
            canvas.drawLine(centerX + offset, centerY - stoneHeight * 0.24f,
                    centerX + offset * 0.4f, centerY + stoneHeight * 0.16f, strokePaint);
        }

        int holes = Math.min(5, (int) (elapsedMs / 600000L));
        paint.setColor(day ? 0x665F5048 : 0x8845505A);
        for (int i = 0; i < holes; i++) {
            float x = centerX + (i - 2) * stoneWidth * 0.11f;
            canvas.drawOval(x - dp(8), centerY - dp(4), x + dp(8), centerY + dp(5), paint);
        }
    }

    private void drawFallingDrop(Canvas canvas, int width, boolean day, float progress,
                                 float timeRightX, float timeBaseline, Paint.FontMetrics metrics,
                                 float stoneY) {
        float impactStart = 0.82f;
        float fallProgress = Math.min(progress / impactStart, 1f);
        float startX = Math.min(width - dp(60), Math.max(dp(60), timeRightX + dp(17)));
        float wobble = (float) Math.sin(progress * Math.PI * 2f) * dp(4.2f);
        float x = startX + wobble;
        float startY = timeBaseline + metrics.ascent * 0.45f;
        float endY = stoneY - dp(30);
        float y = startY + (endY - startY) * easeInOut(fallProgress);
        float stretch = 1f + fallProgress * 0.55f;

        paint.setColor(day ? 0x334CA7D8 : 0x338CD8FF);
        canvas.drawOval(x - dp(3), y - dp(28 * stretch), x + dp(3), y - dp(6), paint);
        paint.setShader(new RadialGradient(x - dp(3), y - dp(7), dp(18),
                day ? 0xF08FE5FF : 0xF4D7F4FF,
                day ? 0x884CA7D8 : 0xAA73C8F4,
                Shader.TileMode.CLAMP));
        canvas.drawOval(x - dp(7), y - dp(11 * stretch), x + dp(7), y + dp(9), paint);
        paint.setShader(null);
        paint.setColor(0xCCFFFFFF);
        canvas.drawCircle(x - dp(2.8f), y - dp(5.5f), dp(1.7f), paint);

        if (progress >= impactStart) {
            float ripple = (progress - impactStart) / (1f - impactStart);
            int alpha = (int) ((1f - ripple) * (day ? 130 : 160));
            strokePaint.setColor(day ? Color.argb(alpha, 74, 167, 216)
                    : Color.argb(alpha, 140, 216, 255));
            strokePaint.setStrokeWidth(dp(1.4f));
            float radius = dp(9 + ripple * 38);
            canvas.drawCircle(startX, stoneY - dp(8), radius, strokePaint);
            strokePaint.setStrokeWidth(dp(0.8f));
            canvas.drawCircle(startX, stoneY - dp(8), radius * 0.55f, strokePaint);
            paint.setColor(day ? Color.argb(alpha / 2, 74, 167, 216)
                    : Color.argb(alpha / 2, 140, 216, 255));
            canvas.drawOval(startX - dp(10), stoneY - dp(12), startX + dp(10), stoneY - dp(4), paint);
        }
    }

    private float easeInOut(float value) {
        return value * value * (3f - 2f * value);
    }

    private float dp(float value) {
        return value * density;
    }
}
