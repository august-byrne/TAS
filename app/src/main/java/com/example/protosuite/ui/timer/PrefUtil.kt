package com.example.protosuite.ui.timer

import android.content.Context
import android.content.SharedPreferences
import com.example.protosuite.ui.notes.SortType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
    ) {

    companion object {
        private const val PREF_PACKAGE_NAME = "com.purin.tas.preferences"
        private const val PREF_KEY_TIMER = "timer"
        private const val PREF_KEY_SHOW_ADS = "ads_state"
        private const val PREF_KEY_PREVIOUS_UNIT = "previous_unit"
        private const val PREF_KEY_SORT_BY = "sort_type"
        private const val DARK_MODE_ENABLED = "dark_mode_enabled"
    }

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

}
