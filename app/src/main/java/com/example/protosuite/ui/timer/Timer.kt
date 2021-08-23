package com.example.protosuite.ui.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.protosuite.ui.notes.NoteViewModel
import com.example.protosuite.ui.values.NotesTheme
import com.example.protosuite.ui.values.yellow50
import java.util.*


enum class TimerState {
    Stopped, Paused, Running
}

//val nowSeconds: Long
//    get() = Calendar.getInstance().timeInMillis / 1000

@ExperimentalAnimationApi
fun setAlarm(context: Context, secondsRemaining: Long) {
    val wakeUpTime = Calendar.getInstance().timeInMillis + (secondsRemaining * 1000)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, TimerBroadcastReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, FLAG_UPDATE_CURRENT)
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
    PrefUtil.setAlarmEndTime(wakeUpTime, context)
}

@ExperimentalAnimationApi
fun removeAlarm(context: Context) {
    val intent = Intent(context, TimerBroadcastReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
    PrefUtil.setAlarmEndTime(0, context)
}

// play: run parsed time on clock, pause, stop alarm and leave time on clock (and save time in preferences
// stop: remove alarm and reset time to 0
// on a button press save TimerState to preferences
// on TimerState.Stopped, nothing, Paused, return value to timer, running, create time left value and start countdown timer
@ExperimentalAnimationApi
@Composable
fun TimerUI(myViewModel: NoteViewModel) {
    val timerLength: Long by myViewModel.timerLengthMilli.observeAsState(0)//timerLength.observeAsState(0)
    val timerState: TimerState by myViewModel.timerState.observeAsState(TimerState.Stopped)
    val hour = timerLength.div(60 * 60)
    val min = timerLength.div(60).mod(60)
    val sec = timerLength.mod(60)
    val formattedTimerLength: String = String.format("%02d:%02d:%02d", hour, min, sec)
    NotesTheme(darkTheme = false) {
        Column(
            modifier = Modifier
                .padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
                .fillMaxSize()
                .background(yellow50),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                TimeButtonsAdd(myViewModel)
                //if (TimerState.Running && timer <= 5sec){flashing text}else{regular text}
                if (timerState == TimerState.Running && timerLength <= 5) {
                    FlashingTimerText(formattedTimerLength)
                } else {
                    TimerText(Modifier, formattedTimerLength)
                }
                TimeButtonsSub(myViewModel)
            }
            PlayPauseStop(myViewModel)
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun PlayPauseStop(myViewModel: NoteViewModel) {
    val context = LocalContext.current
    val timerLength: Long by myViewModel.timerLengthMilli.observeAsState(0)
    val timerState: TimerState by myViewModel.timerState.observeAsState(
        PrefUtil.getTimerState(
            context
        )
    )
    val icon =
        if (timerState == TimerState.Running) Icons.Default.Pause else Icons.Default.PlayArrow
    val tint by animateColorAsState(
        targetValue = if (timerState == TimerState.Running) Color.Yellow else Color.Green,
        animationSpec = tween(
            durationMillis = 400,
            easing = LinearEasing
        )
    )
    //var expand by remember { mutableStateOf(false)}
    //val stopVisible: Boolean = timerState != TimerState.Stopped
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        FloatingActionButton(
            onClick = {
                if (timerLength != 0L) {
                    if (timerState == TimerState.Running) { // Clicked Pause
                        //myViewModel.setTimerState(TimerState.Paused, context)
                        removeAlarm(context)
                        myViewModel.stopTimer()
                    } else {    // Clicked Start
                        if (timerState == TimerState.Stopped) {
                            PrefUtil.setPreviousTimerLengthSeconds(timerLength, context)
                        }
                        //myViewModel.setTimerState(TimerState.Running, context)
                        setAlarm(
                            context = context,
                            secondsRemaining = timerLength
                        )
                        //myViewModel.startTimer(timerLength, context)
                    }
                }
            },
            backgroundColor = tint
        ) {
            Icon(icon, contentDescription = "Start or Pause")
        }
        AnimatedVisibility(
            visible = timerState != TimerState.Stopped,
            enter = slideInHorizontally() + fadeIn(),
            exit = slideOutHorizontally() + fadeOut()
        ) {
            FloatingActionButton(
                onClick = { // Clicked Stop
                    if (timerLength != 0L) {
                        //myViewModel.setTimerState(TimerState.Stopped, context)
                        removeAlarm(context)
                        myViewModel.stopTimer()
                        myViewModel.setTimerLength(PrefUtil.getPreviousTimerLengthSeconds(context))
                    }
                },
                backgroundColor = Color.Red
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Stop")
            }
        }
    }
}

@Composable
fun FlashingTimerText(timerText: String) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    TimerText(
        modifier = Modifier.alpha(alpha),
        timerText = timerText
    )
}

@Composable
fun TimerText(modifier: Modifier = Modifier, timerText: String) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        fontSize = 90.sp,
        style = MaterialTheme.typography.h1,
        text = timerText,
        textAlign = TextAlign.Center
    )
}


@ExperimentalAnimationApi
@Composable
fun TimeButtonsAdd(myViewModel: NoteViewModel) {
    val context = LocalContext.current
    val timerLength: Long by myViewModel.timerLengthMilli.observeAsState(initial = 0)
    val timerState: TimerState by myViewModel.timerState.observeAsState(
        PrefUtil.getTimerState(context)
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AnimatedVisibility(
            visible = timerState != TimerState.Running,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(onClick = {
                myViewModel.setTimerLength(timerLength + 60 * 60)
            }) {
                Text(text = "+")
            }
        }
        AnimatedVisibility(
            visible = timerState != TimerState.Running,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(onClick = {
                myViewModel.setTimerLength(timerLength + 60)
            }) {
                Text(text = "+")
            }
        }
        AnimatedVisibility(
            visible = timerState != TimerState.Running,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(onClick = {
                myViewModel.setTimerLength(timerLength + 1)
            }) {
                Text(text = "+")
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun TimeButtonsSub(myViewModel: NoteViewModel) {
    val context = LocalContext.current
    val timerLength: Long by myViewModel.timerLengthMilli.observeAsState(initial = 0)
    val timerState: TimerState by myViewModel.timerState.observeAsState(
        PrefUtil.getTimerState(context)
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AnimatedVisibility(
            visible = timerState != TimerState.Running,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(onClick = {
                myViewModel.setTimerLength(timerLength - 60 * 60)
            }) {
                Text(text = "-")
            }
        }
        AnimatedVisibility(
            visible = timerState != TimerState.Running,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(onClick = {
                myViewModel.setTimerLength(timerLength - 60)
            }) {
                Text(text = "-")
            }
        }
        AnimatedVisibility(
            visible = timerState != TimerState.Running,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(onClick = {
                myViewModel.setTimerLength(timerLength - 1)
            }) {
                Text(text = "-")
            }
        }
    }
}
