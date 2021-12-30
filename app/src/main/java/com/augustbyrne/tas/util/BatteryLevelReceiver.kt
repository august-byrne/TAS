package com.augustbyrne.tas.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BatteryLevelReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BATTERY_LOW) {
            lowBattery = true
        }
        if (intent.action == Intent.ACTION_BATTERY_OKAY) {
            lowBattery = false
        }
    }

    companion object {
        var lowBattery: Boolean? = null
    }
}