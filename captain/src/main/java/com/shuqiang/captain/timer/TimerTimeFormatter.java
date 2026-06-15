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
    private static final String[] STEMS = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
    private static final String[] BRANCHES = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
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
                formatGanzhiYear(timeMillis, calendar),
                formatLunar(timeMillis),
                seasonInfo.name,
                seasonInfo.season,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)
        );
    }

    public String formatClockTime(long timeMillis) {
        Calendar calendar = Calendar.getInstance(CN);
        calendar.setTimeInMillis(timeMillis);
        return timeFormat.format(calendar.getTime());
    }

    public String formatElapsed(long elapsedMs) {
        long totalSeconds = Math.max(0L, elapsedMs / 1000L);
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds / 60L) % 60L;
        long seconds = totalSeconds % 60L;
        return String.format(CN, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    public String formatCompactElapsed(long elapsedMs) {
        long totalSeconds = Math.max(0L, elapsedMs / 1000L);
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds / 60L) % 60L;
        long seconds = totalSeconds % 60L;
        if (hours > 0L) {
            return String.format(CN, "%d:%02d'%02d\"", hours, minutes, seconds);
        }
        long totalMinutes = totalSeconds / 60L;
        return String.format(CN, "%d'%02d\"", totalMinutes, seconds);
    }

    private String formatGanzhiYear(long timeMillis, Calendar solarCalendar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                return formatGanzhiYearWithIcu(timeMillis);
            } catch (RuntimeException ignored) {
                // ICU 个别机型异常时回退到公历年近似值，保证顶部信息不中断。
            }
        }
        return formatGanzhiYearByGregorian(solarCalendar.get(Calendar.YEAR));
    }

    @TargetApi(Build.VERSION_CODES.N)
    private String formatGanzhiYearWithIcu(long timeMillis) {
        ChineseCalendar lunarCalendar = new ChineseCalendar(CN);
        lunarCalendar.setTimeInMillis(timeMillis);
        int cycleYear = lunarCalendar.get(ChineseCalendar.YEAR);
        if (cycleYear < 1) {
            throw new IllegalStateException("invalid lunar cycle year");
        }
        int index = (cycleYear - 1) % 60;
        return STEMS[index % 10] + BRANCHES[index % 12] + "年";
    }

    private String formatGanzhiYearByGregorian(int year) {
        int index = positiveModulo(year - 4, 60);
        return STEMS[index % 10] + BRANCHES[index % 12] + "年";
    }

    private int positiveModulo(int value, int divisor) {
        int result = value % divisor;
        return result < 0 ? result + divisor : result;
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
        public final String ganzhiYearText;
        public final String lunarText;
        public final String solarTerm;
        public final int season;
        public final int hour;
        public final int minute;
        public final int second;

        private TimeInfo(String timeText, String solarText, String ganzhiYearText, String lunarText,
                         String solarTerm, int season, int hour, int minute, int second) {
            this.timeText = timeText;
            this.solarText = solarText;
            this.ganzhiYearText = ganzhiYearText;
            this.lunarText = lunarText;
            this.solarTerm = solarTerm;
            this.season = season;
            this.hour = hour;
            this.minute = minute;
            this.second = second;
        }
    }

}
