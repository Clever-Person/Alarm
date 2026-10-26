package com.fajralarm

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fajralarm.alarm.AlarmAuditWorker
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class FajrAlarmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val request = PeriodicWorkRequestBuilder<AlarmAuditWorker>(12, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("alarm_schedule_audit", ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
