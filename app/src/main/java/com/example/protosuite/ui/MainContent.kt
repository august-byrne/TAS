package com.example.protosuite.ui

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
import androidx.fragment.app.Fragment
import com.example.protosuite.ui.notes.NoteListUI
import com.example.protosuite.ui.notes.NoteViewModel
import com.example.protosuite.ui.timer.TimerUI
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@ExperimentalPagerApi
@Composable
fun MainUI(myViewModel: NoteViewModel, onNavigate: (noteId: Int) -> Unit, onNavigateStart: (noteId: Int) -> Unit) {
    val pages = remember { listOf("Notes", "Timer", "Calendar") }
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
                    NoteListUI(myViewModel,
                        { noteId: Int ->
                            onNavigate(noteId)
                        },
                        { noteId: Int ->
                            onNavigateStart(noteId)
                        })
                }
                1 -> {
                    TimerUI(myViewModel)
                }
                2 -> {
                    CalendarUI()
                }
            }
        }
    }
}
/*
@ExperimentalPagerApi
@ExperimentalAnimationApi
@Preview
@Composable
fun TabLayoutPreview() {
    NotesTheme {
        MainUI(navController = rememberNavController())
    }
}
 */

