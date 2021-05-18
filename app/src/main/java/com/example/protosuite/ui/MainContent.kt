package com.example.protosuite.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.view.doOnPreDraw
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.viewpager2.widget.ViewPager2
import com.example.protosuite.R
import com.example.protosuite.adapters.SectionsPagerAdapter
import com.example.protosuite.databinding.ContentMainBinding
import com.example.protosuite.ui.timer.PrefUtil
import com.example.protosuite.ui.timer.Timer
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.MaterialElevationScale


class MainContent : Fragment() {

    private var _binding: ContentMainBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
    //private lateinit var viewPager2: ViewPager2
    private var _viewPager2: ViewPager2? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val viewPager2 get() = _viewPager2!!

    //private lateinit var mediator: TabLayoutMediator
    private var _mediator: TabLayoutMediator? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val mediator get() = _mediator!!

    //private lateinit var tabLayout: TabLayout
    private var _tabLayout: TabLayout? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val tabLayout get() = _tabLayout!!

    //private lateinit var adapter: SectionsPagerAdapter
    private var _adapter: SectionsPagerAdapter? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val adapter get() = _adapter!!

    private lateinit var toolbarTextBoxLayout: TextInputLayout

    private var shortAnimationDuration: Int = 0

    //private val mediator get() = _mediator!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        //val binding = DataBindingUtil.inflate<ContentMainBinding>(inflater,R.layout.content_main,container,false)
        _binding = ContentMainBinding.inflate(inflater, container, false)
        val view = binding.root

        toolbarTextBoxLayout = requireActivity().findViewById(R.id.note_text_box_activity_main)
        //fadeout()
        toolbarTextBoxLayout.visibility = View.GONE

        //unlock the navigation drawer
        val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)


        //val fm: FragmentManager = childFragmentManager
        //val lifecycle = viewLifecycleOwner.lifecycle
        //fragmentAdapter = FragmentAdapter(fm, lifecycle)

        //initialize viewpager 2
        _viewPager2 = binding.viewPager
        //val adapter = SectionsPagerAdapter(supportFragmentManager, lifecycle)
        _adapter = SectionsPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        viewPager2.adapter = adapter

        //tab layout created in code
        _tabLayout = binding.tabs
        val names: ArrayList<String> = arrayListOf("Notes", "Timer", "Calender")

        _mediator = TabLayoutMediator(tabLayout, viewPager2){ tab, position ->
            tab.text = names[position]
        }
        mediator.attach()

        //initialize fab
        binding.fab.setOnClickListener{ view ->
            PrefUtil.setTimerState(Timer.TimerState.Stopped, requireActivity().applicationContext)
            Snackbar.make(view, "Set TimerState to Stopped", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        setHasOptionsMenu(true)

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediator.detach()
        _mediator = null
        _viewPager2 = null
        _adapter = null
        _tabLayout = null
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.app_bar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item, requireView().findNavController())
                ||super.onOptionsItemSelected(item)
    }

    private fun fadeout() {
        toolbarTextBoxLayout.apply {
            // Set the content view to 0% opacity but visible, so that it is visible
            // (but fully transparent) during the animation.
            alpha = 1f
            visibility = View.VISIBLE

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            animate()
                .alpha(0f)
                .setDuration(resources.getInteger(R.integer.reply_motion_duration_large).toLong())
                .setListener(null)
        }
    }

}