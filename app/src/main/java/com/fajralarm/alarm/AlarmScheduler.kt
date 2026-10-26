package com.fajralarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.fajralarm.data.AlarmEntity
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

class AlarmScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    private val manager = context.getSystemService(AlarmManager::class.java)

    fun schedule(alarm: AlarmEntity) {
        if (!alarm.enabled) { cancel(alarm.id); return }
        val next = nextOccurrence(alarm, LocalDateTime.now())
        val intent = Intent(context, AlarmReceiver::class.java).putExtra(EXTRA_ALARM_ID, alarm.id)
        val pending = PendingIntent.getBroadcast(context, alarm.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val show = PendingIntent.getActivity(context, 0, context.packageManager.getLaunchIntentForPackage(context.packageName), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val trigger = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (Build.VERSION.SDK_INT >= 31 && !manager.canScheduleExactAlarms()) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
        } else {
            manager.setAlarmClock(AlarmManager.AlarmClockInfo(trigger, show), pending)
        }
    }

    fun cancel(id: Long) {
        val pending = PendingIntent.getBroadcast(context, id.toInt(), Intent(context, AlarmReceiver::class.java), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) ?: return
        manager.cancel(pending)
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        fun nextOccurrence(alarm: AlarmEntity, now: LocalDateTime): LocalDateTime {
            val days = alarm.repeatDays.split(',').mapNotNull { it.toIntOrNull() }.toSet()
            for (offset in 0..7) {
                val candidateDate = now.toLocalDate().plusDays(offset.toLong())
                val candidate = candidateDate.atTime(alarm.hour, alarm.minute)
                val day = candidate.dayOfWeek.value % 7 // Sunday = 0
                if (candidate.isAfter(now) && (days.isEmpty() && offset == 0 || days.isNotEmpty() && day in days)) return candidate
            }
            return now.plusDays(1).withHour(alarm.hour).withMinute(alarm.minute).withSecond(0).withNano(0)
        }
    }
}
