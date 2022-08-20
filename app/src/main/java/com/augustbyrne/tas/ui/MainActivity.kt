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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Snackbar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.augustbyrne.tas.R
import com.augustbyrne.tas.ui.components.MainBottomNavBar
import com.augustbyrne.tas.ui.notes.NoteViewModel
import com.augustbyrne.tas.ui.timer.TimerService
import com.augustbyrne.tas.ui.values.AppTheme
import com.augustbyrne.tas.ui.values.special400
import com.augustbyrne.tas.util.BatteryLevelReceiver
import com.augustbyrne.tas.util.DarkMode
import com.augustbyrne.tas.util.TimerState
import com.augustbyrne.tas.util.classicSystemBarScrollBehavior
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val myViewModel: NoteViewModel by viewModels()

    private val adaptiveAdSize: AdSize
        get() {
            val adWidthPixels = this.resources.displayMetrics.widthPixels
            val density = this.resources.displayMetrics.density
            val adWidth = (adWidthPixels.div(density)).toInt()
            //return the optimal size depends on your orientation (landscape or portrait)
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    private val batteryLevelReceiver = BatteryLevelReceiver()

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //DynamicColors.applyToActivitiesIfAvailable(application)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //initialize the mobile ads sdk
        MobileAds.initialize(this) {}

        registerReceiver(batteryLevelReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // remove system insets as we will handle these ourselves
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val adState by myViewModel.showAdsLiveData.observeAsState(initial = true)
            val darkModeState by myViewModel.isDarkThemeLiveData.observeAsState(initial = DarkMode.System)
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()
            val isAppDark = when (darkModeState) {
                DarkMode.System -> isSystemInDarkTheme()
                DarkMode.Off -> false
                DarkMode.On -> true
            }
            if (navBackStackEntry?.destination?.id == navController.findDestination("note_timer")!!.id) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            val doubleBarState = rememberTopAppBarState()

            AppTheme(darkTheme = isAppDark) {
                // Update the status bar to be translucent
                val systemUiController = rememberSystemUiController()
                LaunchedEffect(navBackStackEntry, isAppDark) {
                    systemUiController.setStatusBarColor(
                        color = Color.Transparent,
                        darkIcons = navBackStackEntry?.destination?.id == navController.findDestination(
                            "note_timer"
                        )!!.id || !isAppDark
                    )
                }
                LaunchedEffect(Unit) {
                    navigateToTimerIfNeeded(intent, navController)
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
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
                        Modifier.classicSystemBarScrollBehavior(doubleBarState, false)
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
                    if (adState) {
                        AndroidView(
                            modifier = Modifier.fillMaxWidth(),
                            factory = { context ->
                                AdView(context).apply {
                                    setAdSize(adaptiveAdSize)
                                    adUnitId = context.getString(R.string.banner_ad_unit_id)
                                    loadAd(AdRequest.Builder().build())
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsedTimer(
    modifier: Modifier = Modifier,
    myViewModel: NoteViewModel,
    navController: NavController,
    navBackStackEntry: NavBackStackEntry?) {
    val coroutineScope = rememberCoroutineScope()
    val timerState: TimerState by TimerService.timerState.observeAsState(TimerState.Closed)
    if (
        timerState != TimerState.Closed &&
        navBackStackEntry?.destination?.id != navController.findDestination("note_timer")!!.id &&
        navBackStackEntry?.destination?.id != navController.findDestination("settings")!!.id &&
        navBackStackEntry?.destination?.id != navController.findDestination("general_timer")!!.id
    ) {
        val timerLengthMilli: Long by TimerService.timerLengthMilli.observeAsState(1L)
        val itemIndex: Int by TimerService.itemIndex.observeAsState(0)
        val totalTimerLengthMilli: Long by TimerService.totalTimerLengthMilli.observeAsState(1L)
        val icon =
            if (timerState == TimerState.Running) Icons.Default.Pause else Icons.Default.PlayArrow
        var scrollOffset by remember { mutableStateOf(0f) }
        var contentHeight by rememberSaveable { mutableStateOf(0) }
        var timerAlpha by remember { mutableStateOf(1f) }
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
                            color = Color.Black.copy(alpha = 0.1f),
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
                    Divider(color = MaterialTheme.colorScheme.onSurface, thickness = Dp.Hairline)
                }
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
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
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


