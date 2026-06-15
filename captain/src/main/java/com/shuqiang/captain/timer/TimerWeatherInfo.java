package com.shuqiang.captain.timer;

// 计时器频道展示用天气快照，只保留页面需要的最小字段。
public final class TimerWeatherInfo {
    private final String condition;
    private final int temperatureCelsius;
    private final Boolean day;

    public TimerWeatherInfo(String condition, int temperatureCelsius, Boolean day) {
        this.condition = condition;
        this.temperatureCelsius = temperatureCelsius;
        this.day = day;
    }

    public String getCondition() {
        return condition;
    }

    public int getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public Boolean isDay() {
        return day;
    }
}
