package com.shuqiang.captain.timer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class TimerSessionTest {
    @Test
    public void pauseResumeShouldKeepElapsedStableWhilePaused() {
        TimerSession session = new TimerSession(1_000L);

        assertEquals(1_500L, session.elapsedMs(2_500L));
        session.pause(3_000L);
        assertEquals(2_000L, session.elapsedMs(9_000L));
        session.resume(10_000L);

        assertEquals(3_500L, session.elapsedMs(11_500L));
    }

    @Test
    public void resetShouldClearRecordsAndRestartRunningTimer() {
        TimerSession session = new TimerSession(1_000L);
        session.addRecord(2_000L, 100_000L);
        session.addRecord(3_000L, 101_000L);

        session.reset(7_000L);

        assertTrue(session.isRunning());
        assertEquals(0L, session.elapsedMs(7_000L));
        assertEquals(0, session.getVisibleRecords().size());
    }

    @Test
    public void visibleRecordsShouldKeepLatestFiveRecords() {
        TimerSession session = new TimerSession(1_000L);
        for (int i = 0; i < 6; i++) {
            session.addRecord(2_000L + i * 100L, 100_000L + i * 1_000L);
        }

        List<TimerLapRecord> records = session.getVisibleRecords();

        assertEquals(5, records.size());
        assertEquals(2, records.get(0).number);
        assertEquals(6, records.get(4).number);
    }
}
