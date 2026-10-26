package com.fajralarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.view.WindowManager
import com.fajralarm.alarm.AlarmScheduler
import com.fajralarm.ui.AlarmTaskScreen
import com.fajralarm.ui.FajrApp
import com.fajralarm.ui.theme.FajrTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var ringingAlarmId by mutableStateOf<Long?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        routeIntent(intent)
        setContent {
            FajrTheme {
                val alarmId = ringingAlarmId
                if (alarmId == null) FajrApp()
                else { BackHandler(enabled = true) {}; AlarmTaskScreen(alarmId) }
            }
        }
    }
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        routeIntent(intent)
    }
    private fun routeIntent(intent: android.content.Intent?) {
        ringingAlarmId = intent?.takeIf { it.hasExtra(AlarmScheduler.EXTRA_ALARM_ID) }?.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, 0L)
        if (ringingAlarmId != null) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
    }
}
