package com.augustbyrne.tas.ui.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.media.ToneGenerator
import android.media.session.MediaSession
import android.os.*
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.augustbyrne.tas.R
import com.augustbyrne.tas.data.PreferenceManager
import com.augustbyrne.tas.data.db.entities.DataItem
import com.augustbyrne.tas.data.db.entities.NoteItem
import com.augustbyrne.tas.data.db.entities.NoteWithItems
import com.augustbyrne.tas.ui.MainActivity
import com.augustbyrne.tas.ui.values.yellow100
import com.augustbyrne.tas.util.CompletionType
import com.augustbyrne.tas.util.TimerState
import kotlin.math.pow

// Since lifecycle interaction with the service or lifecycleScope is needed, we implement lifecycleService()
class TimerService : LifecycleService() {

    private var isFirstRun = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                }
                ACTION_STOP_SERVICE -> {
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        createNotificationChannel(notificationManager)

        val mediaSession = MediaSession(this, "TimerService")

        val mediaStyle = Notification.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)

        fun actionPrevious(index: Int = 0) = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.previous),
            "previous",
            getNotificationPendingIntent("PREV_ITEM", index)
        )

        fun actionNext(index: Int = 0) = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.next),
            "next",
            getNotificationPendingIntent("NEXT_ITEM", index)
        )

        fun actionPlay(index: Int = 0) = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.play),
            "play_pause",
            getNotificationPendingIntent("PLAY", index)
        )

        fun actionPause(index: Int = 0) = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.pause),
            "play_pause",
            getNotificationPendingIntent("PAUSE", index)
        )

        val notificationBuilder = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.play)
            .setColor(yellow100.toArgb())
            .setColorized(true)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setContentIntent(getMainActivityPendingIntent())
            .setActions(
                actionPrevious().build(),
                actionPause().build(),
                actionNext().build()
            )
            .setStyle(mediaStyle)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        val vibratorService = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val vibration = PreferenceManager(applicationContext).vibrationFlow.asLiveData()

        vibration.observe(this) { vibrateOn ->
            completionType.observe(this) { done: CompletionType? ->
                when (done) {
                    CompletionType.Normal -> {
                        beeper.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
                        if (vibrateOn) {
                            vibratorService.vibrate(
                                VibrationEffect.createOneShot(
                                    500,
                                    VibrationEffect.DEFAULT_AMPLITUDE
                                )
                            )
                        }
                        completionType.value = null
                    }
                    CompletionType.Final -> {
                        Toast.makeText(this, "Timed Activity Complete", Toast.LENGTH_SHORT).show()
                        beeper.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
                        if (vibrateOn) {
                            vibratorService.vibrate(
                                VibrationEffect.createWaveform(
                                    longArrayOf(
                                        0L,
                                        200L,
                                        300L,
                                        500L
                                    ), -1
                                )
                            )
                        }
                        completionType.value = null
                    }
                    else -> {}
                }
            }
        }

        itemIndex.observe(this) { index ->
            notificationBuilder
                .setContentTitle(currentNote.title)
                .setContentText(currentNoteItems[index].activity)
                .setActions(
                    actionPrevious(index).build(),
                    if (timerState.value == TimerState.Running) {
                        actionPause(index).build()
                    } else {
                        actionPlay(index).build()
                    },
                    actionNext(index).build()
                )
            if (internalTimerState != TimerState.Stopped) {
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
            }
        }

        timerState.observe(this) { timerState ->
            when (timerState) {
                TimerState.Stopped -> {
                    stopForeground(true)
                    //notificationManager.cancel(NOTIFICATION_ID)
                }
                TimerState.Paused -> {
                    notificationBuilder
                        .setActions(
                            actionPrevious(internalIndex).build(),
                            actionPlay(internalIndex).build(),
                            actionNext(internalIndex).build()
                        )
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                }
                TimerState.Running -> {
                    notificationBuilder
                        .setActions(
                            actionPrevious(internalIndex).build(),
                            actionPause(internalIndex).build(),
                            actionNext(internalIndex).build()
                        )
                    startForeground(NOTIFICATION_ID, notificationBuilder.build())
                    //notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                }
                TimerState.Delayed -> {
                    stopForeground(true)
                }
                else -> {
                }
            }
        }
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
    )

    private fun getNotificationPendingIntent(clickAction: String, index: Int) =
        PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, NotificationReceiver::class.java)
                .also {
                    it.action = clickAction
                    it.putExtra("com.augustbyrne.tas.ItemListIndex", index)
                },
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

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

        var currentNote by mutableStateOf(NoteItem())
        var currentNoteItems = mutableStateListOf<DataItem>()

        private var completionType: MutableLiveData<CompletionType?> = MutableLiveData(null)
        private val beeper: ToneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        private var timer: CountDownTimer? = null
        private var delayedTimer: CountDownTimer? = null

        //takes care of all time unit (and some timer state) manipulation
        fun startTimer(itemIndex: Int = 0) {
            setActiveItemIndex(itemIndex)
            val activeItem = currentNoteItems[itemIndex]
            var activeTimeLengthMilli =
                activeItem.time.times(1000L) * 60F.pow(activeItem.unit).toLong()
            setTotalTimerLengthMilli(activeTimeLengthMilli)
            if (internalTimerState == TimerState.Paused) {
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
                        completionType.value = CompletionType.Normal
                        startTimer(itemIndex.inc())
                    } else {
                        completionType.value = CompletionType.Final
                        stopTimer()
                    }
                }
            }.start()
        }

        fun delayedStart(length: Int = 5, itemIndex: Int = 0) {
            if (length <= 0) {
                startTimer(itemIndex)
            } else {
                setTimerLength(length * 1000L)
                setTimerState(TimerState.Delayed)
                delayedTimer = object : CountDownTimer(length * 1000L, 1000L) {
                    override fun onTick(millisUntilFinished: Long) {
                        setTimerLength(millisUntilFinished)
                    }

                    override fun onFinish() {
                        CompletionType.Normal
                        delayedTimer?.cancel()
                        startTimer(itemIndex)
                    }
                }.start()
            }
        }

        fun stopTimer(index: Int = 0) {
            timer?.cancel()
            setTimerState(TimerState.Stopped)
            setActiveItemIndex(index)
            val firstItem = currentNoteItems[index]
            val firstTimeLengthMilli =
                firstItem.time.times(1000L) * 60F.pow(firstItem.unit).toLong()
            setTotalTimerLengthMilli(firstTimeLengthMilli)
            setTimerLength(firstTimeLengthMilli)
        }

        fun modifyTimer(index: Int) {
            timer?.cancel()
            if (index in 0..currentNoteItems.lastIndex) {
                if (internalTimerState == TimerState.Running) {
                    startTimer(index)
                } else {
                    stopTimer(index)
                }
            } else {
                stopTimer()
            }
        }

        fun pauseTimer(currentTimerLength: Long) {
            if (currentTimerLength != 0L) {
                timer?.cancel()
                setTimerState(TimerState.Paused)
                tempSavedTimerLengthMilli = currentTimerLength
            }
        }

        private var tempSavedTimerLengthMilli = 0L
        private var internalTimerState: TimerState = TimerState.Stopped
        private var internalIndex: Int = 0

        fun initTimerServiceValues(noteWithItems: NoteWithItems, index: Int = 0) {
            currentNote = noteWithItems.note
            currentNoteItems = noteWithItems.dataItems.toMutableStateList()
            stopTimer(index)
        }

        private var serviceTimerLength: MutableLiveData<Long> = MutableLiveData(1L)
        val timerLengthMilli: LiveData<Long> = serviceTimerLength

        fun setTimerLength(timerLength: Long) {
            if (timerLength >= 0L) {
                serviceTimerLength.value = timerLength
            } else {
                serviceTimerLength.value = 0L
            }
        }

        private var serviceTimerState = MutableLiveData(TimerState.Stopped)
        val timerState: LiveData<TimerState> = serviceTimerState

        private fun setTimerState(timerState: TimerState) {
            serviceTimerState.value = timerState
            internalTimerState = timerState
        }

        private var serviceItemIndex = MutableLiveData(0)
        val itemIndex: LiveData<Int> = serviceItemIndex

        private fun setActiveItemIndex(itemIndex: Int) {
            if (itemIndex >= 0) {
                serviceItemIndex.value = itemIndex
                internalIndex = itemIndex
            }
        }

        private var serviceTotalTimerLength: MutableLiveData<Long> = MutableLiveData(1L)
        val totalTimerLengthMilli: LiveData<Long> = serviceTotalTimerLength

        private fun setTotalTimerLengthMilli(timeInMilli: Long) {
            serviceTotalTimerLength.value = timeInMilli
        }
    }
}
