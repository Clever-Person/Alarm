package com.fajralarm.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour, minute") fun observeAlarms(): Flow<List<AlarmEntity>>
    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1") suspend fun get(id: Long): AlarmEntity?
    @Query("SELECT * FROM alarms WHERE enabled = 1") suspend fun enabled(): List<AlarmEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(alarm: AlarmEntity): Long
    @Update suspend fun update(alarm: AlarmEntity)
    @Delete suspend fun delete(alarm: AlarmEntity)
    @Insert suspend fun addEvent(event: WakeEventEntity)
    @Query("SELECT * FROM wake_events ORDER BY completedAt DESC") fun events(): Flow<List<WakeEventEntity>>
}
