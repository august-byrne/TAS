package com.example.protosuite.adapters

import androidx.annotation.NonNull
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.protosuite.ui.CalendarFragment
import com.example.protosuite.ui.notes.NotesFragment
import com.example.protosuite.ui.timer.TimerFragment

/**
 * A [FragmentStateAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
@ExperimentalAnimationApi
class SectionsPagerAdapter(@NonNull fragmentManager: FragmentManager, @NonNull lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle){

    private var fragments:ArrayList<Fragment> = arrayListOf(
        NotesFragment(),
        TimerFragment(),
        CalendarFragment()
    )

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}