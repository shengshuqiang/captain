package com.shuqiang.captain.timer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TimerWeatherCodeMapperTest {
    @Test
    public void mapShouldReturnShortChineseLabelsForKnownOpenMeteoCodes() {
        assertEquals("晴", TimerWeatherCodeMapper.map(0));
        assertEquals("多云", TimerWeatherCodeMapper.map(2));
        assertEquals("雾", TimerWeatherCodeMapper.map(45));
        assertEquals("雨", TimerWeatherCodeMapper.map(61));
        assertEquals("雨", TimerWeatherCodeMapper.map(81));
        assertEquals("雪", TimerWeatherCodeMapper.map(73));
        assertEquals("雷雨", TimerWeatherCodeMapper.map(95));
    }

    @Test
    public void mapShouldFallbackForUnknownCode() {
        assertEquals("天气", TimerWeatherCodeMapper.map(-1));
        assertEquals("天气", TimerWeatherCodeMapper.map(999));
    }
}
