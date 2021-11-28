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
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScaffoldState
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
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.augustbyrne.tas.R
import com.augustbyrne.tas.ui.notes.ExpandedNoteUI
import com.augustbyrne.tas.ui.notes.NoteListUI
import com.augustbyrne.tas.ui.notes.NoteViewModel
import com.augustbyrne.tas.ui.notes.TimerState
import com.augustbyrne.tas.ui.timer.NoteTimer
import com.augustbyrne.tas.ui.timer.PreferenceManager
import com.augustbyrne.tas.ui.timer.TimerService
import com.augustbyrne.tas.ui.timer.orange
import com.augustbyrne.tas.ui.values.AppTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Lazy Inject ViewModel
    private val myViewModel: NoteViewModel by viewModels()

    @Inject
    lateinit var preferences: PreferenceManager

    private val adaptiveAdSize: AdSize
        get() {
            //val adWidthPixels = WindowMetricsCalculator.getOrCreate()
              //  .computeCurrentWindowMetrics(this).bounds.width().toFloat()
            val adWidthPixels = this.resources.displayMetrics.widthPixels
            //removed from window API: WindowManager(this).getCurrentWindowMetrics().bounds.width().toFloat()
            //requires android API 30: windowManager.currentWindowMetrics.bounds.width().toFloat()

            val density = this.resources.displayMetrics.density
            val adWidth = (adWidthPixels.div(density)).toInt()
            //return the optimal size depends on your orientation (landscape or portrait)
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //DynamicColors.applyToActivitiesIfAvailable(application)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        //initialize the mobile ads sdk
        MobileAds.initialize(this) {}

        setContent {
            val adState by preferences.showAdsFlow.collectAsState(initial = true)
            val darkModeState by preferences.isDarkThemeFlow.collectAsState(initial = false)
            val prevTimeTypeState by preferences.lastUsedTimeUnitFlow.collectAsState(initial = 0)
            myViewModel.setPrevTimeType(prevTimeTypeState)
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()

            AppTheme(darkModeState) {
                // create a scaffold state, set it to close by default
                val scaffoldStateNewTemp = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
                val scaffoldState = rememberScaffoldState(rememberDrawerState(androidx.compose.material.DrawerValue.Closed))
                // Create a coroutine scope. Opening of drawer and snackbar should happen in
                // background thread without blocking main thread
                val coroutineScope = rememberCoroutineScope()
                val drawerEnabled by remember {
                    derivedStateOf {
                        navBackStackEntry?.destination?.id == navController.findDestination(
                            "home"
                        )!!.id
                    }
                }
                LaunchedEffect(key1 = true) {
                    navigateToTimerIfNeeded(intent, navController)
                }
                Scaffold(
                    scaffoldState = scaffoldStateNewTemp,
                    //snackbarHost = {
                    //    scaffoldState.snackbarHostState
                    //},
                    drawerContent = {
                        // to close use -> scaffoldState.drawerState.close()
                        Text(
                            modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp),
                            text = "What doesn't work yet",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = "* Any Drag/Drop"
                        )
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = "* Deleting Individual Activity Items"
                        )
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = "* audio beep plays from speakers and earbuds at the same time"
                        )
                        Divider(modifier = Modifier.padding(top = 8.dp))
                        ItemButton(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            icon = Icons.Rounded.Settings,
                            text = "Settings"
                        ) {
                            coroutineScope.launch {
                                scaffoldStateNewTemp.drawerState.close()
                            }
                            navController.navigate("settings")
                        }
                    },
                    drawerContainerColor = MaterialTheme.colorScheme.background,
                    drawerGesturesEnabled = drawerEnabled
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                NavGraph(myViewModel, coroutineScope, navController, scaffoldStateNewTemp)
                            }
                            val timerState: TimerState by TimerService.timerState.observeAsState(
                                TimerState.Stopped
                            )
                            if (timerState != TimerState.Stopped && navBackStackEntry?.destination?.id != navController.findDestination(
                                    "note_timer"
                                )!!.id
                            ) {
                                CollapsedTimerUI(navController)
                            }
                            if (adState) {
                                AndroidView(
                                    modifier = Modifier.fillMaxWidth(),
                                    factory = { context ->
                                        AdView(context).apply {
                                            adSize = adaptiveAdSize // AdSize.BANNER
                                            adUnitId = context.getString(R.string.banner_ad_unit_id)
                                            loadAd(AdRequest.Builder().build())
                                        }
                                    }
                                )
                            }
                        }
                        DefaultSnackbar(
                            snackbarHostState = scaffoldState.snackbarHostState,
                            onDismiss = {
                                scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                                myViewModel.apply {
                                    tempSavedNote?.let {
                                        upsertNoteAndData(it.note, it.dataItems.toMutableList())
                                        tempSavedNote = null
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.BottomCenter)
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

    override fun onStop() {
        CoroutineScope(Dispatchers.Default).launch {
            preferences.setLastUsedTimeUnit(myViewModel.prevTimeType)
        }
        super.onStop()
    }

    private fun navigateToTimerIfNeeded(intent: Intent?, navController: NavController) {
        if (intent?.action == TimerService.ACTION_SHOW_TRACKING_FRAGMENT) {
            navController.navigate("note_timer")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(myViewModel: NoteViewModel, coroutineScope: CoroutineScope, navController: NavHostController, scaffoldState: ScaffoldState) {
    val scaffoldStateOldTemp = rememberScaffoldState(rememberDrawerState(androidx.compose.material.DrawerValue.Closed))
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            /*
            MainUI(
                myViewModel,
                { noteId: Int ->
                    navController.navigate("note_expanded/$noteId")
                },
                { noteId: Int ->
                    navController.navigate("note_timer/$noteId/0")
                })
            */
            NoteListUI(
                myViewModel,
                { noteId: Int ->
                    navController.navigate("note_expanded/$noteId")
                },
                {
                    navController.navigate("note_timer")
                },
                {
                    coroutineScope.launch {
                        scaffoldState.drawerState.open()
                    }
                },
                {
                    navController.navigate("settings")
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
                myViewModel,
                {
                    navController.navigate("note_timer")
                },
                {
                    navController.popBackStack()
                    coroutineScope.launch {
                        scaffoldStateOldTemp.snackbarHostState.showSnackbar(
                            message = "Note deleted",
                            actionLabel = " Undo",
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
            NoteTimer {
                navController.popBackStack()
            }
        }
        composable("settings") {
            SettingsUI {
                navController.popBackStack()
            }
        }
    }
}

@Composable
fun CollapsedTimerUI(navController: NavController) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable {
                navController.navigate("note_timer")
            },
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        val timerLengthMilli: Long by TimerService.timerLengthMilli.observeAsState(
            1L
        )
        val timerState: TimerState by TimerService.timerState.observeAsState(
            TimerState.Stopped
        )
        val itemIndex: Int by TimerService.itemIndex.observeAsState(0)
        val totalTimerLengthMilli: Long by TimerService.totalTimerLengthMilli.observeAsState(
            1L
        )
        val icon =
            if (timerState == TimerState.Running) Icons.Default.Pause else Icons.Default.PlayArrow

        val bgColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
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
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = TimerService.currentNoteItems[itemIndex].activity,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.wrapContentSize()
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
                        .padding(8.dp),
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "back to previous item")
                Icon(
                    modifier = Modifier
                        .clickable {
                            if (timerLengthMilli != 0L) {
                                if (timerState == TimerState.Running) {
                                    // Clicked Pause
                                    TimerService.pauseTimer(
                                        timerLengthMilli
                                    )
                                } else {
                                    // Clicked Start
                                    TimerService.startTimer(itemIndex)
                                }
                            }
                        }
                        .padding(8.dp),
                    imageVector = icon,
                    contentDescription = "Start or Pause"
                )
                Icon(
                    modifier = Modifier
                        .clickable {
                            TimerService.modifyTimer(itemIndex + 1)
                        }
                        .padding(8.dp),
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "skip to next item"
                )
            }
        }
    }
}

@Composable
fun DefaultSnackbar(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    SnackbarHost(
        modifier = modifier,
        hostState = snackbarHostState
    ) { snackBarData ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                snackBarData.actionLabel?.let { actionLabel ->
                    TextButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Undo, "undo delete")
                        Text(actionLabel)
                    }
                }
            }
        ) {
            Text(snackBarData.message)
        }
    }
}


