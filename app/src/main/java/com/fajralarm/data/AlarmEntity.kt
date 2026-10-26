package com.fajralarm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val repeatDays: String = "",
    val label: String = "Wake up",
    val mission: String = "SHAKE",
    val missionTarget: Int = 30,
    val sound: String = "default",
    val volume: Int = 75,
    val vibrate: Boolean = true,
    val enabled: Boolean = true
)

@Entity(tableName = "wake_events")
data class WakeEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alarmId: Long,
    val startedAt: Long,
    val completedAt: Long,
    val successful: Boolean = true
)
