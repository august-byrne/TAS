package com.augustbyrne.tas.ui.timer

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.augustbyrne.tas.ui.components.AutoSizingText
import com.augustbyrne.tas.ui.components.RadioItemsDialog
import com.augustbyrne.tas.ui.notes.NoteViewModel
import com.augustbyrne.tas.ui.values.blue500
import com.augustbyrne.tas.ui.values.orange500
import com.augustbyrne.tas.ui.values.yellow200
import com.augustbyrne.tas.util.BatteryLevelReceiver
import com.augustbyrne.tas.util.TimerState
import com.augustbyrne.tas.util.TimerTheme
import kotlinx.coroutines.launch

@Composable
fun CircleProgressBar(
    modifier: Modifier = Modifier,
    timerState: TimerState,
    timerLengthMilli: Long,
    totalTimerLengthMilli: Long,
    progressPercent: Float,
    progressColorStart: Color = yellow200,
    progressColorEnd: Color = blue500,
    backgroundColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
) {
    val drawStyle = remember { Stroke(width = 24.dp.value, cap = StrokeCap.Round) }
    val brush = remember {
        Brush.sweepGradient(
            colorStops = arrayOf(Pair(0F, progressColorStart), Pair(1F, progressColorEnd))
        )
    }
    val brushTip = remember { SolidColor(progressColorEnd) }
    val brushBackground = remember { SolidColor(backgroundColor) }
    val progressDegrees = progressPercent.times(360f)

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
                sweepAngle = 360f,
                useCenter = false,
                style = drawStyle
            )

            rotate(progressDegrees + 270f, center) {
                drawArc(
                    brush = brush,
                    startAngle = 360f - progressDegrees,
                    sweepAngle = progressDegrees,
                    useCenter = false,
                    style = drawStyle
                )
                drawArc(
                    brush = brushTip,
                    startAngle = 360f,
                    sweepAngle = 0.1f,
                    useCenter = false,
                    style = drawStyle
                )
            }
        }
        Box(modifier = Modifier.align(Alignment.Center)) {
            TimerText(
                timerState = timerState,
                timerLengthMilli = timerLengthMilli,
                totalTimerLengthMilli = totalTimerLengthMilli
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteTimer(myViewModel: NoteViewModel, onNavBack: () -> Unit, onNavTimerSettings: () -> Unit) {
    val context = LocalContext.current
    val localCoroutineScope = rememberCoroutineScope()
    var showTimerThemeDialog by rememberSaveable { mutableStateOf(false) }
    val savedTimerTheme by myViewModel.timerThemeLiveData.observeAsState()
    val timerLengthMilli: Long by TimerService.timerLengthMilli.observeAsState(1L)
    val timerState: TimerState by TimerService.timerState.observeAsState(TimerState.Stopped)
    val itemIndex: Int by TimerService.itemIndex.observeAsState(0)
    val totalTimerLengthMilli: Long by TimerService.totalTimerLengthMilli.observeAsState(1L)
    val progressPercent: Float = 1f - timerLengthMilli.div(totalTimerLengthMilli.toFloat())
    val startDelayState by myViewModel.startDelayPrefLiveData.observeAsState(initial = 5)
    val timerTheme = if (BatteryLevelReceiver.lowBattery == true) {
        TimerTheme.Original
    } else {
        savedTimerTheme
    }
    val delayedStartPrefState by myViewModel.startDelayPrefLiveData.observeAsState(initial = 5)
    val icon =
        if (timerState == TimerState.Running) Icons.Default.Pause else Icons.Default.PlayArrow
    val iconTint by animateColorAsState(
        targetValue = if (timerState == TimerState.Running) Color.Yellow else Color.Green,
        animationSpec = tween(
            durationMillis = 200,
            easing = LinearEasing
        ), label = "timer_icon_tint"
    )
    LaunchedEffect(Unit) {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val batteryPct: Float? = batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toFloat()
        }
        BatteryLevelReceiver.lowBattery = batteryPct != null && batteryPct <= 15
    }
    ThemedBackground(
        timerTheme = timerTheme,
        startingDelay = startDelayState.toLong(),
        timerUI = {
            if (BatteryLevelReceiver.lowBattery == true) {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    text = "LOW BATTERY MODE"
                )
                TimerText(
                    timerState = timerState,
                    timerLengthMilli = timerLengthMilli,
                    totalTimerLengthMilli = totalTimerLengthMilli
                )
            } else {
                CircleProgressBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    progressPercent = progressPercent,
                    timerState = timerState,
                    timerLengthMilli = timerLengthMilli,
                    totalTimerLengthMilli = totalTimerLengthMilli
                )
            }
        },
        contentUI = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.White.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .weight(weight = 1f, fill = true)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    if (TimerService.currentNoteItems.size != 1) {
                        FlowRow(
                            horizontalArrangement = Arrangement.Center
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
                                            indication = ripple()
                                        )
                                        .padding(4.dp),
                                    imageVector = Icons.Default.Circle,
                                    contentDescription = "item marker",
                                    tint = if (itemIndex == dataItemIndex) Color.Green else Color.DarkGray
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (TimerService.currentNoteItems[itemIndex].activity.isNotBlank()) {
                            Spacer(Modifier.width(58.dp))
                            Text(
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .weight(weight = 1f, fill = true),
                                text = TimerService.currentNoteItems[itemIndex].activity,
                                maxLines = 2,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        TextButton(
                            onClick = {
                                TimerService.modifyTimer(itemIndex)
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                        ) {
                            Icon(
                                modifier = Modifier.scale(1.5f),
                                imageVector = Icons.Default.Replay,
                                contentDescription = "restart current item"
                            )
                        }
                    }
                    if (itemIndex + 1 <= TimerService.currentNoteItems.lastIndex) {
                        Text(
                            text = "Next: ${TimerService.currentNoteItems[itemIndex + 1].activity}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge
                        )
                    } else if (itemIndex == TimerService.currentNoteItems.lastIndex) {
                        Text(
                            text = "Final Item",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge
                        )
                    } else {
                        Text(
                            text = "",
                            maxLines = 1,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(bottom = 32.dp, top = 8.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            TimerService.modifyTimer(itemIndex - 1)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                    ) {
                        Icon(
                            modifier = Modifier.scale(1.75f),
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "back to previous item"
                        )
                    }
                    FloatingActionButton(
                        onClick = {
                            if (timerState == TimerState.Running) { // Pause is Clicked
                                TimerService.pauseTimer(timerLengthMilli)
                            } else { // Start is Clicked
                                if (timerState == TimerState.Stopped) {
                                    TimerService.delayedStart(
                                        length = delayedStartPrefState,
                                        itemIndex = itemIndex
                                    )
                                } else {
                                    TimerService.startTimer(itemIndex)
                                }
                            }
                        },
                        containerColor = iconTint,
                        contentColor = Color.Black
                    ) {
                        Icon(
                            modifier = Modifier.scale(1.5f),
                            imageVector = icon,
                            contentDescription = "Start or Pause"
                        )
                    }
                    TextButton(
                        onClick = {
                            TimerService.modifyTimer(itemIndex + 1)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                    ) {
                        Icon(
                            modifier = Modifier.scale(1.75f),
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "skip to next item"
                        )
                    }
                }
            }
        }
    )
    Box(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            modifier = Modifier
                .statusBarsPadding()
                .align(Alignment.TopCenter),
            title = {
                AutoSizingText(
                    modifier = Modifier.fillMaxWidth(0.9F),
                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                    text = TimerService.currentNote.title
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavBack
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        tint = Color.Black,
                        contentDescription = "minimize"
                    )
                }
            },
            actions = {
                var expanded by remember { mutableStateOf(false) }
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        tint = Color.Black,
                        contentDescription = "Menu"
                    )
                }
                DropdownMenu(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    content = {
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                showTimerThemeDialog = true
                                //onNavTimerSettings()
                            },
                            text = {
                                Text(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    text = "Timer theme"
                                )
                            }
                        )
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
        if (showTimerThemeDialog) {
            RadioItemsDialog(
                title = "Timer theme",
                radioItemNames = listOf("basic", "vibrant", "vaporwave"),
                currentState = timerTheme?.theme,
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
fun TimerText(
    modifier: Modifier = Modifier,
    timerState: TimerState,
    timerLengthMilli: Long,
    totalTimerLengthMilli: Long
) {
    val timerLengthAdjusted = if (timerState == TimerState.Stopped) {
        totalTimerLengthMilli.div(1000)
    } else {
        (timerLengthMilli.div(1000) + 1).coerceIn(0, totalTimerLengthMilli.div(1000))
    }
    val hour = timerLengthAdjusted.div(3600).toInt()
    val min = timerLengthAdjusted.div(60).mod(60)
    val sec = timerLengthAdjusted.mod(60)
    val formattedTimerLength: String = String.format("%02d:%02d:%02d", hour, min, sec)
    Box(modifier = modifier) {
        if (timerState == TimerState.Running && timerLengthAdjusted <= 5) {
            // Flashing Timer Text
            val infiniteTransition = rememberInfiniteTransition(label = "timer_transition")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 0.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = FastOutLinearInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "timer_text_alpha"
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
}
