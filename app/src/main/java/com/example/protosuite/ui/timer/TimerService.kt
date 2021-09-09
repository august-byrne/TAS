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
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
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
class TimerService : Service() {

    var isFirstRun = true

    //var timer: CountDownTimer? = null

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

    override fun onBind(p0: Intent?): IBinder? {
        // we don't want to implement binding
        return null
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_play_arrow_24)
            .setContentTitle(currentNote.title)
            .setContentText("currentNoteItems[itemIndex].activity")
            .setColor(yellow100.toArgb())
            .setColorized(true)
            .setContentIntent(getMainActivityPendingIntent())
            // TODO: add notification actions
            //.addAction(R.drawable.ic_baseline_drag_indicator_24, "rewind/previous item", rewindIntent)
            //.addAction(R.drawable.ic_baseline_drag_indicator_24, "play/pause item", playPauseIntent)
            //.addAction(R.drawable.ic_baseline_drag_indicator_24, "next item", fastForwardIntent)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
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
            var activeTimeLengthMilli = activeItem.time.times(1000L) * 60F.pow(activeItem.unit).toLong()
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

        private var _totalTimerLengthMilli: MutableLiveData<Long> = MutableLiveData(1L)
        val totalTimerLengthMilli: LiveData<Long> = _totalTimerLengthMilli

        private fun setTotalTimerLengthMilli(timeInMilli: Long) {
            _totalTimerLengthMilli.value = timeInMilli
        }
    }
}
