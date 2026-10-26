package com.fajralarm.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.fajralarm.R
import com.fajralarm.MainActivity
import kotlinx.coroutines.*

class AlarmRingingService : Service() {
    private var player: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var rampJob: Job? = null
    private var alarmId: Long = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopAlarm(); return START_NOT_STICKY }
        alarmId = intent?.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, 0L) ?: 0L
        startForeground(NOTIFICATION_ID, makeNotification())
        acquireWakeLock()
        CoroutineScope(Dispatchers.IO).launch {
            val db = com.fajralarm.data.FajrDatabase.open(this@AlarmRingingService)
            val alarm = db.alarmDao().get(alarmId)
            db.close()
            withContext(Dispatchers.Main) { startSound(alarm?.volume ?: 75, alarm?.vibrate ?: true, alarm?.sound ?: "alarm") }
        }
        return START_REDELIVER_INTENT
    }

    private fun makeNotification(): Notification {
        val channelId = "fajr_ringing"
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel(channelId, "Alarm", NotificationManager.IMPORTANCE_HIGH).apply { lockscreenVisibility = Notification.VISIBILITY_PUBLIC })
        val fullScreen = PendingIntent.getActivity(this, 1, Intent(this, MainActivity::class.java).putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Fajr Alarm")
            .setContentText("Complete your mission to turn off the alarm")
            .setCategory(NotificationCompat.CATEGORY_ALARM).setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true).setAutoCancel(false).setFullScreenIntent(fullScreen, true).build()
    }

    @Suppress("DEPRECATION") private fun acquireWakeLock() {
        wakeLock = getSystemService(PowerManager::class.java).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FajrAlarm:Ringing").apply { acquire(12 * 60 * 1000L) }
    }

    private fun startSound(volumePercent: Int, vibrate: Boolean, sound: String) {
        runCatching {
            val type = if (sound == "notification") RingtoneManager.TYPE_NOTIFICATION else RingtoneManager.TYPE_ALARM
            val uri = RingtoneManager.getDefaultUri(type) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            player = MediaPlayer().apply {
                setDataSource(this@AlarmRingingService, uri)
                setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                isLooping = true
                setVolume(0.25f * (volumePercent / 100f), 0.25f * (volumePercent / 100f))
                prepare()
                start()
            }
            if (vibrate) {
                val vibrator = getSystemService(Vibrator::class.java)
                vibrateLoop(vibrator)
            }
            rampJob = CoroutineScope(Dispatchers.Main).launch {
                val ceiling = (volumePercent.coerceIn(10, 100) / 100f)
                for (step in 0..10) { val current = minOf(ceiling, ceiling * (0.25f + 0.075f * step)); player?.setVolume(current, current); delay(20_000) }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrateLoop(vibrator: Vibrator) {
        if (Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 450, 350, 450), 0))
        else vibrator.vibrate(longArrayOf(0, 450, 350, 450), 0)
    }

    private fun stopAlarm() {
        rampJob?.cancel(); player?.let { runCatching { it.stop(); it.release() } }; player = null
        getSystemService(Vibrator::class.java).cancel()
        wakeLock?.let { if (it.isHeld) it.release() }; wakeLock = null
        stopForeground(STOP_FOREGROUND_REMOVE); stopSelf()
    }

    override fun onDestroy() { stopAlarm(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "com.fajralarm.STOP_ALARM"
        const val NOTIFICATION_ID = 8410
        fun stop(context: android.content.Context) { context.startService(Intent(context, AlarmRingingService::class.java).setAction(ACTION_STOP)) }
    }
}
