package com.example.protosuite.ui.timer

import android.app.*
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.protosuite.R
import java.util.*
import kotlin.math.pow

class TimerBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "Broadcast Receiver Working", Toast.LENGTH_LONG).show()
    }
}

class NoteBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteName = intent.getStringExtra("com.example.protosuite.NoteName")
        val noteId = intent.getIntExtra("com.example.protosuite.NoteId", 0)
        val itemNameList = intent.getStringArrayExtra("com.example.protosuite.ItemNameList")
        val itemTimeList = intent.getLongArrayExtra("com.example.protosuite.ItemTimeList")!!
        val itemTimeTypeList = intent.getIntArrayExtra("com.example.protosuite.ItemTimeTypeList")!!
        val itemsCurrentIndex = intent.getIntExtra("com.example.protosuite.ItemListIndex", 0) + 1
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (itemsCurrentIndex < itemTimeList.size) {
            val secondsRemaining =
                itemTimeList[itemsCurrentIndex] * 60F.pow(itemTimeTypeList[itemsCurrentIndex])
                    .toLong()
            val wakeUpTime = Calendar.getInstance().timeInMillis + (secondsRemaining * 1000)
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val newIntent = Intent(context, this.javaClass).apply {
                putExtra("com.example.protosuite.NoteName", noteName)
                putExtra("com.example.protosuite.NoteId", noteId)
                putExtra("com.example.protosuite.ItemNameList", itemNameList)
                putExtra("com.example.protosuite.ItemTimeList", itemTimeList)
                putExtra("com.example.protosuite.ItemListIndex", itemsCurrentIndex)
                putExtra("com.example.protosuite.ItemTimeTypeList", itemTimeTypeList)
            }
            val pendingIntent =
                PendingIntent.getBroadcast(context, 0, newIntent, FLAG_UPDATE_CURRENT)
/*            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                wakeUpTime,
                pendingIntent
            )*/
            notificationManager.notify(
                42,
                notification(
                    noteName.toString(),
                    itemNameList?.get(itemsCurrentIndex).toString(),
                    wakeUpTime,
                    context
                )
            )
            Toast.makeText(
                context,
                "Broadcast Receiver $itemsCurrentIndex of ${itemTimeList.size}",
                Toast.LENGTH_LONG
            ).show()
            //PrefUtil.setAlarmEndTime(wakeUpTime, context)
        } else {
            Toast.makeText(context, "Broadcasts all done", Toast.LENGTH_LONG).show()
            notificationManager.notify(
                42,
                NotificationCompat.Builder(context, "note_alarm_timer")
                    .setContentTitle(noteName.toString())
                    .setContentText("Complete")
                    .setUsesChronometer(false)
                    .setSmallIcon(R.drawable.ic_baseline_play_arrow_24)
                    //.setContentIntent(this)
                    .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                    .setSound(null)
                    .setOngoing(false)
                    .setAutoCancel(true)
                    .setChannelId("note_alarm_timer")
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build()
            )
        }
    }

    fun notification(
        noteTitle: String,
        itemDescription: String,
        formattedDuration: Long,
        context: Context
    ): Notification {
        val channelId = "note_alarm_timer"
        val label = "Protosuite Task Timer"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(channelId, label, importance).apply {
                setSound(null, null)
                description = "Notification for timer functionality of the app"
            }
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(noteTitle)
            .setContentText(itemDescription)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(formattedDuration)
            .setShowWhen(true)
            .setSmallIcon(R.drawable.ic_baseline_play_arrow_24)
            //.setContentIntent(this)
            .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
            .setSound(null)
            .setOngoing(true)
            .setAutoCancel(true)
            .setChannelId(channelId)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        return builder.build()
    }
}

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