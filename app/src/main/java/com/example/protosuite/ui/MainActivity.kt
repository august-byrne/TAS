package com.example.protosuite.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        /*
        // Start Koin
        startKoin{
            androidLogger(Level.ERROR)
            androidContext(this@MainActivity)
            modules(notesModule)
        }
         */

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

    /** OVERRIDES HERE **/
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        //FirebaseUser currentUser = mAuth.getCurrentUser()
        //updateUI(currentUser)
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
        //stopKoin()
        super.onDestroy()
    }


}
/*
@Composable
fun MainActivityUI() {
    //val navController = rememberNavController()

    val baseTitle = "" // stringResource(id = R.string.app_name)
    val (title, setTitle) = remember { mutableStateOf(baseTitle) }

    val (canPop, setCanPop) = remember { mutableStateOf(false) }

    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    //navController.addOnDestinationChangedListener { controller, _, _ ->
    //    setCanPop(controller.previousBackStackEntry != null)
    //}

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "LayoutsCodelab")
                },
                navigationIcon = {
                    if (canPop)
                    {
                        IconButton(onClick = {
                            //navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                    else
                    {
                        IconButton(onClick = {
                            scope.launch {
                                scaffoldState.drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                },
                actions = {
                    DropdownMenu(
                        expanded = false,
                        onDismissRequest = {},
                        content = {
                            DropdownMenuItem(onClick = { }) {
                                Text(text = "Option 1")
                            }
                            DropdownMenuItem(onClick = {  }) {
                                Text(text = "Option 2")
                            }
                            DropdownMenuItem(onClick = {  }) {
                                Text(text = "Settings")
                            }
                        }
                    )
                }
            )
        },
        drawerContent = {
            Column(modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 58.dp)) {
                Text(text = "body", fontSize = 32.sp)
                Text(text = "asdf")
            }
        }//,
        //bodyContent = {

        //}
    ) { innerPadding ->
        Column {

            TextTabs()

            //AdView()
        }
    }
}

@Composable
fun TextTabs() {
    var tabIndex = 0 //by remember { mutableStateOf(0) }
    val tabData = listOf("NOTES", "TIMER", "CALENDER")
    TabRow(selectedTabIndex = tabIndex) {
        tabData.forEachIndexed { index, text ->
            Tab(selected = tabIndex == index, onClick = {
                tabIndex = index
            }, text = {
                Text(text = text)
            })
        }
    }
}

@Composable
fun AdView() {
    // Adds view to Compose
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(), // Occupy the max size in the Compose UI tree
        factory = { context ->
            var initialLayoutComplete = false
            // Creates custom view
            AdView(context).apply {
                val adWidthPixels =
                    WindowManager(context).getCurrentWindowMetrics().bounds.width().toFloat()
                val density = context.resources.displayMetrics.density
                val adWidth = (adWidthPixels.div(density)).toInt()
                //return the optimal size depends on your orientation (landscape or portrait)
                val adaptiveAdSize: AdSize =
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)

                // Sets up listeners for View -> Compose communication
                //myView.setOnClickListener {
                //selectedItem.value = 1
                //}

                //display our adaptively sized ads
                MobileAds.initialize(context) {}   //initialize the mobile ads sdk
                //val mAdView = AdView(context)  //create an adview
                //this.addView(mAdView)    //add the adview into our container for sizing/constraints

                this.viewTreeObserver.addOnGlobalLayoutListener {
                    if (!initialLayoutComplete) {
                        initialLayoutComplete = true
                        //load banner
                        this.adUnitId = context.getString(R.string.banner_ad_unit_id)
                        this.adSize = adaptiveAdSize
                        this.loadAd(AdRequest.Builder().build())
                    }
                }
            }
        },
        update = { view ->
            // View's been inflated or state read in this block has been updated
            // Add logic here if necessary

            // As selectedItem is read here, AndroidView will recompose
            // whenever the state changes
            // Example of Compose -> View communication
            //view.coordinator.selectedItem = selectedItem.value
        }
    )
}

/*
@Preview
@Composable
fun AdViewUIPreview() {
    AdView()
}
*/

@Preview
@Composable
fun MainActivityUIPreview() {
    MainActivityUI()
}
 */

