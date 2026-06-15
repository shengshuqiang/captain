package com.shuqiang.captain.timer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TimerTimeFormatterTest {
    @Test
    public void formatElapsedShouldUseHourMinuteSecondClockShape() {
        TimerTimeFormatter formatter = new TimerTimeFormatter();

        assertEquals("00:00:00", formatter.formatElapsed(999L));
        assertEquals("01:02:03", formatter.formatElapsed(3_723_000L));
    }

    @Test
    public void formatCompactElapsedShouldUseMinuteSecondAbbreviation() {
        TimerTimeFormatter formatter = new TimerTimeFormatter();

        assertEquals("10'11\"", formatter.formatCompactElapsed(611_000L));
        assertEquals("1:02'03\"", formatter.formatCompactElapsed(3_723_000L));
    }
}
