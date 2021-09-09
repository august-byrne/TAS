package com.example.protosuite.ui.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi

@ExperimentalAnimationApi
class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        //NotificationUtil.showTimerExpired(context)
        Toast.makeText(context, "Broadcast Receiver Working", Toast.LENGTH_LONG).show()

        //PrefUtil.setTimerState(TimerState.Stopped, context)
        //PrefUtil.setAlarmSetTime(0, context)
    }
}
