package com.shuqiang.captain.timer;

// 按公历近似节气日解析时令，保证离线也能给背景提供季节语义。
final class TimerSolarTermResolver {
    private static final SolarTerm[] TERMS = {
            new SolarTerm(1, 5, "小寒", 3), new SolarTerm(1, 20, "大寒", 3),
            new SolarTerm(2, 4, "立春", 0), new SolarTerm(2, 19, "雨水", 0),
            new SolarTerm(3, 5, "惊蛰", 0), new SolarTerm(3, 20, "春分", 0),
            new SolarTerm(4, 5, "清明", 0), new SolarTerm(4, 20, "谷雨", 0),
            new SolarTerm(5, 5, "立夏", 1), new SolarTerm(5, 21, "小满", 1),
            new SolarTerm(6, 5, "芒种", 1), new SolarTerm(6, 21, "夏至", 1),
            new SolarTerm(7, 7, "小暑", 1), new SolarTerm(7, 22, "大暑", 1),
            new SolarTerm(8, 7, "立秋", 2), new SolarTerm(8, 23, "处暑", 2),
            new SolarTerm(9, 7, "白露", 2), new SolarTerm(9, 23, "秋分", 2),
            new SolarTerm(10, 8, "寒露", 2), new SolarTerm(10, 23, "霜降", 2),
            new SolarTerm(11, 7, "立冬", 3), new SolarTerm(11, 22, "小雪", 3),
            new SolarTerm(12, 7, "大雪", 3), new SolarTerm(12, 21, "冬至", 3)
    };

    private TimerSolarTermResolver() {
    }

    static TimerSeasonInfo resolve(int month, int day) {
        SolarTerm current = TERMS[TERMS.length - 1];
        for (SolarTerm term : TERMS) {
            if (month > term.month || (month == term.month && day >= term.day)) {
                current = term;
            } else {
                break;
            }
        }
        return new TimerSeasonInfo(current.name, current.season);
    }

    private static final class SolarTerm {
        private final int month;
        private final int day;
        private final String name;
        private final int season;

        private SolarTerm(int month, int day, String name, int season) {
            this.month = month;
            this.day = day;
            this.name = name;
            this.season = season;
        }
    }
}
