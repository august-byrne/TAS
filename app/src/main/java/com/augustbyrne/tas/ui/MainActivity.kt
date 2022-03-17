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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Snackbar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.augustbyrne.tas.R
import com.augustbyrne.tas.ui.notes.NoteViewModel
import com.augustbyrne.tas.ui.timer.TimerService
import com.augustbyrne.tas.ui.values.AppTheme
import com.augustbyrne.tas.ui.values.special400
import com.augustbyrne.tas.util.BatteryLevelReceiver
import com.augustbyrne.tas.util.DarkMode
import com.augustbyrne.tas.util.TimerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint

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

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //DynamicColors.applyToActivitiesIfAvailable(application)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //initialize the mobile ads sdk
        MobileAds.initialize(this) {}

        registerReceiver(batteryLevelReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // remove system insets as we will handle these ourselves with accompanist
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val adState by myViewModel.showAdsFlow.observeAsState(initial = true)
            val darkModeState by myViewModel.isDarkThemeFlow.observeAsState(initial = DarkMode.System)
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()
            val systemDarkMode = isSystemInDarkTheme()
            val isAppDark = when (darkModeState) {
                DarkMode.System -> systemDarkMode
                DarkMode.Off -> false
                DarkMode.On -> true
            }
            if (navBackStackEntry?.destination?.id == navController.findDestination("note_timer")!!.id) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
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
                            snackbarState = snackbarHostState
                        )
                        CollapsedTimer(
                            Modifier.align(Alignment.BottomCenter),
                            navController,
                            navBackStackEntry
                        )
                    }
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
                                    adSize = adaptiveAdSize
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

@Composable
fun CollapsedTimer(modifier: Modifier = Modifier, navController: NavController, navBackStackEntry: NavBackStackEntry?) {
    val timerState: TimerState by TimerService.timerState.observeAsState(TimerState.Stopped)
    if (timerState != TimerState.Stopped &&
        navBackStackEntry?.destination?.id != navController.findDestination("note_timer")!!.id &&
        navBackStackEntry?.destination?.id != navController.findDestination("settings")!!.id &&
        navBackStackEntry?.destination?.id != navController.findDestination("general_timer")!!.id
    ) {
        val timerLengthMilli: Long by TimerService.timerLengthMilli.observeAsState(1L)
        val itemIndex: Int by TimerService.itemIndex.observeAsState(0)
        val totalTimerLengthMilli: Long by TimerService.totalTimerLengthMilli.observeAsState(1L)
        val icon =
            if (timerState == TimerState.Running) Icons.Default.Pause else Icons.Default.PlayArrow
        Box(
            modifier = modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.9f))
                    .clickable(
                        onClick = { navController.navigate("note_timer") },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    ),
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
                                5
                            )
                        }",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
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
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.inverseOnSurface
                            )
                            Text(
                                text = TimerService.currentNoteItems[itemIndex].activity,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.inverseOnSurface
                            )
                        }
                        Row(
                            modifier = Modifier.wrapContentSize()
                        ) {
                            TextButton(
                                onClick = {
                                    if (timerLengthMilli > totalTimerLengthMilli - 5000L) {
                                        TimerService.modifyTimer(itemIndex - 1)
                                    } else {
                                        TimerService.modifyTimer(itemIndex)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "back to previous item",
                                    tint = MaterialTheme.colorScheme.inverseOnSurface
                                )
                            }
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
                                    tint = MaterialTheme.colorScheme.inverseOnSurface
                                )
                            }
                            TextButton(
                                onClick = {
                                    TimerService.modifyTimer(itemIndex + 1)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "skip to next item",
                                    tint = MaterialTheme.colorScheme.inverseOnSurface
                                )
                            }
                        }
                    }
                    Canvas(
                        Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        val progressLine =
                            size.width * (1f - (timerLengthMilli.toFloat() / totalTimerLengthMilli.toFloat()))
                        drawLine(
                            color = special400,
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Square,
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


