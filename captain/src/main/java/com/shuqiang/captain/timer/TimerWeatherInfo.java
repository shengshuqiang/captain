package com.shuqiang.captain.timer;

// 计时器频道展示用天气快照，只保留页面需要的最小字段。
public final class TimerWeatherInfo {
    private final String condition;
    private final int temperatureCelsius;
    private final Boolean day;
    private final boolean defaultLocation;

    public TimerWeatherInfo(String condition, int temperatureCelsius, Boolean day, boolean defaultLocation) {
        this.condition = condition;
        this.temperatureCelsius = temperatureCelsius;
        this.day = day;
        this.defaultLocation = defaultLocation;
    }

    public String getDisplayText() {
        String suffix = defaultLocation ? " · 默认位置" : "";
        return condition + " " + temperatureCelsius + "°C" + suffix;
    }

    public Boolean isDay() {
        return day;
    }
}
