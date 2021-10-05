package com.example.protosuite.ui.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.protosuite.R
import com.example.protosuite.data.db.entities.DataItem
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.ui.MainActivity
import com.example.protosuite.ui.notes.TimerState
import com.example.protosuite.ui.values.yellow100
import kotlin.math.pow

// if lifecycle interaction with the service or lifecycleScope is needed, instead implement lifecycleService()
class TimerService : LifecycleService() {

    private var isFirstRun = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        //Timber.d("Resuming service...")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    //Timber.d("Paused service")
                }
                ACTION_STOP_SERVICE -> {
                    //Timber.d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

/*    override fun onBind(p0: Intent?): IBinder? {
        // we don't want to implement binding
        return null
    }*/

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        // Create a media session. NotificationCompat.MediaStyle
        // PlayerService is your own Service or Activity responsible for media playback.
        val mediaSession = MediaSessionCompat(this, "PlayerService")

        // Create a MediaStyle object and supply your media session token to it.
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_play_arrow_24)
            .setContentTitle(currentNote.title)
            //.setContentText(currentNoteItems[0].activity)
            .setColor(yellow100.toArgb())
            .setColorized(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(getMainActivityPendingIntent())
            // TODO: add notification actions
            .addAction(
                R.drawable.previous,
                "previous",
                getNotificationPendingIntent("PREV_ITEM", 0)
            )
            .addAction(R.drawable.pause, "play_pause", getNotificationPendingIntent("PAUSE"))
            .addAction(R.drawable.next, "next", getNotificationPendingIntent("NEXT_ITEM", 0))
            .setStyle(mediaStyle)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        var tempIndex = 0
        itemIndex.observe(this, { index ->
            tempIndex = index
            notificationBuilder
                .setContentText(currentNoteItems[index].activity)
                .clearActions()
                .addAction(
                    R.drawable.previous,
                    "previous",
                    getNotificationPendingIntent("PREV_ITEM", index)
                )
                .addAction(
                    if (timerState.value == TimerState.Running) {
                        R.drawable.pause
                    } else {
                        R.drawable.play
                           },
                    "play_pause", getNotificationPendingIntent(
                        if (timerState.value == TimerState.Running) {
                            "PAUSE"
                        } else {
                            "PLAY"
                        }
                    ))
                .addAction(
                    R.drawable.next,
                    "next",
                    getNotificationPendingIntent("NEXT_ITEM", index)
                )
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        })

        timerState.observe(this, { timerState ->
            when (timerState) {
                TimerState.Stopped -> {
                    stopForeground(Service.STOP_FOREGROUND_REMOVE)
                }
                TimerState.Paused -> {
                    notificationBuilder
                        .clearActions()
                        .addAction(
                            R.drawable.previous,
                            "previous",
                            getNotificationPendingIntent("PREV_ITEM", tempIndex)
                        )
                        .addAction(R.drawable.play, "play_pause", getNotificationPendingIntent("PLAY"))
                        .addAction(
                            R.drawable.next,
                            "next",
                            getNotificationPendingIntent("NEXT_ITEM", tempIndex)
                        )
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                }
                TimerState.Running -> {
                    notificationBuilder
                        .clearActions()
                        .addAction(
                            R.drawable.previous,
                            "previous",
                            getNotificationPendingIntent("PREV_ITEM", tempIndex)
                        )
                        .addAction(R.drawable.pause, "play_pause", getNotificationPendingIntent("PAUSE"))
                        .addAction(
                            R.drawable.next,
                            "next",
                            getNotificationPendingIntent("NEXT_ITEM", tempIndex)
                        )
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                }
                else -> {
                }
            }
        })
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
    )

    private fun getNotificationPendingIntent(clickAction: String, index: Int? = null) = PendingIntent.getBroadcast(
        this,
        0,
        Intent(this, NotificationReceiver::class.java)
            .also {
                it.action = clickAction
                if (index != null) {
                    it.putExtra("com.example.protosuite.ItemListIndex", index)
                }
            },
        FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
        const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

        const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Tracking"
        const val NOTIFICATION_ID = 1

        val isTiming = MutableLiveData<Boolean>()

        var currentNote by mutableStateOf(NoteItem(0, null, null, 0, "", ""))
        var currentNoteItems = mutableStateListOf<DataItem>()

        private var timer: CountDownTimer? = null

        //takes care of all time unit (and some timer state) manipulation
        fun startTimer(itemIndex: Int) {
            setActiveItemIndex(itemIndex)
            val activeItem = currentNoteItems[itemIndex]
            var activeTimeLengthMilli =
                activeItem.time.times(1000L) * 60F.pow(activeItem.unit).toLong()
            setTotalTimerLengthMilli(activeTimeLengthMilli)
            if (isPaused) {
                isPaused = false
                activeTimeLengthMilli = tempSavedTimerLengthMilli
            }
            setTimerLength(activeTimeLengthMilli)
            setTimerState(TimerState.Running)
            timer = object : CountDownTimer(activeTimeLengthMilli, 10L) {
                override fun onTick(millisUntilFinished: Long) {
                    setTimerLength(millisUntilFinished)
                }

                override fun onFinish() {
                    if (itemIndex < currentNoteItems.lastIndex) {
                        setActiveItemIndex(itemIndex.inc())
                        startTimer(itemIndex.inc())
                    } else {
                        setTimerState(TimerState.Stopped)
                        setActiveItemIndex(0)
                    }
                }
            }.start()
        }

        fun stopTimer() {
            setTimerState(TimerState.Stopped)
            isPaused = false
            timer?.cancel()
        }

        fun modifyTimer(index: Int) {
            stopTimer()
            if (index in 0..currentNoteItems.lastIndex) {
                startTimer(index)
            } else {
                setActiveItemIndex(0)
            }
        }

        fun pauseTimer(currentTimerLength: Long) {
            timer?.cancel()
            setTimerState(TimerState.Paused)
            isPaused = true
            tempSavedTimerLengthMilli = currentTimerLength
        }

        private var tempSavedTimerLengthMilli = 0L
        private var isPaused: Boolean = false

        fun initTimerService(note: NoteItem, dataItems: List<DataItem>, index: Int = 0) {
            currentNote = note
            currentNoteItems = dataItems.toMutableStateList()
            stopTimer()
            setActiveItemIndex(index)
            startTimer(index)
        }

        // LiveData holds state which is observed by the UI
        // (state flows down from ViewModel)
        private var _timerLengthMilli: MutableLiveData<Long> = MutableLiveData(1L)
        val timerLengthMilli: LiveData<Long> = _timerLengthMilli

        // setTimerLength is an event we're defining that the UI can invoke
        // (events flow up from UI)
        fun setTimerLength(timerLength: Long) {
            if (timerLength >= 0L) {
                _timerLengthMilli.value = timerLength
            } else {
                _timerLengthMilli.value = 0L
            }
        }

        private var _timerState = MutableLiveData(TimerState.Stopped)
        val timerState: LiveData<TimerState> = _timerState

        fun setTimerState(timerState: TimerState) {
            _timerState.value = timerState
        }

        private var _itemIndex = MutableLiveData(0)
        val itemIndex: LiveData<Int> = _itemIndex

        fun setActiveItemIndex(itemIndex: Int) {
            if (itemIndex >= 0) {
                _itemIndex.value = itemIndex
            }
        }

        fun decItemIndex() {
            _itemIndex.value.let {
                if (it != null && it > 0) {
                    _itemIndex.value?.dec()
                }
            }
        }

        fun incItemIndex() {
            _itemIndex.value?.inc()
        }

        private var _totalTimerLengthMilli: MutableLiveData<Long> = MutableLiveData(1L)
        val totalTimerLengthMilli: LiveData<Long> = _totalTimerLengthMilli

        private fun setTotalTimerLengthMilli(timeInMilli: Long) {
            _totalTimerLengthMilli.value = timeInMilli
        }
    }
}
