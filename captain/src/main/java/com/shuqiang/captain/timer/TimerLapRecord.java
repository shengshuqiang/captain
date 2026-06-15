package com.shuqiang.captain.timer;

// 秒表记录快照：固定保存点击时刻和当时的正计时用时。
final class TimerLapRecord {
    final int number;
    final long wallTimeMs;
    final long elapsedMs;

    TimerLapRecord(int number, long wallTimeMs, long elapsedMs) {
        this.number = number;
        this.wallTimeMs = wallTimeMs;
        this.elapsedMs = elapsedMs;
    }
}
