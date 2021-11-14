package com.example.protosuite.ui.timer

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

// At the top level of your kotlin file:
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "com.purin.tas.preferences")

class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
    ) {

    companion object {
        private val PREF_SHOW_ADS = booleanPreferencesKey("ads_state")
        private val PREF_DARK_MODE = booleanPreferencesKey("dark_mode_state")
        private val PREF_SORT_BY = intPreferencesKey("sort_type")
        private val PREF_LAST_TIME_UNIT = intPreferencesKey("previous_time_unit")
    }

    val showAdsFlow: Flow<Boolean> = context.dataStore.data.map{ it[PREF_SHOW_ADS] ?: true }
    suspend fun setShowAds(value: Boolean) = context.dataStore.edit { it[PREF_SHOW_ADS] = value }

    val isDarkThemeFlow: Flow<Boolean> = context.dataStore.data.map{ it[PREF_DARK_MODE] ?: false }
    suspend fun setIsDarkTheme(value: Boolean) = context.dataStore.edit { it[PREF_DARK_MODE] = value }

    val sortTypeFlow: Flow<Int> = context.dataStore.data.map{ it[PREF_SORT_BY] ?: 0 }
    suspend fun setSortType(value: Int) = context.dataStore.edit { it[PREF_SORT_BY] = value }

    val lastUsedTimeUnitFlow: Flow<Int> = context.dataStore.data.map{ it[PREF_LAST_TIME_UNIT] ?: 0 }
    suspend fun setLastUsedTimeUnit(value: Int) = context.dataStore.edit { it[PREF_LAST_TIME_UNIT] = value }

}
