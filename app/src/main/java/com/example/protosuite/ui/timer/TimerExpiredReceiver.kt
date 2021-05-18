package com.example.protosuite.ui.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        //NotificationUtil.showTimerExpired(context)
        Toast.makeText(context, "Broadcast Receiver Working", Toast.LENGTH_LONG).show()

        PrefUtil.setTimerState(Timer.TimerState.Stopped, context)
        //PrefUtil.setAlarmSetTime(0, context)
    }
}
