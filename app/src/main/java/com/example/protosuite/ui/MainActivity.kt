package com.example.protosuite.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.window.layout.WindowMetricsCalculator
import com.example.protosuite.data.db.entities.NoteWithItems
import com.example.protosuite.ui.notes.ExpandedNoteUI
import com.example.protosuite.ui.notes.NoteListUI
import com.example.protosuite.ui.notes.NoteTimer
import com.example.protosuite.ui.notes.NoteViewModel
import com.example.protosuite.ui.timer.PreferenceManager
import com.example.protosuite.ui.timer.TimerState
import com.example.protosuite.ui.values.NotesTheme
import com.example.protosuite.ui.values.blue100
import com.example.protosuite.ui.values.blue200
import com.example.protosuite.ui.values.blue500
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
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
            val adWidthPixels = WindowMetricsCalculator.getOrCreate()
                .computeCurrentWindowMetrics(this).bounds.width().toFloat()
            //removed from window API: WindowManager(this).getCurrentWindowMetrics().bounds.width().toFloat()
            //requires android API 30: windowManager.currentWindowMetrics.bounds.width().toFloat()

            val density = this.resources.displayMetrics.density
            val adWidth = (adWidthPixels.div(density)).toInt()
            //return the optimal size depends on your orientation (landscape or portrait)
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    @ExperimentalAnimationApi
    @ExperimentalPagerApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initialize the mobile ads sdk
        MobileAds.initialize(this) {}

        //Initialize ViewModel Values
        //myViewModel.setPrevTimeType(PreferenceManager.lastUsedTimeUnit)
        myViewModel.setTimerState(preferences.timerState)
        myViewModel.setTimerLength(preferences.timeInMillis)
        myViewModel.activeNoteId = preferences.noteId
        myViewModel.setActiveItemIndex(preferences.itemIndex)
        myViewModel.setPrevTimeType(preferences.lastUsedTimeUnit)

        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()

            NotesTheme(darkTheme = false) {
                // create a scaffold state, set it to close by default
                val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
                // Create a coroutine scope. Opening of drawer and snackbar should happen in
                // background thread without blocking main thread
                val coroutineScope = rememberCoroutineScope()
                var expanded by remember { mutableStateOf(false) }
                val drawerEnabled by remember {
                    derivedStateOf {
                        navBackStackEntry?.destination?.id == navController.findDestination(
                            "home"
                        )!!.id
                    }
                }

/*                Log.d("navLearning", "name ${navBackStackEntry?.destination?.displayName}\n" +
                        "id ${navBackStackEntry?.destination?.id}\n" +
                        "route ${navBackStackEntry?.destination?.route}\n" +
                        "route from nav ${navController.findDestination("note_expanded/{noteId}")?.route}\n" +
                        "id from nav ${navController.findDestination("note_expanded/{noteId}")?.id}")*/
                LaunchedEffect(key1 = true) {
                    if (PreferenceManager(applicationContext).timerState == TimerState.Running) {
                        navController.navigate("note_timer")
                    }
                }
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        if (navBackStackEntry?.destination?.id != navController.findDestination(
                                "note_timer/{noteId}/{itemIndex}"
                            )?.id
                        ) {
                            TopAppBar(
                                // Provide Title
                                title = {
                                    if (navBackStackEntry?.destination?.id == navController.findDestination(
                                            "note_expanded/{noteId}"
                                        )!!.id
                                    ) {
                                        BasicTextField(
                                            modifier = Modifier
                                                .background(blue100, MaterialTheme.shapes.small)
                                                .fillMaxWidth(0.9F)
                                                .border(
                                                    border = BorderStroke(
                                                        0.5.dp,
                                                        color = Color.Black
                                                    ),
                                                    shape = MaterialTheme.shapes.small
                                                )
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            textStyle = MaterialTheme.typography.subtitle1,
                                            value = myViewModel.currentNote.title,//myViewModel.currentNoteTitle,
                                            onValueChange = { newValue ->
                                                myViewModel.beginTyping = true
                                                //myViewModel.currentNoteTitle = newValue
                                                myViewModel.currentNote =
                                                    myViewModel.currentNote.copy(title = newValue)
                                            },
                                            //placeholder = { Text("Title", color = Color.White) },
                                            singleLine = true,
                                            decorationBox = { innerTextField ->
                                                if (myViewModel.currentNote.title.isEmpty()) {
                                                    Text(
                                                        text = "Title",
                                                        color = Color.Gray,
                                                        style = MaterialTheme.typography.subtitle1
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        )
                                    } else {
                                        Text(text = "Tasky or Plan/r", color = Color.White)
                                    }
                                },
                                // Provide the drawer or the navigation Icon depending on navigation stack
                                navigationIcon = {
                                    if (navBackStackEntry?.destination?.id == navController.findDestination(
                                            "home"
                                        )!!.id
                                    ) {
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    // to close use -> scaffoldState.drawerState.close()
                                                    scaffoldState.drawerState.open()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = "Menu",
                                                tint = Color.White
                                            )
                                        }
                                    } else {
                                        IconButton(
                                            onClick = {
                                                navController.popBackStack()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowBack,
                                                contentDescription = "Back",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    if (navBackStackEntry?.destination?.id == navController.findDestination(
                                            "home"
                                        )!!.id
                                    ) {
                                        IconButton(onClick = {
                                            myViewModel.openSortPopup = true
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Sort,
                                                contentDescription = "Sort",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Menu",
                                            tint = Color.White
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        if (navBackStackEntry?.destination?.id == navController.findDestination(
                                                "note_expanded/{noteId}"
                                            )?.id
                                        ) {
                                            val noteId =
                                                navBackStackEntry!!.arguments?.getInt("noteId") ?: 0
                                            DropdownMenuItem(onClick = {
                                                myViewModel.tempSavedNote = NoteWithItems(
                                                    myViewModel.currentNote,
                                                    myViewModel.currentNoteItems
                                                )
                                                navController.popBackStack()
                                                myViewModel.deleteNote(noteId)
                                                expanded = false
                                                coroutineScope.launch {
                                                    scaffoldState.snackbarHostState.showSnackbar(
                                                        message = "Note deleted",
                                                        actionLabel = "Undo",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }) {
                                                Text("Delete")
                                            }
                                        }
                                        DropdownMenuItem(onClick = {
                                            /* Handle sort! */
                                            expanded = false
                                        }) {
                                            Text("Sort?")
                                        }
                                        DropdownMenuItem(onClick = {
                                            /* Handle settings! */
                                            expanded = false
                                        }) {
                                            Text("Settings")
                                        }
                                        Divider()
                                        DropdownMenuItem(onClick = {
                                            /* Handle donate or send feedback! */
                                            expanded = false
                                        }) {
                                            Text("Donate")
                                        }
                                    }
                                },
                                backgroundColor = if (navBackStackEntry?.destination?.id == navController.findDestination(
                                        "note_timer/{noteId}/{itemIndex}"
                                    )?.id
                                ) Color.Transparent else blue500
                            )
                        }
                    },
                    snackbarHost = {
                        scaffoldState.snackbarHostState
                    },
                    drawerContent = {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(start = 24.dp, top = 48.dp)
                        ) {
                            //Image(
                            //    painter = painterResource(R.drawable.ic_crane_drawer),
                            //    contentDescription = stringResource(R.string.cd_drawer)
                            //)
                            //for (screen in screens) {
                            Spacer(Modifier.height(24.dp))
                            Text(text = "screen", style = MaterialTheme.typography.h5)
                            //}
                        }
                    },
                    drawerGesturesEnabled = drawerEnabled
                ) {
                    Box {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            NavGraph(myViewModel, navController)
                            //TODO: AdView
/*                            AndroidView(
                                factory = { context ->
                                    AdView(context)
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                val params = ConstraintLayout.LayoutParams(
                                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                                    adaptiveAdSize.height
                                )
                                it.layoutParams = params

                                it.adUnitId = getString(R.string.google_ads_id)
                                it.adSize = adaptiveAdSize
                                it.loadAd(AdRequest.Builder().build())
                            }*/
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

    @ExperimentalAnimationApi
    override fun onDestroy() {
        //PrefUtil.setPrevTimeType(myViewModel.prevTimeType, applicationContext)
        preferences.timerState = myViewModel.timerState.value ?: TimerState.Stopped
        preferences.timeInMillis = myViewModel.totalTimerLengthMilli.value ?: 1L
        preferences.noteId = myViewModel.activeNoteId
        preferences.itemIndex = myViewModel.itemIndex.value ?: 0
        preferences.lastUsedTimeUnit = myViewModel.prevTimeType
        super.onDestroy()
    }
}

@ExperimentalPagerApi
@ExperimentalAnimationApi
@Composable
fun NavGraph(myViewModel: NoteViewModel, navController: NavHostController) {
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
                { noteId: Int ->
                    navController.navigate("note_timer/$noteId/0")
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
            ExpandedNoteUI(noteId, myViewModel) { itemIndex ->
                navController.navigate("note_timer/$noteId/$itemIndex")
            }
        }
        composable(
            route = "note_timer/{noteId}/{itemIndex}",
            arguments = listOf(
                navArgument("noteId") {
                    // Make argument type safe
                    type = NavType.IntType
                },
                navArgument("itemIndex") {
                    type = NavType.IntType
                }
            )
        ) {
            val noteId = it.arguments?.getInt("noteId") ?: 0
            val itemIndex = it.arguments?.getInt("itemIndex") ?: 0
            NoteTimer(noteId, itemIndex, myViewModel) {
                navController.popBackStack()
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
                        Text(actionLabel)
                        Icon(Icons.Default.Undo, "undo delete")
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
    if (myViewModel.openSortPopup) {
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = {
                myViewModel.openSortPopup = false
            }
        ) {
            Card(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .wrapContentSize(),
                elevation = 24.dp,
                backgroundColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        modifier = Modifier
                            .background(blue200)
                            .fillMaxWidth()
                    ) {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.h6,
                            text = "Sort by"
                        )
                    }
                    Column(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(8.dp)
                                .clickable {
                                    myViewModel.sortType = 1
                                    myViewModel.openSortPopup = false
                                },
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            RadioButton(
                                modifier = Modifier.padding(end = 8.dp),
                                selected = myViewModel.sortType == 1,
                                /*
                                colors = RadioButtonColors.radioColor(
                                    enabled = blue200,
                                    selected = yellow200
                                ),

                                 */
                                onClick = { })
                            Text("Creation date")
                        }
                        Row(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(8.dp)
                                .clickable {
                                    myViewModel.sortType = 2
                                    myViewModel.openSortPopup = false
                                },
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            RadioButton(
                                modifier = Modifier.padding(end = 8.dp),
                                selected = myViewModel.sortType == 2,
                                onClick = { })
                            Text("Last edited")
                        }
                        Row(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(8.dp)
                                .clickable {
                                    myViewModel.sortType = 3
                                    myViewModel.openSortPopup = false
                                },
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            RadioButton(
                                modifier = Modifier.padding(end = 8.dp),
                                selected = myViewModel.sortType == 3,
                                onClick = { })
                            Text("Custom")
                        }
                    }
                }
            }
        }
    }
}

