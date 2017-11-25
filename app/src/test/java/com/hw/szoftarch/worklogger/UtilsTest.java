package com.hw.szoftarch.worklogger;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilsTest {
    @Test
    public void testShowedElapsedTime() throws Exception {
        String result = Utils.getShowedElapsedTime(3600L);
        assertEquals("01:00:00", result);

        result = Utils.getShowedElapsedTime(3540L);
        assertEquals("00:59:00", result);

        result = Utils.getShowedElapsedTime(0L);
        assertEquals("00:00:00", result);

        result = Utils.getShowedElapsedTime(59L);
        assertEquals("00:00:59", result);

        result = Utils.getShowedElapsedTime(60L);
        assertEquals("00:01:00", result);

        result = Utils.getShowedElapsedTime(61L);
        assertEquals("00:01:01", result);

        result = Utils.getShowedElapsedTime(86400L);
        assertEquals("24:00:00", result);
    }

    @Test
    public void testGetSeconds() {
        long result = Utils.getSeconds(1, 0, 0);
        assertEquals(3600, result);

        result = Utils.getSeconds(0, 60, 0);
        assertEquals(3600, result);

        result = Utils.getSeconds(0, 0, 3600);
        assertEquals(3600, result);

        result = Utils.getSeconds(0, 0, 0);
        assertEquals(0, result);

        result = Utils.getSeconds(1, 20, 14);
        assertEquals(4814, result);
    }

    @Test
    public void testRemainders() {
        long result = Utils.getHoursRemainder(3600L);
        assertEquals(1, result);

        result = Utils.getHoursRemainder(7199L);
        assertEquals(1, result);

        result = Utils.getHoursRemainder(7200L);
        assertEquals(2, result);

        result = Utils.getMinutesRemainder(3600L);
        assertEquals(0, result);

        result = Utils.getMinutesRemainder(7199L);
        assertEquals(59, result);

        result = Utils.getMinutesRemainder(7200L);
        assertEquals(0, result);

        result = Utils.getMinutesRemainder(60L);
        assertEquals(1, result);

        result = Utils.getMinutesRemainder(59L);
        assertEquals(0, result);

        result = Utils.getMinutesRemainder(3725L);
        assertEquals(2, result);
    }
}