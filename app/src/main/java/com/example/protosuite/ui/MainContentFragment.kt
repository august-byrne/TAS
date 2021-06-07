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
import com.example.protosuite.R
import com.example.protosuite.adapters.SectionsPagerAdapter
import com.example.protosuite.databinding.ContentMainBinding
import com.example.protosuite.ui.timer.PrefUtil
import com.example.protosuite.ui.timer.TimerFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.MaterialElevationScale


class MainContentFragment : Fragment() {

    private var _binding: ContentMainBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var toolbarTextBoxLayout: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        //val binding = DataBindingUtil.inflate<ContentMainBinding>(inflater,R.layout.content_main,container,false)
        _binding = ContentMainBinding.inflate(inflater, container, false)
        val view = binding.root

        toolbarTextBoxLayout = requireActivity().findViewById(R.id.note_text_box_activity_main)
        toolbarTextBoxLayout.visibility = View.GONE

        //unlock the navigation drawer
        val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        //initialize viewpager 2 adapter
        binding.viewPager.adapter = SectionsPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)

        //tab layout
        val names: ArrayList<String> = arrayListOf("Notes", "Timer", "Calender")
        TabLayoutMediator(binding.tabs, binding.viewPager){ tab, position ->
            tab.text = names[position]
        }.attach()

        //initialize fab
        binding.fab.setOnClickListener{ fabView ->
            PrefUtil.setTimerState(TimerFragment.TimerState.Stopped, requireActivity().applicationContext)
            Snackbar.make(fabView, "Set TimerState to Stopped", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        setHasOptionsMenu(true)

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

}