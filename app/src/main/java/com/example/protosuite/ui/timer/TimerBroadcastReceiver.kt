package com.example.protosuite.ui.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class TimerBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "Broadcast Receiver Working", Toast.LENGTH_LONG).show()
    }


    /*
        private fun notification(formattedDuration: String): Notification {
        val channelId = "simple_alarm_timer"
        val label = getString(R.string.timer)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (isOreoPlus()) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            NotificationChannel(channelId, label, importance).apply {
                setSound(null, null)
                notificationManager.createNotificationChannel(this)
            }
        }

        val builder = NotificationCompat.Builder(this)
                .setContentTitle(label)
                .setContentText(formattedDuration)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentIntent(this.getOpenTimerTabIntent())
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setSound(null)
                .setOngoing(true)
                .setAutoCancel(true)
                .setChannelId(channelId)

        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        return builder.build()
    }
     */
}