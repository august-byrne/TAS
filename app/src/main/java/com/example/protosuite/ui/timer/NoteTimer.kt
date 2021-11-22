package com.example.protosuite.ui.timer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.protosuite.ui.AutoSizingText
import com.example.protosuite.ui.notes.TimerState
import com.example.protosuite.ui.values.NotesTheme
import com.example.protosuite.ui.values.blue500
import com.example.protosuite.ui.values.yellow200

@Composable
fun DeterminateProgressBar(
    modifier: Modifier = Modifier,
    progressInMilli: Long,
    progressColor: Color = blue500,
    backgroundColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
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
                    sweepAngle = 0.1f,
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
            progressInMilli = 911L
        ) {
            TimerText(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp), "00:12:05")
        }
    }
}

private const val PROGRESS_FULL_DEGREES = 360f

// play: run parsed time on clock, pause, stop alarm and leave time on clock (and save time in preferences
// stop: remove alarm and reset time to 0
// on a button press save TimerState to preferences
// on TimerState.Stopped, nothing, Paused, return value to timer, running, create time left value and start countdown timer
@Composable
fun NoteTimer(onNavBack: () -> Unit) {
    val timerLengthMilli: Long by TimerService.timerLengthMilli.observeAsState(1L)
    val timerState: TimerState by TimerService.timerState.observeAsState(TimerState.Stopped)
    val itemIndex: Int by TimerService.itemIndex.observeAsState(0)
    val totalTimerLengthMilli: Long by TimerService.totalTimerLengthMilli.observeAsState(1L)

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

        CenterAlignedTopAppBar(
            title = {
                AutoSizingText(
                    modifier = Modifier.fillMaxWidth(0.9F),
                    text = TimerService.currentNote.title
                )
                    },
            navigationIcon = {
                IconButton(
                    onClick = onNavBack
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                var expanded by remember { mutableStateOf(false) }
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu"
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    content = {
                        DropdownMenuItem(onClick = {  }) {
                            Text("Timer Settings")
                        }
                    }
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.Black,
                navigationIconContentColor = Color.Black,
                actionIconContentColor = Color.Black
            )
        )

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
                    val infiniteTransition = rememberInfiniteTransition()
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 0.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400, easing = FastOutLinearInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    AutoSizingText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                            .alpha(alpha),
                        textStyle = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp),
                        text = formattedTimerLength
                    )
                    //FlashingTimerText(formattedTimerLength)
                } else {
                    AutoSizingText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        textStyle = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp),
                        text = formattedTimerLength
                    )
                    //TimerText(Modifier, formattedTimerLength)
                }
            }

            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.Center
            ) {
                for (item in TimerService.currentNoteItems) {
                    Icon(
                        modifier = Modifier
                            .padding(2.dp)
                            .scale(0.5f),
                        imageVector = Icons.Default.FiberManualRecord,
                        tint = if (TimerService.currentNoteItems[itemIndex] == item) Color.Green else Color.DarkGray,
                        contentDescription = "dot"
                    )
                }
            }

            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp),
                text = TimerService.currentNoteItems[itemIndex].activity,
                style = MaterialTheme.typography.headlineSmall
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
                                TimerService.modifyTimer(itemIndex - 1)
                            } else {
                                TimerService.modifyTimer(itemIndex)
                            }
                        }
                        .scale(1.5f)
                        .padding(8.dp),
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "back to previous item")

                PlayPauseStopButtons(
                    timerState = timerState,
                    onClickStartPause = {
                        if (timerLengthMilli != 0L) {
                            if (timerState == TimerState.Running) {
                                // Pause is Clicked
                                TimerService.pauseTimer(timerLengthMilli)
                            } else {
                                // Start is Clicked
                                TimerService.startTimer(itemIndex)
                            }
                        }
                    },
                    onClickStop = {
                        if (timerLengthMilli != 0L) {
                            TimerService.stopTimer(itemIndex)
                        }
                    }
                )

                Icon(
                    modifier = Modifier
                        .clickable {
                            TimerService.modifyTimer(itemIndex + 1)
                        }
                        .scale(1.5f)
                        .padding(8.dp),
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "skip to next item"
                )
            }
        }
    }
}

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
            colors = ButtonDefaults.buttonColors(containerColor = tint, contentColor = Color.Black),
            shape = androidx.compose.material.MaterialTheme.shapes.small.copy(CornerSize(12.dp))
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
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.Black),
                shape = androidx.compose.material.MaterialTheme.shapes.small.copy(CornerSize(12.dp))
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
        style = MaterialTheme.typography.displayLarge,
        text = timerText,
        textAlign = TextAlign.Center
    )
}
