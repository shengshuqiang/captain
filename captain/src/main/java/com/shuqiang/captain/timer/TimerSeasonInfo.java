package com.shuqiang.captain.timer;

// 计时器频道的时令信息，season 用于驱动背景色温。
final class TimerSeasonInfo {
    final String name;
    final int season;

    TimerSeasonInfo(String name, int season) {
        this.name = name;
        this.season = season;
    }
}
