package com.fajralarm.alarm

import com.fajralarm.data.AlarmEntity
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class AlarmSchedulerTest {
    @Test fun oneShotAlarmUsesTodayWhenStillAhead() {
        val alarm = AlarmEntity(hour = 7, minute = 15, repeatDays = "")
        assertEquals(LocalDateTime.of(2026, 10, 26, 7, 15), AlarmScheduler.nextOccurrence(alarm, LocalDateTime.of(2026, 10, 26, 6, 0)))
    }

    @Test fun oneShotAlarmAfterTimeMovesToTomorrow() {
        val alarm = AlarmEntity(hour = 5, minute = 30)
        assertEquals(LocalDateTime.of(2026, 10, 27, 5, 30), AlarmScheduler.nextOccurrence(alarm, LocalDateTime.of(2026, 10, 26, 6, 0)))
    }

    @Test fun repeatingAlarmFindsNextSelectedDay() {
        // Monday 06:00; a Tuesday-only alarm is day 2 (Java day values, Sunday is 0).
        val alarm = AlarmEntity(hour = 5, minute = 30, repeatDays = "2")
        assertEquals(LocalDateTime.of(2026, 10, 27, 5, 30), AlarmScheduler.nextOccurrence(alarm, LocalDateTime.of(2026, 10, 26, 6, 0)))
    }
}
