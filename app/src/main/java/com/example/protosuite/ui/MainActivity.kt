package com.example.protosuite.ui

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
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.window.layout.WindowMetricsCalculator
import com.example.protosuite.R
import com.example.protosuite.data.db.entities.NoteWithItems
import com.example.protosuite.ui.notes.*
import com.example.protosuite.ui.timer.NoteTimer
import com.example.protosuite.ui.timer.PreferenceManager
import com.example.protosuite.ui.timer.TimerService
import com.example.protosuite.ui.timer.orange
import com.example.protosuite.ui.values.NotesTheme
import com.example.protosuite.ui.values.blue100
import com.example.protosuite.ui.values.blue200
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Lazy Inject ViewModel
    private val myViewModel: NoteViewModel by viewModels()

    @Inject
    lateinit var preferences: PreferenceManager

    private val adaptiveAdSize: AdSize
        get() {
            val adWidthPixels = WindowMetricsCalculator.getOrCreate()
                .computeCurrentWindowMetrics(this).bounds.width().toFloat()
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

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        //initialize the mobile ads sdk
        MobileAds.initialize(this) {}

        //Initialize ViewModel Values
        myViewModel.setPrevTimeType(preferences.lastUsedTimeUnit)
        myViewModel.sortType = preferences.sortType
        myViewModel.adState = preferences.showAds
        myViewModel.isDarkTheme = preferences.isDarkTheme

        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()

            NotesTheme(myViewModel.isDarkTheme) {
                // create a scaffold state, set it to close by default
                val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
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
                    scaffoldState = scaffoldState,
                    snackbarHost = {
                        scaffoldState.snackbarHostState
                    },
                    drawerContent = {
                        // to close use -> scaffoldState.drawerState.close()
                        Column(
                            Modifier
                                .padding(16.dp)
                                .fillMaxHeight()
                                .wrapContentWidth()
                        ) {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                modifier = Modifier.padding(vertical = 8.dp),
                                text = "What doesn't work yet",
                                style = MaterialTheme.typography.h5
                            )
                            Text(
                                modifier = Modifier.padding(vertical = 8.dp),
                                text = "* Any Drag/Drop"
                            )
                            Text(
                                modifier = Modifier.padding(vertical = 8.dp),
                                text = "* Deleting Individual Activity Items"
                            )
                            Text(
                                modifier = Modifier.padding(vertical = 8.dp),
                                text = "* audio beep plays from speakers and earbuds at the same time"
                            )
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            ItemButton(
                                modifier = Modifier.padding(vertical = 8.dp),
                                icon = Icons.Rounded.Settings,
                                text = "Settings"
                            ) {
                                coroutineScope.launch {
                                    scaffoldState.drawerState.close()
                                }
                                navController.navigate("settings")
                            }
                        }
                    },
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
                                NavGraph(myViewModel, coroutineScope, navController, scaffoldState)
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
                            if (myViewModel.adState) {
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
        preferences.lastUsedTimeUnit = myViewModel.prevTimeType
        preferences.sortType = myViewModel.sortType
        preferences.isDarkTheme = myViewModel.isDarkTheme
        super.onStop()
    }

    private fun navigateToTimerIfNeeded(intent: Intent?, navController: NavController) {
        if (intent?.action == TimerService.ACTION_SHOW_TRACKING_FRAGMENT) {
            navController.navigate("note_timer")
        }
    }
}

@Composable
fun NavGraph(myViewModel: NoteViewModel, coroutineScope: CoroutineScope, navController: NavHostController, scaffoldState: ScaffoldState) {
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
                    myViewModel.apply {
                        tempSavedNote = NoteWithItems(currentNote, currentNoteItems)
                        deleteNote(noteId)
                        noteDeleted = true
                    }
                    navController.popBackStack()
                    coroutineScope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(
                            message = "Note deleted",
                            actionLabel = " Undo",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                {
                    myViewModel.apply {
                        upsertNoteAndData(
                            currentNote.copy(
                                id = 0,
                                title = currentNote.title.plus(" - Copy"),
                                last_edited_on = Calendar.getInstance(),
                                creation_date = currentNote.creation_date
                                    ?: Calendar.getInstance()
                            ),
                            currentNoteItems.mapTo(mutableListOf()) { dataItem ->
                                dataItem.copy(id = 0)
                            }
                        )
                    }
                    navController.popBackStack()
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
            SettingsUI(myViewModel) {
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
            .background(blue100)
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

        val bgColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
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
                    style = MaterialTheme.typography.h6,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = TimerService.currentNoteItems[itemIndex].activity,
                    style = MaterialTheme.typography.subtitle1,
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
                                    //removeAlarm(context)
                                } else {
                                    // Clicked Start
                                    //setAlarm(context,timerLengthMilli)
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

@Composable
fun SortPopupUI(myViewModel: NoteViewModel) {
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = {
            myViewModel.openSortPopup = false
        },
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            shape = MaterialTheme.shapes.medium.copy(CornerSize(16.dp)),
            modifier = Modifier
                .wrapContentHeight()
                .width(IntrinsicSize.Max),
            elevation = 24.dp,
            backgroundColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(blue200)
                        .padding(8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h6,
                    text = "Sort by"
                )
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(8.dp)
                            .clickable {
                                myViewModel.sortType = SortType.Creation
                                myViewModel.openSortPopup = false
                            },
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("Creation date")
                        RadioButton(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            selected = myViewModel.sortType == SortType.Creation,
                            /*
                                colors = RadioButtonColors.radioColor(
                                    enabled = blue200,
                                    selected = yellow200
                                ),
                                 */
                            onClick = {
                                myViewModel.sortType = SortType.Creation
                                myViewModel.openSortPopup = false
                            }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(8.dp)
                            .clickable {
                                myViewModel.sortType = SortType.LastEdited
                                myViewModel.openSortPopup = false
                            },
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("Last edited")
                        RadioButton(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            selected = myViewModel.sortType == SortType.LastEdited,
                            onClick = {
                                myViewModel.sortType = SortType.LastEdited
                                myViewModel.openSortPopup = false
                            }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(8.dp)
                            .clickable {
                                myViewModel.sortType = SortType.Order
                                myViewModel.openSortPopup = false
                            },
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("Custom")
                        RadioButton(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            selected = myViewModel.sortType == SortType.Order,
                            onClick = {
                                myViewModel.sortType = SortType.Order
                                myViewModel.openSortPopup = false
                            }
                        )
                    }
                }
            }
        }
    }
}

