package com.augustbyrne.tas.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.augustbyrne.tas.R
import com.augustbyrne.tas.ui.notes.*
import com.augustbyrne.tas.ui.timer.NoteTimer
import com.augustbyrne.tas.ui.timer.QuickTimer
import com.augustbyrne.tas.ui.timer.TimerService
import com.augustbyrne.tas.ui.timer.orange
import com.augustbyrne.tas.ui.values.AppTheme
import com.augustbyrne.tas.ui.values.Blue40
import com.augustbyrne.tas.ui.values.Blue90
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val myViewModel: NoteViewModel by viewModels()

    private val adaptiveAdSize: AdSize
        get() {
            val adWidthPixels = this.resources.displayMetrics.widthPixels
            //removed from window API: WindowManager(this).getCurrentWindowMetrics().bounds.width().toFloat()
            //requires android API 30: windowManager.currentWindowMetrics.bounds.width().toFloat()
            val density = this.resources.displayMetrics.density
            val adWidth = (adWidthPixels.div(density)).toInt()
            //return the optimal size depends on your orientation (landscape or portrait)
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //DynamicColors.applyToActivitiesIfAvailable(application)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        //initialize the mobile ads sdk
        MobileAds.initialize(this) {}

        setContent {
            val adState by myViewModel.showAdsFlow.observeAsState(initial = true)
            val darkModeState by myViewModel.isDarkThemeFlow.observeAsState(initial = DarkMode.System)
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()
            val systemDarkMode = isSystemInDarkTheme()
            AppTheme(
                when (darkModeState) {
                    DarkMode.System -> systemDarkMode
                    DarkMode.Off -> false
                    DarkMode.On -> true
                }
            ) {
                LaunchedEffect(Unit) {
                    navigateToTimerIfNeeded(intent, navController)
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        NavGraph(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            viewModel = myViewModel,
                            coroutineScope = coroutineScope,
                            navController = navController,
                            snackbarState = snackbarHostState
                        )
                        CollapsedTimerUI(navController, navBackStackEntry)
/*                        if (navBackStackEntry?.destination?.id != navController.findDestination("note_timer")!!.id) {
                            NavBar(navController = navController, myViewModel = myViewModel)
                        }*/
                        if (adState /*&& navBackStackEntry?.destination?.id == navController.findDestination("note_timer")!!.id*/) {
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
                    DefaultSnackbar(
                        modifier = Modifier.align(Alignment.BottomCenter),
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setContent {
            val navController = rememberNavController()
            navigateToTimerIfNeeded(intent, navController)
        }
    }

    private fun navigateToTimerIfNeeded(intent: Intent?, navController: NavController) {
        if (intent?.action == TimerService.ACTION_SHOW_TRACKING_FRAGMENT) {
            navController.navigate("note_timer")
        }
    }
}

@Composable
fun NavGraph(modifier: Modifier = Modifier, viewModel: NoteViewModel, coroutineScope: CoroutineScope, navController: NavHostController, snackbarState: SnackbarHostState) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            NoteListUI(
                viewModel,
                { noteId: Int ->
                    navController.navigate("note_expanded/$noteId")
                },
                {
                    navController.navigate("note_timer")
                },
                {
                    navController.navigate("settings")
                },
                {
                    navController.navigate("general_timer")
                }
            )
        }
        composable(
            route = "note_expanded/{noteId}",
            arguments = listOf(
                navArgument("noteId") {
                    // Make argument type safe
                    type = NavType.IntType
                }
            )
        ) {
            val noteId = it.arguments?.getInt("noteId") ?: 0
            ExpandedNoteUI(
                noteId,
                viewModel,
                {
                    navController.navigate("note_timer")
                },
                {
                    navController.popBackStack()
                    coroutineScope.launch {
                        snackbarState.showSnackbar(
                            message = "Note deleted.",
                            actionLabel = " UNDO",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                {
                    navController.popBackStack()
                }
            )
        }
        composable("note_timer") {
            NoteTimer (
                {
                    navController.popBackStack()
                }, {
                    //TODO: Create Timer Theming Options
                }
            )
        }
        composable("general_timer") {
            QuickTimer(
                {
                    navController.navigate("note_timer")
                },
                {
                    navController.popBackStack()
                }
            )
        }
        composable("settings") {
            SettingsUI(viewModel) {
                navController.popBackStack()
            }
        }
    }
}

@Composable
fun CollapsedTimerUI(navController: NavController, navBackStackEntry: NavBackStackEntry?) {
    val timerState: TimerState by TimerService.timerState.observeAsState(TimerState.Stopped)
    if (timerState != TimerState.Stopped && navBackStackEntry?.destination?.id != navController.findDestination(
            "note_timer"
        )!!.id
    ) {
        val timerLengthMilli: Long by TimerService.timerLengthMilli.observeAsState(1L)
        val itemIndex: Int by TimerService.itemIndex.observeAsState(0)
        val totalTimerLengthMilli: Long by TimerService.totalTimerLengthMilli.observeAsState(1L)
        val icon =
            if (timerState == TimerState.Running) Icons.Default.Pause else Icons.Default.PlayArrow
        val bgColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(
                    onClick = { navController.navigate("note_timer") },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple()
                ),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Canvas(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
            ) {
                val progressLine =
                    size.width * (1f - (timerLengthMilli.toFloat() / totalTimerLengthMilli.toFloat()))
                drawLine(
                    color = bgColor,
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Square,
                    start = Offset(y = 2.dp.toPx(), x = 0f),
                    end = Offset(y = 2.dp.toPx(), x = size.width)
                )
                drawLine(
                    color = orange,
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Square,
                    start = Offset(y = 2.dp.toPx(), x = 0f),
                    end = Offset(
                        y = 2.dp.toPx(),
                        x = progressLine
                    )
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .wrapContentHeight()
                        .weight(1f)
                ) {
                    Text(
                        text = TimerService.currentNote.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = TimerService.currentNoteItems[itemIndex].activity,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
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
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
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
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
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
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DefaultSnackbar(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onClickUndo: () -> Unit
) {
    SnackbarHost(
        modifier = modifier,
        hostState = snackbarHostState
    ) { snackBarData ->
        Snackbar(
            modifier = Modifier.padding(8.dp),
            action = {
                snackBarData.actionLabel?.let { actionLabel ->
                    TextButton(onClick = onClickUndo) {
                        Text(
                            color = Blue40,
                            text = actionLabel
                        )
                    }
                }
            }
        ) {
            Text(
                color = Blue90,
                text = snackBarData.message
            )
        }
    }
}


