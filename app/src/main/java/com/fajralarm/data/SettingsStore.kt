package com.fajralarm.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "fajr_settings")

class SettingsStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val darkKey = booleanPreferencesKey("dark_theme")
    private val formatKey = stringPreferencesKey("time_format")
    private val onboardingKey = booleanPreferencesKey("onboarding_complete")
    private val targetKey = intPreferencesKey("default_mission_target")
    val darkTheme = context.settingsDataStore.data.map { it[darkKey] ?: true }
    val timeFormat = context.settingsDataStore.data.map { it[formatKey] ?: "24h" }
    val onboardingComplete = context.settingsDataStore.data.map { it[onboardingKey] ?: false }
    val defaultTarget = context.settingsDataStore.data.map { it[targetKey] ?: 30 }
    suspend fun setDark(enabled: Boolean) { context.settingsDataStore.edit { it[darkKey] = enabled } }
    suspend fun setTimeFormat(value: String) { context.settingsDataStore.edit { it[formatKey] = value } }
    suspend fun setOnboardingComplete() { context.settingsDataStore.edit { it[onboardingKey] = true } }
    suspend fun setDefaultTarget(value: Int) { context.settingsDataStore.edit { it[targetKey] = value.coerceIn(5, 100) } }
}
