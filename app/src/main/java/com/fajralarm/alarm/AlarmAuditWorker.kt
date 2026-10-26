package com.fajralarm.alarm

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fajralarm.data.FajrDatabase

/** A periodic safety net that restores the next exact alarm after app/system maintenance. */
class AlarmAuditWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runCatching {
        val db = FajrDatabase.open(applicationContext)
        try {
            val scheduler = AlarmScheduler(applicationContext)
            db.alarmDao().enabled().forEach(scheduler::schedule)
        } finally { db.close() }
        Result.success()
    }.getOrElse { Result.retry() }
}
