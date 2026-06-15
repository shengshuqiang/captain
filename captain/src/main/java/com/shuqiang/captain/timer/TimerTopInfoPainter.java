package com.shuqiang.captain.timer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import androidx.annotation.Nullable;

// 顶部状态栏绘制：左侧时令和天气按可用宽度降级，右侧公历日期保持稳定。
final class TimerTopInfoPainter {
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;

    TimerTopInfoPainter(float density) {
        this.density = density;
        textPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
    }

    void drawTopLabels(Canvas canvas, int width, TimerTimeFormatter.TimeInfo timeInfo,
                       @Nullable TimerWeatherInfo weatherInfo, boolean day,
                       TimerBackgroundPainter backgroundPainter) {
        textPaint.setTextSize(dp(15));
        textPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(day ? 0xCC5E5568 : 0xCCDFE8F2);

        float leftPadding = dp(28);
        float rightPadding = dp(28);
        float baseline = dp(34);
        String solarText = timeInfo.solarText;
        float solarWidth = textPaint.measureText(solarText);
        float rightGuardX = width - rightPadding - solarWidth - dp(18);
        float x = leftPadding;
        String lunar = timeInfo.ganzhiYearText + " · " + timeInfo.lunarText + " · " + timeInfo.solarTerm;
        float maxLunarWidth = Math.max(0f, rightGuardX - leftPadding);
        boolean lunarFits = textPaint.measureText(lunar) <= maxLunarWidth;
        String visibleLunar = fitText(lunar, maxLunarWidth);
        canvas.drawText(visibleLunar, x, baseline, textPaint);
        x += textPaint.measureText(visibleLunar) + dp(13);

        if (lunarFits && weatherInfo != null && canFitWeather(x, rightGuardX, weatherInfo)) {
            x = drawWeather(canvas, x, baseline, rightGuardX, weatherInfo, day, backgroundPainter);
        }

        if (lunarFits && x + dp(28) <= rightGuardX) {
            backgroundPainter.drawHorizonBody(canvas, x + dp(10), baseline - dp(8), day);
        }

        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setTextSize(dp(14));
        textPaint.setColor(day ? 0xAA5E5568 : 0xAADFE8F2);
        canvas.drawText(solarText, width - rightPadding, baseline, textPaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    private float drawWeather(Canvas canvas, float x, float baseline, float rightGuardX,
                              TimerWeatherInfo weatherInfo, boolean day,
                              TimerBackgroundPainter backgroundPainter) {
        float iconCenterY = baseline - dp(6);
        backgroundPainter.drawWeatherIcon(canvas, weatherInfo.getCondition(), x + dp(8), iconCenterY, day);
        float nextX = x + dp(23);
        String temperature = weatherInfo.getTemperatureCelsius() + "°C";
        if (nextX + textPaint.measureText(temperature) + dp(15) <= rightGuardX) {
            canvas.drawText(temperature, nextX, baseline, textPaint);
            return nextX + textPaint.measureText(temperature) + dp(15);
        }
        return nextX + dp(9);
    }

    private boolean canFitWeather(float x, float rightGuardX, TimerWeatherInfo weatherInfo) {
        if (x + dp(23) > rightGuardX) {
            return false;
        }
        String temperature = weatherInfo.getTemperatureCelsius() + "°C";
        return x + dp(23) + textPaint.measureText(temperature) + dp(8) <= rightGuardX
                || x + dp(32) <= rightGuardX;
    }

    private String fitText(String text, float maxWidth) {
        if (maxWidth <= 0f) {
            return "";
        }
        if (textPaint.measureText(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "…";
        float ellipsisWidth = textPaint.measureText(ellipsis);
        if (ellipsisWidth > maxWidth) {
            return "";
        }
        int end = text.length();
        while (end > 0 && textPaint.measureText(text, 0, end) + ellipsisWidth > maxWidth) {
            end--;
        }
        return text.substring(0, end) + ellipsis;
    }

    private float dp(float value) {
        return value * density;
    }
}
