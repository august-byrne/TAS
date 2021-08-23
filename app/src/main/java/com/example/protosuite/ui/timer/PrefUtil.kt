package com.example.protosuite.ui.timer

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
    ) {

    companion object {
        private const val PREF_PACKAGE_NAME = "com.augustbyrne.protosuite.preferences"
        private const val PREF_KEY_TIMER = "timer"
        private const val PREF_KEY_TEMP_TIMER = "temp_timer"
        private const val PREF_KEY_TIMER_RUNNING = "timer_running"
        private const val PREF_KEY_TIMER_STATE = "timer_state"
        private const val PREF_KEY_NOTE_ID = "note_id"
        private const val PREF_KEY_ITEM_INDEX = "item_index"
        private const val PREF_KEY_PREVIOUS_UNIT = "previous_unit"
    }

    private val pref: SharedPreferences =
        context.getSharedPreferences(PREF_PACKAGE_NAME, Context.MODE_PRIVATE)

    var timeInMillis: Long
        get() = pref.getLong(PREF_KEY_TIMER, 0)
        set(timer) = pref.edit().putLong(PREF_KEY_TIMER, timer).apply()

    var tempTimeInMillis: Long
        get() = pref.getLong(PREF_KEY_TEMP_TIMER, 0)
        set(temp) = pref.edit().putLong(PREF_KEY_TEMP_TIMER, temp).apply()

    var isTimerRunning: Boolean
        get() = pref.getBoolean(PREF_KEY_TIMER_RUNNING, false)
        set(isTimerRunning) = pref.edit().putBoolean(PREF_KEY_TIMER_RUNNING, isTimerRunning).apply()

    var timerState: TimerState
        get() = TimerState.values()[pref.getInt(PREF_KEY_TIMER_STATE, 0)]
        set(timerState) = pref.edit().putInt(PREF_KEY_TIMER_STATE, timerState.ordinal).apply()

    var noteId: Int
        get() = pref.getInt(PREF_KEY_NOTE_ID, 0)
        set(noteId) = pref.edit().putInt(PREF_KEY_NOTE_ID, noteId).apply()

    var itemIndex: Int
        get() = pref.getInt(PREF_KEY_ITEM_INDEX, 0)
        set(itemIndex) = pref.edit().putInt(PREF_KEY_ITEM_INDEX, itemIndex).apply()

    var lastUsedTimeUnit: Int
        get() = pref.getInt(PREF_KEY_PREVIOUS_UNIT, 0)
        set(unitOfTime) = pref.edit().putInt(PREF_KEY_PREVIOUS_UNIT, unitOfTime).apply()

}


@ExperimentalAnimationApi
@Deprecated("use PreferenceManager instead")
object PrefUtil{

    /*
    private const val TIMER_LENGTH_ID = "com.example.protosuite.timer_length"
    fun getTimerLength(context: Context): Int{
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getInt(TIMER_LENGTH_ID, 10)
    }

     */

    private const val PREVIOUS_TIMER_LENGTH_SECONDS_ID = "com.example.protosuite.previous_timer_length_seconds"

    fun getPreviousTimerLengthSeconds(context: Context): Long{
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, 0)
    }

    fun setPreviousTimerLengthSeconds(seconds: Long, context: Context){
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, seconds)
        editor.apply()
    }


    private const val TIMER_STATE_ID = "com.example.protosuite.timer_state"

    //All TimerState were originally TimerFragment.TimerState
    fun getTimerState(context: Context): TimerState {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val ordinal = preferences.getInt(TIMER_STATE_ID, 0)
        return TimerState.values()[ordinal]
    }

    fun setTimerState(state: TimerState, context: Context){
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        val ordinal = state.ordinal
        editor.putInt(TIMER_STATE_ID, ordinal)
        editor.apply()
    }


    private const val SECONDS_REMAINING_ID = "com.example.protosuite.seconds_remaining"

    fun getSecondsRemaining(context: Context): Long{
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getLong(SECONDS_REMAINING_ID, 0)
    }

    fun setSecondsRemaining(seconds: Long, context: Context){
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putLong(SECONDS_REMAINING_ID, seconds)
        editor.apply()
    }


    private const val ALARM_END_TIME_ID = "com.example.protosuite.backgrounded_time"

    fun getAlarmEndTime(context: Context): Long{
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return  preferences.getLong(ALARM_END_TIME_ID, 0)
    }

    fun setAlarmEndTime(time: Long, context: Context){
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putLong(ALARM_END_TIME_ID, time)
        editor.apply()
    }

}