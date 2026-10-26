package com.fajralarm.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import com.fajralarm.ui.theme.FajrTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        val id = intent.getLongExtra(com.fajralarm.alarm.AlarmScheduler.EXTRA_ALARM_ID, 0L)
        setContent { FajrTheme { BackHandler(enabled = true) {}; AlarmTaskScreen(id) } }
    }
    @Suppress("DEPRECATION") override fun onBackPressed() { /* Alarm stays active until its mission is complete. */ }
}
