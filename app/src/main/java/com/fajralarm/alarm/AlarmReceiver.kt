package com.fajralarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.fajralarm.data.FajrDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, 0L)
        val ringing = Intent(context, AlarmRingingService::class.java)
            .putExtra(AlarmScheduler.EXTRA_ALARM_ID, id)
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(ringing) else context.startService(ringing)
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FajrDatabase.open(context)
                val dao = db.alarmDao()
                val alarm = dao.get(id)
                if (alarm != null) {
                    if (alarm.repeatDays.isBlank()) dao.update(alarm.copy(enabled = false))
                    else AlarmScheduler(context).schedule(alarm)
                }
                db.close()
            } finally { pending.finish() }
        }
    }
}
