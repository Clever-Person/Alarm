package com.fajralarm.di

import android.content.Context
import androidx.room.Room
import com.fajralarm.data.AlarmDao
import com.fajralarm.data.FajrDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): FajrDatabase =
        Room.databaseBuilder(context, FajrDatabase::class.java, "fajr_alarm.db").build()
    @Provides fun alarmDao(db: FajrDatabase): AlarmDao = db.alarmDao()
}
