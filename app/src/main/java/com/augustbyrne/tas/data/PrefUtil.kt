package com.augustbyrne.tas.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "com.augustbyrne.tas.preferences")

class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
    ) {

    companion object {
        private val PREF_SHOW_ADS = booleanPreferencesKey("ads_state")
        private val PREF_DARK_MODE = intPreferencesKey("dark_mode_state")
        private val PREF_TIMER_THEME = intPreferencesKey("timer_theme_state")
        private val PREF_SORT_BY = intPreferencesKey("sort_type")
        private val PREF_LAST_TIME_UNIT = intPreferencesKey("previous_time_unit")
        private val PREF_VIBRATE = booleanPreferencesKey("vibration_state")
        private val PREF_START_DELAY = intPreferencesKey("start_delay")
    }

    val showAdsFlow: Flow<Boolean> = context.dataStore.data.map { it[PREF_SHOW_ADS] ?: true }
    suspend fun setShowAds(value: Boolean) = context.dataStore.edit { it[PREF_SHOW_ADS] = value }

    val isDarkThemeFlow: Flow<Int> = context.dataStore.data.map { it[PREF_DARK_MODE] ?: 0 }
    suspend fun setIsDarkTheme(value: Int) = context.dataStore.edit { it[PREF_DARK_MODE] = value }

    val timerThemeFlow: Flow<Int> = context.dataStore.data.map { it[PREF_TIMER_THEME] ?: 0 }
    suspend fun setTimerTheme(value: Int) = context.dataStore.edit { it[PREF_TIMER_THEME] = value }

    val sortTypeFlow: Flow<Int> = context.dataStore.data.map { it[PREF_SORT_BY] ?: 0 }
    suspend fun setSortType(value: Int) = context.dataStore.edit { it[PREF_SORT_BY] = value }

    val lastUsedTimeUnitFlow: Flow<Int> =
        context.dataStore.data.map { it[PREF_LAST_TIME_UNIT] ?: 0 }

    suspend fun setLastUsedTimeUnit(value: Int) =
        context.dataStore.edit { it[PREF_LAST_TIME_UNIT] = value }

    val vibrationFlow: Flow<Boolean> = context.dataStore.data.map { it[PREF_VIBRATE] ?: true }
    suspend fun setVibration(value: Boolean) = context.dataStore.edit { it[PREF_VIBRATE] = value }

    val startDelay: Flow<Int> = context.dataStore.data.map { it[PREF_START_DELAY] ?: 5 }
    suspend fun setStartDelay(value: Int) = context.dataStore.edit { it[PREF_START_DELAY] = value }
}
