package com.shuqiang.captain.timer;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 计时器频道的正计时状态，只处理时间和记录，不掺入绘制生命周期。
final class TimerSession {
    private static final int MAX_VISIBLE_RECORDS = 5;

    private final ArrayList<TimerLapRecord> records = new ArrayList<>();
    private boolean running;
    private long anchorElapsedMs;
    private long baseElapsedMs;
    private int nextRecordNumber;

    TimerSession() {
        this(SystemClock.elapsedRealtime());
    }

    TimerSession(long nowElapsedMs) {
        reset(nowElapsedMs);
    }

    boolean isRunning() {
        return running;
    }

    long elapsedMs(long nowElapsedMs) {
        if (!running) {
            return baseElapsedMs;
        }
        return baseElapsedMs + Math.max(0L, nowElapsedMs - anchorElapsedMs);
    }

    void pause(long nowElapsedMs) {
        if (!running) {
            return;
        }
        baseElapsedMs = elapsedMs(nowElapsedMs);
        running = false;
    }

    void resume(long nowElapsedMs) {
        if (running) {
            return;
        }
        anchorElapsedMs = nowElapsedMs;
        running = true;
    }

    void reset(long nowElapsedMs) {
        running = true;
        anchorElapsedMs = nowElapsedMs;
        baseElapsedMs = 0L;
        nextRecordNumber = 1;
        records.clear();
    }

    TimerLapRecord addRecord(long nowElapsedMs, long nowWallTimeMs) {
        TimerLapRecord record = new TimerLapRecord(nextRecordNumber++,
                nowWallTimeMs,
                elapsedMs(nowElapsedMs));
        records.add(record);
        return record;
    }

    List<TimerLapRecord> getVisibleRecords() {
        int start = Math.max(0, records.size() - MAX_VISIBLE_RECORDS);
        return Collections.unmodifiableList(new ArrayList<>(records.subList(start, records.size())));
    }
}
