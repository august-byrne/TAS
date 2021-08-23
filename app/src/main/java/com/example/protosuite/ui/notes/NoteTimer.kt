package com.example.protosuite.ui.notes

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.protosuite.ui.BackgroundGradient
import com.example.protosuite.ui.timer.FlashingTimerText
import com.example.protosuite.ui.timer.PreferenceManager
import com.example.protosuite.ui.timer.TimerState
import com.example.protosuite.ui.timer.TimerText
import com.example.protosuite.ui.values.NotesTheme
import com.example.protosuite.ui.values.blue500
import com.example.protosuite.ui.values.yellow200

@Composable
fun DeterminateProgressBar(
    modifier: Modifier = Modifier,
    progressInMilli: Long,
    progressColor: Color = blue500,
    backgroundColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
    content: @Composable () -> Unit
) {

    val drawStyle = remember { Stroke(width = 24.dp.value, cap = StrokeCap.Round) }

    val brush = remember {
        Brush.sweepGradient(
            colorStops = arrayOf(Pair(0F, yellow200), Pair(1F, progressColor))
        )
    }

    val brushTip = remember { SolidColor(progressColor) }

    val brushBackground = remember { SolidColor(backgroundColor) }

/*    val animateCurrentProgress by animateFloatAsState(
            targetValue = progressInMilli.toFloat().div(1000F),
            animationSpec = if(progressInMilli != 0L) {
                tween(durationMillis = 1, easing = LinearEasing)
            } else {
                snap(delayMillis = 0)
            }
    )*/

    val progressDegrees = progressInMilli.toFloat().div(1000F) * PROGRESS_FULL_DEGREES

    Box {
        Canvas(
            modifier = modifier
                .aspectRatio(1f)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // Background of Progress bar
            drawArc(
                brush = brushBackground,
                startAngle = 90f,
                sweepAngle = PROGRESS_FULL_DEGREES,
                useCenter = false,
                style = drawStyle
            )

            rotate(progressDegrees + 270f, center) {
                drawArc(
                    brush = brush,
                    startAngle = PROGRESS_FULL_DEGREES - progressDegrees,
                    sweepAngle = progressDegrees,
                    useCenter = false,
                    style = drawStyle
                )
                drawArc(
                    brush = brushTip,
                    startAngle = PROGRESS_FULL_DEGREES,
                    sweepAngle = 0.014f,
                    useCenter = false,
                    style = drawStyle
                )
            }

        }
        Box(modifier = Modifier.align(Alignment.Center)) {
            content()
        }
    }
}

@Preview
@Composable
fun PreviewProgressBar() {
    NotesTheme(false) {
        //BackgroundGradient()
        DeterminateProgressBar(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            progressInMilli = 991L
        ) {
            TimerText(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp), "00:12:05")
        }
    }
}

private const val PROGRESS_FULL_DEGREES = 360f
/*
@ExperimentalAnimationApi
fun setAlarm2(context: Context, milliSecRemaining: Long) {
    val wakeUpTime = Calendar.getInstance().timeInMillis + milliSecRemaining
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, TimerBroadcastReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, FLAG_UPDATE_CURRENT)
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
    PrefUtil.setAlarmEndTime(wakeUpTime, context)
}

@ExperimentalAnimationApi
fun removeAlarm2(context: Context) {
    val intent = Intent(context, TimerBroadcastReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
    PrefUtil.setAlarmEndTime(0, context)
}*/

// play: run parsed time on clock, pause, stop alarm and leave time on clock (and save time in preferences
// stop: remove alarm and reset time to 0
// on a button press save TimerState to preferences
// on TimerState.Stopped, nothing, Paused, return value to timer, running, create time left value and start countdown timer
@ExperimentalAnimationApi
@Composable
fun NoteTimer(noteId: Int, currentItemIndex: Int, myViewModel: NoteViewModel, onNavBack: () -> Unit) {
    val context = LocalContext.current
    val noteWithItems by myViewModel.getNoteWithItemsById(
        myViewModel.activeNoteId
    ).observeAsState()
    val timerLengthMilli: Long by myViewModel.timerLengthMilli.observeAsState(1L)
    //val timerLengthMilli: Long by myViewModel.flowTime.collectAsState(1L)
    val timerState: TimerState by myViewModel.timerState.observeAsState(TimerState.Stopped)
    val itemIndex: Int by myViewModel.itemIndex.observeAsState(0)
    val totalTimerLengthMilli: Long by myViewModel.totalTimerLengthMilli.observeAsState(1L)

    val timerLengthAdjusted = if (timerState == TimerState.Stopped) {
        totalTimerLengthMilli.div(1000)
    } else {
        (timerLengthMilli.div(1000) + 1).coerceIn(0, totalTimerLengthMilli.div(1000))
    }
    val hour = timerLengthAdjusted.div(60 * 60)
    val min = timerLengthAdjusted.div(60).mod(60)
    val sec = timerLengthAdjusted.mod(60)
    val formattedTimerLength: String = String.format("%02d:%02d:%02d", hour, min, sec)

    val progressInMilli: Long = if (timerState != TimerState.Stopped) {
        1000L - (timerLengthMilli.times(1000L) / totalTimerLengthMilli)
    } else {
        0L
    }

    BackgroundGradient()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                modifier = Modifier
                    //.scale(2f)
                    .clickable(onClick = onNavBack)
                    .padding(8.dp), //padding applied in clickable, expanding clickable region
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )

            Text(
                modifier = Modifier.wrapContentWidth(),
                text = noteWithItems?.note?.title ?: "",
                //myViewModel.currentNote.title,
                style = MaterialTheme.typography.h5
            )

            Spacer(modifier = Modifier.width(40.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            DeterminateProgressBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                progressInMilli = progressInMilli
            ) {
                if (timerState == TimerState.Running && timerLengthMilli <= 5000) {
                    FlashingTimerText(formattedTimerLength)
                } else {
                    TimerText(Modifier, formattedTimerLength)
                }
            }

            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.Center
            ) {
                noteWithItems?.dataItems?.let { items ->
                    for (item in items) {
                        Icon(
                            modifier = Modifier
                                .padding(2.dp)
                                .scale(0.5f),
                            imageVector = Icons.Default.FiberManualRecord,
                            tint = if (items[itemIndex] == item) Color.Green else Color.DarkGray,
                            contentDescription = "dot"
                        )
                    }
                }
/*                    for (item in noteWithItems?.dataItems?: listOf()) {
                        Icon(
                            modifier = Modifier
                                .padding(2.dp)
                                .scale(0.5f),
                            imageVector = Icons.Default.FiberManualRecord,
                            tint = if (myViewModel.currentNoteItems[itemIndex] == item) Color.Green else Color.DarkGray,
                            contentDescription = "dot"
                        )
                    }*/
            }

            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp),
                text = noteWithItems?.dataItems?.get(itemIndex)?.activity ?: "",
                //myViewModel.currentNoteItems[itemIndex].activity,
                style = MaterialTheme.typography.h5
            )

            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    modifier = Modifier
                        .clickable {
                            if (timerLengthMilli > totalTimerLengthMilli - 5000L) {
                                myViewModel.modifyTimer(myViewModel.currentNoteItems, itemIndex - 1)
                            } else {
                                myViewModel.modifyTimer(myViewModel.currentNoteItems, itemIndex)
                            }
                        }

                        .scale(2f)
                        .padding(8.dp),
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "back to previous item")

                PlayPauseStopButtons(
                    timerState = timerState,
                    onClickStartPause = {
                        if (timerLengthMilli != 0L) {
                            if (timerState == TimerState.Running) {
                                // Clicked Pause
                                myViewModel.pauseTimer(timerLengthMilli)
                                //removeAlarm2(context)
                            } else {
                                // Clicked Start
                                if (timerState == TimerState.Stopped) {
                                    PreferenceManager(context).timeInMillis =
                                        myViewModel.timerLengthMilli.value ?: 1L
                                }
                                //temp disabled for testing
                                //setAlarm2(
                                //    context = context,
                                //    milliSecRemaining = timerLengthMilli
                                //)
                                myViewModel.startTimer(myViewModel.currentNoteItems, itemIndex)
                            }
                        }
                    },
                    onClickStop = {
                        if (timerLengthMilli != 0L) {
                            //removeAlarm2(context)
                            myViewModel.stopTimer()
                            //timerLengthMilli = PreferenceManager(context).timeInMillis
                            myViewModel.setTimerLength(PreferenceManager(context).timeInMillis)
                        }
                    }
                )

                Icon(
                    modifier = Modifier
                        .clickable {
                            myViewModel.modifyTimer(myViewModel.currentNoteItems, itemIndex + 1)
                        }
                        .scale(2f)
                        .padding(8.dp),
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "skip to next item"
                )
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun PlayPauseStopButtons(timerState: TimerState, onClickStartPause: () -> Unit, onClickStop: () -> Unit) {
    val icon =
        if (timerState == TimerState.Running) Icons.Default.Pause else Icons.Default.PlayArrow
    val tint by animateColorAsState(
        targetValue = if (timerState == TimerState.Running) Color.Yellow else Color.Green,
        animationSpec = tween(
            durationMillis = 400,
            easing = LinearEasing
        )
    )
    Row(
        modifier = Modifier.wrapContentWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Button(
            modifier = Modifier.padding(8.dp),
            onClick = onClickStartPause,
            colors = ButtonDefaults.buttonColors(backgroundColor = tint),
            shape = MaterialTheme.shapes.small.copy(CornerSize(12.dp))
        ) {
            Icon(icon, contentDescription = "Start or Pause")
        }
        AnimatedVisibility(
            visible = timerState != TimerState.Stopped,
            enter = slideInHorizontally() + fadeIn(),
            exit = slideOutHorizontally() + fadeOut()
        ) {
            Button(
                modifier = Modifier.padding(8.dp),
                onClick = onClickStop,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                shape = MaterialTheme.shapes.small.copy(CornerSize(12.dp))
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Stop")
            }
        }
    }
}
