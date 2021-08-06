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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.window.layout.WindowMetricsCalculator
import com.example.protosuite.R
import com.example.protosuite.ui.notes.ExpandedNoteUI
import com.example.protosuite.ui.notes.NoteViewModel
import com.example.protosuite.ui.timer.PrefUtil
import com.example.protosuite.ui.values.NotesTheme
import com.example.protosuite.ui.values.blue100
import com.example.protosuite.ui.values.blue200
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    //private var initialLayoutComplete = false
    //private lateinit var drawerLayout: DrawerLayout
    //private lateinit var myAdView: AdView
    //private lateinit var navController: NavController
    //private lateinit var binding: ActivityMainBinding

    // Lazy Inject ViewModel
    private val myViewModel: NoteViewModel by viewModels()

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
        myViewModel.setTimerState(PrefUtil.getTimerState(applicationContext), applicationContext)
        myViewModel.setTimerLength(PrefUtil.getPreviousTimerLengthSeconds(applicationContext))
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
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        TopAppBar(
                            // Provide Title
                            title = {
                                if (navBackStackEntry?.destination?.id != null && navBackStackEntry?.destination?.id != navController.findDestination(
                                        "home"
                                    )?.id
                                ) {
                                    BasicTextField(
                                        modifier = Modifier
                                            .background(blue100, MaterialTheme.shapes.small)
                                            .fillMaxWidth(0.9F)
                                            .border(
                                                border = BorderStroke(0.5.dp, color = Color.Black),
                                                shape = MaterialTheme.shapes.small
                                            )
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        textStyle = MaterialTheme.typography.subtitle1,
                                        value = myViewModel.currentNoteTitle,
                                        onValueChange = { newValue ->
                                            myViewModel.beginTyping = true
                                            myViewModel.currentNoteTitle = newValue
                                        },
                                        //placeholder = { Text("Title", color = Color.White) },
                                        singleLine = true,
                                        decorationBox = { innerTextField ->
                                            if (myViewModel.currentNoteTitle.isEmpty()) {
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
                                if (navBackStackEntry?.destination?.id.toString() == navController.findDestination(
                                        "home"
                                    )?.id.toString()
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
                                if (navBackStackEntry?.destination?.id.toString() == navController.findDestination(
                                        "home"
                                    )?.id.toString()
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
                                    if (navBackStackEntry?.destination?.id != null && navBackStackEntry?.destination?.id != navController.findDestination(
                                            "home"
                                        )?.id
                                    ) {
                                        val noteId =
                                            navBackStackEntry!!.arguments?.getInt("noteId") ?: 0
                                        DropdownMenuItem(onClick = {
                                            navController.popBackStack()
                                            myViewModel.deleteNote(noteId)
                                            expanded = false
                                            coroutineScope.launch {
                                                scaffoldState.snackbarHostState.showSnackbar(
                                                    message = "Note deleted",
                                                    actionLabel = "Dismiss",
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
                            }
                        )
                    },
                    snackbarHost = {
                        scaffoldState.snackbarHostState
                    },
                    drawerContent = {
                        Text("Yeah")
                    },
                ) {
                    Box {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            NavGraph(myViewModel, navController, scaffoldState, coroutineScope)
                            //TODO: AdView
                            AndroidView(
                                factory = { context ->
                                    AdView(context)
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                //val params = ConstraintLayout.LayoutParams(
                                //    ConstraintLayout.LayoutParams.MATCH_PARENT,
                                //    adaptiveAdSize.height
                                //)
                                it.adUnitId = getString(R.string.banner_ad_unit_id)
                                it.adSize = adaptiveAdSize
                                //it.layoutParams = params
                                it.loadAd(AdRequest.Builder().build())
                            }
                        }
                        DefaultSnackbar(
                            snackbarHostState = scaffoldState.snackbarHostState,
                            onDismiss = {
                                scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                            },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }
    /*
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        setSupportActionBar(binding.toolbar)
        drawerLayout = binding.drawerLayout
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        //val navController = findNavController(R.id.my_nav_host_fragment)
        setupActionBarWithNavController(navController, drawerLayout)
        setupWithNavController(binding.navView, navController)

        //display our adaptively sized ads
        MobileAds.initialize(this) {}   //initialize the mobile ads sdk
        mAdView = AdView(this)  //create an adview
        binding.adViewContainer.addView(mAdView)    //add the adview into our container for sizing/constraints

        binding.adViewContainer.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete) {
                initialLayoutComplete = true
                //load banner
                mAdView.adUnitId = getString(R.string.banner_ad_unit_id)
                mAdView.adSize = adaptiveAdSize
                mAdView.loadAd(AdRequest.Builder().build())
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        //val navHostFragment = supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment
        //val navController = navHostFragment.navController
        return navigateUp(navController, drawerLayout) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    /** Called when leaving the activity  */
    public override fun onPause() {
        mAdView.pause()
        super.onPause()
    }

    /** Called when returning to the activity  */
    public override fun onResume() {
        super.onResume()
        mAdView.resume()
    }

    /** Called before the activity is destroyed  */
    public override fun onDestroy() {
        mAdView.destroy()
        super.onDestroy()
    }

     */
}

@ExperimentalPagerApi
@ExperimentalAnimationApi
@Composable
fun NavGraph(myViewModel: NoteViewModel, navController: NavHostController, scaffoldState: ScaffoldState, coroutineScope: CoroutineScope) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            MainUI (myViewModel) { noteId: Int ->
                navController.navigate("note_expanded/$noteId")
            }
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
            ExpandedNoteUI(noteId, myViewModel)
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

/*
//typealias OnExploreItemClicked = (ExploreModel) -> Unit

enum class CraneScreen {
    Notes, Timer, Calender
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun CraneHome(
    //onExploreItemClicked: OnExploreItemClicked,
    //onDateSelectionClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.statusBarsPadding(),
        drawerContent = {
            CraneDrawer()
        },

    ) {
        val scope = rememberCoroutineScope()
        CraneHomeContent(
            modifier = modifier,
            //onExploreItemClicked = onExploreItemClicked,
            //onDateSelectionClicked = onDateSelectionClicked,
            openDrawer = {
                scope.launch {
                    scaffoldState.drawerState.open()
                }
            }
        )
    }
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun CraneHomeContent(
    //onExploreItemClicked: OnExploreItemClicked,
    //onDateSelectionClicked: () -> Unit,
    openDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    //viewModel: NoteViewModel = viewModel()
) {
    //val suggestedDestinations by viewModel.suggestedDestinations.observeAsState()

    //val onPeopleChanged: (Int) -> Unit = { viewModel.updatePeople(it) }
    //var tabSelected by remember { mutableStateOf(CraneScreen.Notes) }
    var tabSelected by remember { mutableStateOf(CraneScreen.Notes) }

    BackdropScaffold(
        modifier = modifier,
        scaffoldState = rememberBackdropScaffoldState(BackdropValue.Revealed),
        frontLayerScrimColor = Color.Transparent,
        appBar = {
            HomeTabBar(openDrawer, tabSelected, onTabSelected = { tabSelected = it })
        },
        backLayerContent = {
            //SearchContent(
            //    tabSelected,
                //viewModel,
                //onPeopleChanged,
                //onDateSelectionClicked,
                //onExploreItemClicked
            //)
        },
        frontLayerContent = {
            when (tabSelected) {
                CraneScreen.Notes -> {
                    /*
                    suggestedDestinations?.let { destinations ->
                        ExploreSection(
                            title = "Explore Flights by Destination",
                            exploreList = destinations,
                            onItemClicked = onExploreItemClicked
                        )
                    }

                     */
                    //NotesFragment()
                }
                CraneScreen.Timer -> {
                    TimerUI()
                    /*
                    ExploreSection(
                        title = "Explore Properties by Destination",
                        exploreList = viewModel.hotels,
                        onItemClicked = onExploreItemClicked
                    )

                     */
                }
                CraneScreen.Calender -> {
                    CalendarUI()
                    /*
                    ExploreSection(
                        title = "Explore Restaurants by Destination",
                        exploreList = viewModel.restaurants,
                        onItemClicked = onExploreItemClicked
                    )

                     */
                }
            }
        }
    )
}

@Composable
private fun HomeTabBar(
    openDrawer: () -> Unit,
    tabSelected: CraneScreen,
    onTabSelected: (CraneScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    CraneTabBar(
        modifier = modifier,
        onMenuClicked = openDrawer
    ) { tabBarModifier ->
        CraneTabs(
            modifier = tabBarModifier,
            titles = CraneScreen.values().map { it.name },
            tabSelected = tabSelected,
            onTabSelected = { newTab -> onTabSelected(CraneScreen.values()[newTab.ordinal]) }
        )
    }
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Preview
@Composable
fun TestMain(){
    CraneHome()
}

 */
