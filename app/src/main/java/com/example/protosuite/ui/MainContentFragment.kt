package com.example.protosuite.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.doOnPreDraw
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.protosuite.R
import com.example.protosuite.ui.notes.NoteListUI
import com.example.protosuite.ui.timer.TimerUI
import com.example.protosuite.ui.values.NotesTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainContentFragment : Fragment() {

    //private var _binding: ContentMainBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    //private val binding get() = _binding!!

    private lateinit var toolbarTextBoxLayout: TextInputLayout

    @ExperimentalAnimationApi
    @ExperimentalPagerApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        //val binding = DataBindingUtil.inflate<ContentMainBinding>(inflater,R.layout.content_main,container,false)
        //_binding = ContentMainBinding.inflate(inflater, container, false)
        //val view = binding.root

        toolbarTextBoxLayout = requireActivity().findViewById(R.id.note_text_box_activity_main)
        toolbarTextBoxLayout.visibility = View.GONE

        //unlock the navigation drawer
        val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        setHasOptionsMenu(true)

        return ComposeView(requireContext()).apply {
            setContent {
                NotesTheme(darkTheme = false) {
                    MainUI { directions ->
                        findNavController().navigate(directions)
                    }
                }
            }
        }

/*
        //initialize viewpager 2 adapter
        binding.viewPager.adapter = SectionsPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)

        //tab layout
        val names: ArrayList<String> = arrayListOf("Notes", "Timer", "Calender")
        TabLayoutMediator(binding.tabs, binding.viewPager){ tab, position ->
            tab.text = names[position]
        }.attach()

        //initialize fab
        binding.fab.setOnClickListener{ fabView ->
            PrefUtil.setTimerState(TimerState.Stopped, requireActivity().applicationContext)
            Snackbar.make(fabView, "Set TimerState to Stopped", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
*/
        //return view
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

        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

/*
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
 */

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.app_bar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(
            item,
            requireView().findNavController()
        ) || super.onOptionsItemSelected(item)
    }

}

@ExperimentalAnimationApi
@ExperimentalPagerApi
@Composable
fun MainUI(onNavigate: (NavDirections) -> Unit) {
    val pages = remember { listOf("Notes", "Timer", "Calender") }
    Column(Modifier.fillMaxSize()) {
        val pagerState = rememberPagerState(pageCount = pages.size)
        val composableScope = rememberCoroutineScope()
        TabRow(
            // Our selected tab is our current page
            selectedTabIndex = pagerState.currentPage,
            // Override the indicator, using the provided pagerTabIndicatorOffset modifier
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            }
        ) {
            // Add tabs for all of our pages
            pages.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        composableScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { page ->
            when (page) {
                0 -> {
                    NoteListUI(myViewModel = viewModel(), onNavigate)
                }
                1 -> {
                    TimerUI()
                }
                2 -> {
                    CalendarUI()
                }
            }
        }
    }
}

@ExperimentalPagerApi
@ExperimentalAnimationApi
@Preview
@Composable
fun TabLayoutPreview() {
    NotesTheme {
        MainUI {}
    }
}

