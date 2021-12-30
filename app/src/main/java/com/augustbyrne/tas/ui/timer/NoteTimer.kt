package com.augustbyrne.tas.ui.timer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.augustbyrne.tas.ui.components.AutoSizingText
import com.augustbyrne.tas.ui.components.RadioItemsDialog
import com.augustbyrne.tas.ui.components.ThemedBackground
import com.augustbyrne.tas.ui.notes.NoteViewModel
import com.augustbyrne.tas.ui.values.blue500
import com.augustbyrne.tas.ui.values.orange500
import com.augustbyrne.tas.ui.values.yellow200
import com.augustbyrne.tas.util.BatteryLevelReceiver
import com.augustbyrne.tas.util.TimerState
import com.augustbyrne.tas.util.TimerTheme
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode
import kotlinx.coroutines.launch

@Composable
fun DeterminateProgressBar(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    progressInMilli: Long,
    progressColorStart: Color = yellow200,
    progressColorEnd: Color = blue500,
    backgroundColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
    content: @Composable () -> Unit
) {
    val drawStyle = remember { Stroke(width = 24.dp.value, cap = StrokeCap.Round) }
    val brush = remember {
        Brush.sweepGradient(
            colorStops = arrayOf(Pair(0F, progressColorStart), Pair(1F, progressColorEnd))
        )
    }
    val brushTip = remember { SolidColor(progressColorEnd) }
    val brushBackground = remember { SolidColor(backgroundColor) }
    val progressDegrees = progressInMilli.toFloat().div(1000F) * PROGRESS_FULL_DEGREES

    Box {
        if (enabled) {
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
        } else {
            Spacer(modifier = Modifier
                .aspectRatio(1f)
                .fillMaxSize()
                .padding(16.dp))
        }
        Box(modifier = Modifier.align(Alignment.Center)) {
            content()
        }
    }
}
private const val PROGRESS_FULL_DEGREES = 360f
@Composable
fun NoteTimer(myViewModel: NoteViewModel, onNavBack: () -> Unit, onNavTimerSettings: () -> Unit) {
    val localCoroutineScope = rememberCoroutineScope()
    var showTimerThemeDialog by rememberSaveable { mutableStateOf(false) }
    val savedTimerTheme by myViewModel.timerThemeFlow.observeAsState(initial = TimerTheme.Vibrant)
    val timerLengthMilli: Long by TimerService.timerLengthMilli.observeAsState(1L)
    val timerState: TimerState by TimerService.timerState.observeAsState(TimerState.Stopped)
    val itemIndex: Int by TimerService.itemIndex.observeAsState(0)
    val totalTimerLengthMilli: Long by TimerService.totalTimerLengthMilli.observeAsState(1L)
    val timerLengthAdjusted = if (timerState == TimerState.Stopped) {
        totalTimerLengthMilli.div(1000)
    } else {
        (timerLengthMilli.div(1000) + 1).coerceIn(0, totalTimerLengthMilli.div(1000))
    }
    val hour = timerLengthAdjusted.div(3600)
    val min = timerLengthAdjusted.div(60).mod(60)
    val sec = timerLengthAdjusted.mod(60)
    val formattedTimerLength: String = String.format("%02d:%02d:%02d", hour, min, sec)
    val progressInMilli: Long = if (timerState != TimerState.Stopped) {
        1000L - (timerLengthMilli.times(1000L) / totalTimerLengthMilli)
    } else {
        0L
    }
    val timerTheme = if (BatteryLevelReceiver.lowBattery == true) {
        TimerTheme.Original
    } else {
        savedTimerTheme
    }
    ThemedBackground(timerTheme, progressInMilli)
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
                        DropdownMenuItem(onClick = {
                            expanded = false
                            //onNavTimerSettings()
                        }) {
                            Text("Timer theme")
                        }
                    }
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = if (BatteryLevelReceiver.lowBattery == true) {
                    orange500
                } else {
                    Color.Transparent
                },
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
            if (BatteryLevelReceiver.lowBattery == true) {
                Text(style = MaterialTheme.typography.titleMedium, text = "LOW BATTERY MODE")
            }
            DeterminateProgressBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                enabled = timerTheme != TimerTheme.VaporWave && BatteryLevelReceiver.lowBattery != true,
                progressInMilli = progressInMilli
            ) {
                if (timerState == TimerState.Running && timerLengthMilli <= 5000) {
                    // Flashing Timer Text
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
                } else {
                    // Normal Resized Text
                    AutoSizingText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        textStyle = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp),
                        text = formattedTimerLength
                    )
                }
            }
            if (TimerService.currentNoteItems.size != 1) {
                FlowRow(
                    mainAxisSize = SizeMode.Expand,
                    mainAxisAlignment = FlowMainAxisAlignment.Center,
                    mainAxisSpacing = 0.dp,
                    crossAxisAlignment = FlowCrossAxisAlignment.Start,
                    crossAxisSpacing = 8.dp
                ) {
                    for (dataItemIndex in TimerService.currentNoteItems.indices) {
                        Icon(
                            modifier = Modifier
                                .wrapContentSize()
                                .scale(0.64f)
                                .clip(CircleShape)
                                .clickable(
                                    onClick = { TimerService.modifyTimer(dataItemIndex) },
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = rememberRipple()
                                )
                                .padding(4.dp),
                            imageVector = Icons.Default.Circle,
                            contentDescription = "item marker",
                            tint = if (itemIndex == dataItemIndex) Color.Green else Color.DarkGray
                        )
                    }
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
                TextButton(
                    onClick = {
                        if (timerLengthMilli > totalTimerLengthMilli - 5000L) {
                            TimerService.modifyTimer(itemIndex - 1)
                        } else {
                            TimerService.modifyTimer(itemIndex)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                ) {
                    Icon(
                        modifier = Modifier
                            .scale(1.5f)
                            .padding(8.dp),
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "back to previous item"
                    )
                }
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
                TextButton(
                    onClick = {
                        TimerService.modifyTimer(itemIndex + 1)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                ) {
                    Icon(
                        modifier = Modifier
                            .scale(1.5f)
                            .padding(8.dp),
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "skip to next item"
                    )
                }
            }
        }
        if (showTimerThemeDialog) {
            RadioItemsDialog(
                title = "Timer theme",
                radioItemNames = listOf("original", "vibrant", "vapor wave"),
                currentState = timerTheme.theme,
                onClickItem = { indexClicked ->
                    localCoroutineScope.launch {
                        myViewModel.setTimerTheme(TimerTheme.getTheme(indexClicked))
                    }
                    showTimerThemeDialog = false
                },
                onDismissRequest = {
                    showTimerThemeDialog = false
                }
            )
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
            colors = ButtonDefaults.buttonColors(containerColor = tint, contentColor = Color.Black)
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
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Stop")
            }
        }
    }
}
