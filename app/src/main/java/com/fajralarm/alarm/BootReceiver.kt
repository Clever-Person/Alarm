package com.fajralarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fajralarm.data.FajrDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FajrDatabase.open(context)
                val dao = db.alarmDao()
                val scheduler = AlarmScheduler(context)
                dao.enabled().forEach(scheduler::schedule)
                db.close()
            } finally { pending.finish() }
        }
    }
}
