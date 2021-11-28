package com.augustbyrne.tas.ui.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        val currentIndex = intent.getIntExtra("com.example.protosuite.ItemListIndex", 0)
        when(intent.action) {
            "PREV_ITEM" -> {
                TimerService.modifyTimer(currentIndex.dec())
            }
            "PLAY" -> {
                TimerService.startTimer(currentIndex)
            }
            "PAUSE" -> {
                TimerService.timerLengthMilli.value?.let { TimerService.pauseTimer(it) }
            }
            "NEXT_ITEM" -> {
                TimerService.modifyTimer(currentIndex.inc())
            }
        }
    }

}