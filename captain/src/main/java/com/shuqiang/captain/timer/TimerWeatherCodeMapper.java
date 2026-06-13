package com.shuqiang.captain.timer;

// Open-Meteo weather_code 到计时器频道短中文文案的最小映射。
final class TimerWeatherCodeMapper {
    private TimerWeatherCodeMapper() {
    }

    static String map(int code) {
        if (code == 0) {
            return "晴";
        }
        if (code >= 1 && code <= 3) {
            return "多云";
        }
        if (code == 45 || code == 48) {
            return "雾";
        }
        if ((code >= 51 && code <= 67) || (code >= 80 && code <= 82)) {
            return "雨";
        }
        if (code >= 71 && code <= 77) {
            return "雪";
        }
        if (code >= 95 && code <= 99) {
            return "雷雨";
        }
        return "天气";
    }
}
