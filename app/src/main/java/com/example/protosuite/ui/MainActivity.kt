package com.example.protosuite.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.window.WindowManager
import com.example.protosuite.R
import com.example.protosuite.databinding.ActivityMainBinding
import com.example.protosuite.ui.notes.NoteViewModel
import com.example.protosuite.ui.timer.PrefUtil
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var initialLayoutComplete = false
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var mAdView: AdView
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    // Lazy Inject ViewModel
    private val myViewModel: NoteViewModel by viewModels()

    private val adaptiveAdSize: AdSize
        get() {
            var adWidthPixels = binding.adViewContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = WindowManager(this).getCurrentWindowMetrics().bounds.width().toFloat()
            }
            val density = this.resources.displayMetrics.density
            val adWidth = (adWidthPixels.div(density)).toInt()
            //return the optimal size depends on your orientation (landscape or portrait)
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    @ExperimentalAnimationApi
    @ExperimentalPagerApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Initialize ViewModel Values
        myViewModel.setTimerState(PrefUtil.getTimerState(applicationContext), applicationContext)
        myViewModel.setTimerLength(PrefUtil.getPreviousTimerLengthSeconds(applicationContext))

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
