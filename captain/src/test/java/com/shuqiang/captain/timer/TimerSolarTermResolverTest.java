package com.shuqiang.captain.timer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TimerSolarTermResolverTest {
    @Test
    public void resolveShouldUsePreviousYearWinterSolsticeBeforeFirstTerm() {
        TimerSeasonInfo info = TimerSolarTermResolver.resolve(1, 1);

        assertEquals("冬至", info.name);
        assertEquals(3, info.season);
    }

    @Test
    public void resolveShouldMatchSummerTermsAroundJune() {
        TimerSeasonInfo grainInEar = TimerSolarTermResolver.resolve(6, 13);
        TimerSeasonInfo summerSolstice = TimerSolarTermResolver.resolve(6, 21);

        assertEquals("芒种", grainInEar.name);
        assertEquals(1, grainInEar.season);
        assertEquals("夏至", summerSolstice.name);
        assertEquals(1, summerSolstice.season);
    }

    @Test
    public void resolveShouldSwitchSeasonAtAutumnStart() {
        TimerSeasonInfo info = TimerSolarTermResolver.resolve(8, 7);

        assertEquals("立秋", info.name);
        assertEquals(2, info.season);
    }
}
