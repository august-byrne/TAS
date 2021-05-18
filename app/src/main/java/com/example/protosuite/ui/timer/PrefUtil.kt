package com.example.protosuite.ui.timer

import android.content.Context
import androidx.preference.PreferenceManager


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

    fun getTimerState(context: Context): Timer.TimerState{
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val ordinal = preferences.getInt(TIMER_STATE_ID, 0)
        return Timer.TimerState.values()[ordinal]
    }

    fun setTimerState(state: Timer.TimerState, context: Context){
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