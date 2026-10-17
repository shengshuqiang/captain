package com.shuqiang.captain.timer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import androidx.core.content.ContextCompat;

import captain.R;

// 计时器左右控制按钮绘制，保持自绘图标避免依赖 emoji 字体差异。
final class TimerControlPainter {
    static final int ICON_STOPWATCH = 1;
    static final int ICON_PAUSE = 2;
    static final int ICON_PLAY = 3;
    static final int ICON_REFRESH = 4;

    private final Context context;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF arcRect = new RectF();
    private final float density;

    TimerControlPainter(Context context, float density) {
        this.context = context.getApplicationContext();
        this.density = density;
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
    }

    private int color(int resId) {
        return ContextCompat.getColor(context, resId);
    }

    void drawGlassButton(Canvas canvas, RectF bounds, boolean day, int icon) {
        // 只统一外围控件；沿用原触控边界、昼夜模式及自绘图标。
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color(day ? R.color.captain_brand_100
                : R.color.captain_timer_button_glass_core_night));
        float corner = context.getResources().getDimension(R.dimen.captain_radius_control);
        canvas.drawRoundRect(bounds, corner, corner, paint);
        drawIcon(canvas, bounds, day, icon);
    }

    private void drawIcon(Canvas canvas, RectF bounds, boolean day, int icon) {
        int iconColor = color(day ? R.color.captain_timer_button_icon_day : R.color.captain_timer_button_icon_night);
        strokePaint.setColor(iconColor);
        strokePaint.setStrokeWidth(dp(2.6f));
        paint.setColor(iconColor);
        float cx = bounds.centerX();
        float cy = bounds.centerY();
        if (icon == ICON_STOPWATCH) {
            canvas.drawCircle(cx, cy + dp(2), dp(10), strokePaint);
            canvas.drawLine(cx, cy + dp(2), cx + dp(4), cy - dp(4), strokePaint);
            canvas.drawLine(cx, cy - dp(11), cx, cy - dp(16), strokePaint);
            canvas.drawLine(cx - dp(4), cy - dp(16), cx + dp(4), cy - dp(16), strokePaint);
        } else if (icon == ICON_PAUSE) {
            canvas.drawRoundRect(cx - dp(8), cy - dp(11), cx - dp(3), cy + dp(11), dp(2), dp(2), paint);
            canvas.drawRoundRect(cx + dp(3), cy - dp(11), cx + dp(8), cy + dp(11), dp(2), dp(2), paint);
        } else if (icon == ICON_PLAY) {
            path.reset();
            path.moveTo(cx - dp(6), cy - dp(11));
            path.lineTo(cx - dp(6), cy + dp(11));
            path.lineTo(cx + dp(12), cy);
            path.close();
            canvas.drawPath(path, paint);
        } else if (icon == ICON_REFRESH) {
            arcRect.set(cx - dp(12), cy - dp(12), cx + dp(12), cy + dp(12));
            canvas.drawArc(arcRect, -38, 285, false, strokePaint);
            path.reset();
            path.moveTo(cx + dp(11), cy - dp(13));
            path.lineTo(cx + dp(15), cy - dp(5));
            path.lineTo(cx + dp(6), cy - dp(6));
            canvas.drawPath(path, paint);
        }
    }

    private float dp(float value) {
        return value * density;
    }
}
