package com.augustbyrne.tas.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.animate
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.augustbyrne.tas.ui.components.MainBottomNavBar
import com.augustbyrne.tas.ui.notes.NoteViewModel
import com.augustbyrne.tas.ui.timer.TimerService
import com.augustbyrne.tas.ui.values.AppTheme
import com.augustbyrne.tas.ui.values.special400
import com.augustbyrne.tas.util.BarType
import com.augustbyrne.tas.util.BatteryLevelReceiver
import com.augustbyrne.tas.util.DarkMode
import com.augustbyrne.tas.util.TimerState
import com.augustbyrne.tas.util.classicSystemBarScrollBehavior
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val myViewModel: NoteViewModel by viewModels()

    private val batteryLevelReceiver = BatteryLevelReceiver()

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //DynamicColors.applyToActivitiesIfAvailable(application)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        registerReceiver(batteryLevelReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // remove system insets as we will handle these ourselves
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val darkModeState by myViewModel.isDarkThemeLiveData.observeAsState(initial = DarkMode.System)
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val snackbarHostState = remember { SnackbarHostState() }
            val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
            val coroutineScope = rememberCoroutineScope()
            val isAppDark = when (darkModeState) {
                DarkMode.System -> isSystemInDarkTheme()
                DarkMode.Off -> false
                DarkMode.On -> true
            }
            if (navBackStackEntry?.destination?.route == "note_timer") {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            val doubleBarState = rememberTopAppBarState()

            AppTheme(darkTheme = isAppDark) {
//                enableEdgeToEdge()
                // Update the status bar to be translucent
                val systemUiController = rememberSystemUiController()
                LaunchedEffect(navBackStackEntry, isAppDark) {
                    systemUiController.setStatusBarColor(
                        color = Color.Transparent,
                        darkIcons = navBackStackEntry?.destination?.route == "note_timer" || !isAppDark
                    )
                }
                LaunchedEffect(Unit) {
                    navigateToTimerIfNeeded(intent, navController)
                }
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, true)
                    ) {
                        NavGraph(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = myViewModel,
                            coroutineScope = coroutineScope,
                            navController = navController,
                            snackbarState = snackbarHostState,
                            barState = doubleBarState
                        )
                        CollapsedTimer(
                            Modifier.align(Alignment.BottomCenter),
                            myViewModel,
                            navController,
                            navBackStackEntry
                        )
                    }
                    MainBottomNavBar(
                        navBackStackEntry,
                        navController,
                        Modifier.classicSystemBarScrollBehavior(doubleBarState, BarType.Bottom)
                    )
                    DefaultSnackbar(
                        snackbarHostState = snackbarHostState,
                        onClickUndo = {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            myViewModel.apply {
                                tempSavedNote?.let {
                                    upsertNoteAndData(it.note, it.dataItems.toMutableList())
                                    tempSavedNote = null
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setContent {
            val navController = rememberNavController()
            navigateToTimerIfNeeded(intent, navController)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryLevelReceiver)
    }

    private fun navigateToTimerIfNeeded(intent: Intent?, navController: NavController) {
        if (intent?.action == TimerService.ACTION_SHOW_TRACKING_FRAGMENT) {
            navController.navigate("note_timer")
        }
    }
}

@Composable
fun CollapsedTimer(
    modifier: Modifier = Modifier,
    myViewModel: NoteViewModel,
    navController: NavController,
    navBackStackEntry: NavBackStackEntry?) {
    val coroutineScope = rememberCoroutineScope()
    val timerState: TimerState by TimerService.timerState.observeAsState(TimerState.Stopped)
    val timerLengthMilli: Long by TimerService.timerLengthMilli.observeAsState(1L)
    val totalTimerLengthMilli: Long by TimerService.totalTimerLengthMilli.observeAsState(1L)
    val timerIndicatorBG = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    if (timerState == TimerState.Stopped) {
        myViewModel.updateFabPadding(0f, 0f)
    } else if (
        navBackStackEntry?.destination?.route != "note_timer" &&
        navBackStackEntry?.destination?.route != "settings" &&
        navBackStackEntry?.destination?.route != "general_timer"
    ) {
        val itemIndex: Int by TimerService.itemIndex.observeAsState(0)
        val icon =
            if (timerState == TimerState.Running) Icons.Default.Pause else Icons.Default.PlayArrow
        var scrollOffset by remember { mutableFloatStateOf(0f) }
        var contentHeight by rememberSaveable { mutableIntStateOf(0) }
        var timerAlpha by remember { mutableFloatStateOf(1f) }
        myViewModel.updateFabPadding(contentHeight.toFloat(), scrollOffset)
        Surface(
            modifier = modifier
                .clipToBounds()
                .alpha(timerAlpha)
                .layout { measurable, constraints ->
                    // Measure the composable
                    val placeable = measurable.measure(constraints)
                    contentHeight = placeable.height
                    val placeableResizedY = placeable.height + scrollOffset.toInt()
                    val yOffset = 0
                    layout(placeable.width, placeableResizedY) {
                        // Where the composable gets placed
                        placeable.placeRelative(0, yOffset)
                    }
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            scrollOffset = (scrollOffset - dragAmount).coerceAtMost(0f)
                            myViewModel.updateFabPadding(contentHeight.toFloat(), scrollOffset)
                            timerAlpha = 1f + (scrollOffset / contentHeight.toFloat())
                        },
                        onDragEnd = {
                            if (scrollOffset < -contentHeight / 2) {
                                coroutineScope.launch {
                                    animate(
                                        initialValue = scrollOffset,
                                        targetValue = -contentHeight.toFloat()
                                    ) { value, _ ->
                                        scrollOffset = value
                                        myViewModel.updateFabPadding(contentHeight.toFloat(), value)
                                    }
                                }
                                TimerService.closeTimer()
                                //scrollOffset = 0f
                            } else {
                                coroutineScope.launch {
                                    animate(
                                        initialValue = scrollOffset,
                                        targetValue = 0f
                                    ) { value, _ ->
                                        scrollOffset = value
                                        myViewModel.updateFabPadding(contentHeight.toFloat(), value)
                                    }
                                }
                            }
                        }
                    )
                },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 3.0.dp, // Same as Bottom Navigation Bar
            onClick = { navController.navigate("note_timer") }
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                if (timerState == TimerState.Delayed) {
                    Text(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        text = "Timer starting in ${
                            (timerLengthMilli.div(1000) + 1).coerceIn(
                                0,
                                10
                            )
                        }",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .wrapContentHeight()
                                .weight(1f)
                        ) {
                            Text(
                                text = TimerService.currentNote.title,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = TimerService.currentNoteItems[itemIndex].activity,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        TextButton(
                            onClick = {
                                if (timerLengthMilli != 0L) {
                                    if (timerState == TimerState.Running) { // Clicked Pause
                                        TimerService.pauseTimer(timerLengthMilli)
                                    } else { // Clicked Start
                                        TimerService.startTimer(itemIndex)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "Start or Pause",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Canvas(
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        drawLine(
                            color = timerIndicatorBG,
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                            start = Offset(y = 0f, x = 0f),
                            end = Offset(
                                y = 0f,
                                x = size.width
                            )
                        )
                        val progressLine =
                            size.width * (1f - (timerLengthMilli.toFloat() / totalTimerLengthMilli.toFloat()))
                        drawLine(
                            color = special400,
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                            start = Offset(y = 0f, x = 0f),
                            end = Offset(
                                y = 0f,
                                x = progressLine
                            )
                        )
                    }
                }
            }
        }
    } else if (
        navBackStackEntry.destination.route == "settings" ||
        navBackStackEntry.destination.route == "general_timer"
    ) {
        Surface(
            modifier = modifier
                .height(2.dp)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 3.0.dp, // Same as Bottom Navigation Bar
        ) {
            Canvas(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                drawLine(
                    color = timerIndicatorBG,
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    start = Offset(y = 0f, x = 0f),
                    end = Offset(
                        y = 0f,
                        x = size.width
                    )
                )
                val progressLine =
                    size.width * (1f - (timerLengthMilli.toFloat() / totalTimerLengthMilli.toFloat()))
                drawLine(
                    color = special400,
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    start = Offset(y = 0f, x = 0f),
                    end = Offset(
                        y = 0f,
                        x = progressLine
                    )
                )
            }
        }
    }
}

@Composable
fun DefaultSnackbar(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    onClickUndo: () -> Unit
) {
    SnackbarHost(
        modifier = modifier,
        hostState = snackbarHostState
    ) { snackBarData ->
        Snackbar(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            action = {
                snackBarData.visuals.actionLabel?.let { actionLabel ->
                    TextButton(onClick = onClickUndo) {
                        Text(
                            color = MaterialTheme.colorScheme.primary,
                            text = actionLabel
                        )
                    }
                }
            }
        ) {
            Text(
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                text = snackBarData.visuals.message
            )
        }
    }
}


