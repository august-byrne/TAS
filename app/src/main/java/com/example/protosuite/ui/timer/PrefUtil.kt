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
/*
        private const val PREF_PACKAGE_NAME = "com.purin.tas.preferences"
        private const val PREF_KEY_TIMER = "timer"
        private const val PREF_KEY_SHOW_ADS = "ads_state"
        private const val PREF_KEY_PREVIOUS_UNIT = "previous_unit"
        private const val PREF_KEY_SORT_BY = "sort_type"
        private const val DARK_MODE_ENABLED = "dark_mode_enabled"
*/

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

/*
    private val pref: SharedPreferences =
        context.getSharedPreferences(PREF_PACKAGE_NAME, Context.MODE_PRIVATE)

    var showAds: Boolean
        get() = pref.getBoolean(PREF_KEY_SHOW_ADS, true)
        set(value) = pref.edit().putBoolean(PREF_KEY_SHOW_ADS, value).apply()

    var isDarkTheme: Boolean
        get() = pref.getBoolean(DARK_MODE_ENABLED, false)
        set(value) = pref.edit().putBoolean(DARK_MODE_ENABLED, value).apply()

    var timeInMillis: Long
        get() = pref.getLong(PREF_KEY_TIMER, 0)
        set(timer) = pref.edit().putLong(PREF_KEY_TIMER, timer).apply()

    var lastUsedTimeUnit: Int
        get() = pref.getInt(PREF_KEY_PREVIOUS_UNIT, 0)
        set(unitOfTime) = pref.edit().putInt(PREF_KEY_PREVIOUS_UNIT, unitOfTime).apply()

    var sortType: SortType
        get() = SortType.values()[pref.getInt(PREF_KEY_SORT_BY, 0)]
        set(sortType) = pref.edit().putInt(PREF_KEY_SORT_BY, sortType.ordinal).apply()
*/
}
