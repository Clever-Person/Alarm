package com.fajralarm.data

import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room

@Database(entities = [AlarmEntity::class, WakeEventEntity::class], version = 1, exportSchema = true)
abstract class FajrDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    companion object {
        fun open(context: Context): FajrDatabase = Room.databaseBuilder(context.applicationContext, FajrDatabase::class.java, "fajr_alarm.db").build()
    }
}
