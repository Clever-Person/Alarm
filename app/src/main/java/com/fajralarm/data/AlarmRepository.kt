package com.fajralarm.data

import javax.inject.Inject

class AlarmRepository @Inject constructor(private val dao: AlarmDao) {
    val alarms = dao.observeAlarms()
    val events = dao.events()
    suspend fun save(alarm: AlarmEntity): Long = if (alarm.id == 0L) dao.insert(alarm) else { dao.update(alarm); alarm.id }
    suspend fun delete(alarm: AlarmEntity) = dao.delete(alarm)
    suspend fun byId(id: Long) = dao.get(id)
    suspend fun enabled() = dao.enabled()
    suspend fun record(event: WakeEventEntity) = dao.addEvent(event)
}
