package com.shuqiang.captain.timer;

import android.annotation.TargetApi;
import android.icu.util.ChineseCalendar;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// 统一生成计时器频道的时间、公历、农历和时令文案。
public final class TimerTimeFormatter {
    private static final Locale CN = Locale.CHINA;
    private static final String[] LUNAR_MONTHS = {
            "正月", "二月", "三月", "四月", "五月", "六月",
            "七月", "八月", "九月", "十月", "冬月", "腊月"
    };
    private static final String[] LUNAR_DAYS = {
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", CN);
    private final SimpleDateFormat solarFormat = new SimpleDateFormat("yyyy.MM.dd E", CN);

    public TimeInfo format(long timeMillis) {
        Calendar calendar = Calendar.getInstance(CN);
        calendar.setTimeInMillis(timeMillis);
        TimerSeasonInfo seasonInfo = TimerSolarTermResolver.resolve(
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        return new TimeInfo(
                timeFormat.format(calendar.getTime()),
                solarFormat.format(calendar.getTime()),
                formatLunar(timeMillis),
                seasonInfo.name,
                seasonInfo.season,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)
        );
    }

    private String formatLunar(long timeMillis) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return "农历日期";
        }
        return formatLunarWithIcu(timeMillis);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private String formatLunarWithIcu(long timeMillis) {
        ChineseCalendar lunarCalendar = new ChineseCalendar(CN);
        lunarCalendar.setTimeInMillis(timeMillis);
        int month = lunarCalendar.get(ChineseCalendar.MONTH);
        int day = lunarCalendar.get(ChineseCalendar.DAY_OF_MONTH);
        String monthText = month >= 0 && month < LUNAR_MONTHS.length ? LUNAR_MONTHS[month] : "农历";
        String dayText = day >= 1 && day <= LUNAR_DAYS.length ? LUNAR_DAYS[day - 1] : "";
        boolean leapMonth = false;
        try {
            // ICU 的闰月字段在 Android API 24+ 可用，低风险失败时只隐藏“闰”字。
            leapMonth = lunarCalendar.get(22) == 1;
        } catch (RuntimeException ignored) {
            leapMonth = false;
        }
        return (leapMonth ? "闰" : "") + monthText + dayText;
    }

    public static final class TimeInfo {
        public final String timeText;
        public final String solarText;
        public final String lunarText;
        public final String solarTerm;
        public final int season;
        public final int hour;
        public final int minute;
        public final int second;

        private TimeInfo(String timeText, String solarText, String lunarText, String solarTerm,
                         int season, int hour, int minute, int second) {
            this.timeText = timeText;
            this.solarText = solarText;
            this.lunarText = lunarText;
            this.solarTerm = solarTerm;
            this.season = season;
            this.hour = hour;
            this.minute = minute;
            this.second = second;
        }
    }

}
