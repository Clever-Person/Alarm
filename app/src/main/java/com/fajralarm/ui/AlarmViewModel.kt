package com.fajralarm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fajralarm.alarm.AlarmScheduler
import com.fajralarm.data.AlarmEntity
import com.fajralarm.data.AlarmRepository
import com.fajralarm.data.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val repo: AlarmRepository,
    private val scheduler: AlarmScheduler,
    private val settings: SettingsStore
) : ViewModel() {
    val alarms = repo.alarms.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val events = repo.events.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val darkTheme = settings.darkTheme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
    val onboardingComplete = settings.onboardingComplete.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val timeFormat = settings.timeFormat.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "24h")
    val defaultTarget = settings.defaultTarget.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 30)
    fun save(alarm: AlarmEntity) = viewModelScope.launch { val id = repo.save(alarm); repo.byId(id)?.let(scheduler::schedule) }
    fun toggle(alarm: AlarmEntity) = viewModelScope.launch { val changed = alarm.copy(enabled = !alarm.enabled); repo.save(changed); if (changed.enabled) scheduler.schedule(changed) else scheduler.cancel(changed.id) }
    fun delete(alarm: AlarmEntity) = viewModelScope.launch { scheduler.cancel(alarm.id); repo.delete(alarm) }
    fun setDark(value: Boolean) = viewModelScope.launch { settings.setDark(value) }
    fun rescheduleEnabled() = viewModelScope.launch { repo.enabled().forEach(scheduler::schedule) }
    fun setTimeFormat(value: String) = viewModelScope.launch { settings.setTimeFormat(value) }
    fun setDefaultTarget(value: Int) = viewModelScope.launch { settings.setDefaultTarget(value) }
    fun finishOnboarding() = viewModelScope.launch { settings.setOnboardingComplete() }
}
