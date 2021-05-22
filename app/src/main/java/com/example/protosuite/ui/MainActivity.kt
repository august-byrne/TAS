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
import com.example.protosuite.ui.notes.notesModule
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level


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

        // Start Koin
        startKoin{
            androidLogger(Level.ERROR)
            androidContext(this@MainActivity)
            modules(notesModule)
        }

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

    //@SuppressLint("RestrictedApi")
    //override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //menuInflater.inflate(R.menu.app_bar_menu, menu)
        //if (menu is MenuBuilder) {
          //  menu.setOptionalIconsVisible(true)
        //}
        //return super.onCreateOptionsMenu(menu)
    //}

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
        stopKoin()
        super.onDestroy()
    }


}

