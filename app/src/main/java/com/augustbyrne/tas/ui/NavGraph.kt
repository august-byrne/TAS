package com.augustbyrne.tas.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.augustbyrne.tas.ui.notes.ExpandedNoteUI
import com.augustbyrne.tas.ui.notes.NoteListUI
import com.augustbyrne.tas.ui.notes.NoteViewModel
import com.augustbyrne.tas.ui.timer.NoteTimer
import com.augustbyrne.tas.ui.timer.QuickTimer
import com.augustbyrne.tas.ui.timer.TimerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    viewModel: NoteViewModel,
    coroutineScope: CoroutineScope,
    navController: NavHostController,
    snackbarState: SnackbarHostState,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val context = LocalContext.current
    val delayedStartPrefState by viewModel.startDelayPrefLiveData.observeAsState(initial = 5)
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            NoteListUI(
                viewModel,
                { noteId: Int ->
                    navController.navigate("note_expanded/$noteId")
                },
                { noteId ->
                    coroutineScope.launch {
                        val noteWithItems = viewModel.getStaticNoteWithItemsById(noteId)
                        if (noteWithItems.dataItems.isNotEmpty()) {
                            TimerService.initTimerServiceValues(noteWithItems)
                            navController.navigate("note_timer")
                            TimerService.delayedStart(length = delayedStartPrefState)
                            Intent(context, TimerService::class.java).also {
                                it.action = "ACTION_START_OR_RESUME_SERVICE"
                                context.startService(it)
                            }
                        } else {
                            Toast.makeText(context, "Empty activity", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                scrollBehavior
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
            ExpandedNoteUI(
                noteId,
                scrollBehavior,
                viewModel,
                { noteWithItems, index ->
                    if (!noteWithItems.dataItems.isNullOrEmpty()) {
                        TimerService.initTimerServiceValues(noteWithItems)
                        navController.navigate("note_timer")
                        TimerService.delayedStart(length = delayedStartPrefState, itemIndex = index)
                        Intent(context, TimerService::class.java).also { intent ->
                            intent.action = "ACTION_START_OR_RESUME_SERVICE"
                            context.startService(intent)
                        }
                    } else {
                        Toast.makeText(context, "Empty activity", Toast.LENGTH_SHORT).show()
                    }
                },
                { noteWithItems ->
                    viewModel.tempSavedNote = noteWithItems
                    navController.popBackStack()
                    viewModel.deleteNote(noteId)
                    coroutineScope.launch {
                        snackbarState.showSnackbar(
                            message = "Note deleted.",
                            actionLabel = " UNDO",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                { noteWithItems ->
                    noteWithItems.apply {
                        if (note.title.isNotEmpty()) {
                            viewModel.upsertNoteAndData(
                                note.run {
                                    copy(
                                        id = 0,
                                        title = title.plus(" - copy"),
                                        last_edited_on = LocalDateTime.now(),
                                        creation_date = LocalDateTime.now()
                                    )
                                },
                                dataItems.mapTo(mutableListOf()) { dataItem ->
                                    dataItem.copy(id = 0)
                                }
                            )
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Add a title to clone", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                { noteWithItems ->
                    navController.popBackStack()
                    noteWithItems.apply {
                        if (note.title.isEmpty() && note.description.isEmpty() && dataItems.isNullOrEmpty()) {
                            viewModel.deleteNote(noteId)
                            Toast.makeText(context, "Removed empty note", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
        composable("note_timer") {
            NoteTimer(viewModel, {
                navController.popBackStack()
            }, {
                // Use when more NoteTimer options are needed
            }
            )
        }
        composable("general_timer") {
            QuickTimer(
                { noteWithItems ->
                    TimerService.initTimerServiceValues(noteWithItems)
                    navController.navigate("note_timer")
                    TimerService.delayedStart(length = delayedStartPrefState)
                    Intent(context, TimerService::class.java).also {
                        it.action = "ACTION_START_OR_RESUME_SERVICE"
                        context.startService(it)
                    }
                },
                {
                    navController.popBackStack()
                }
            )
        }
        composable("settings") {
            SettingsUI(viewModel) {
                navController.popBackStack()
            }
        }
    }
}
