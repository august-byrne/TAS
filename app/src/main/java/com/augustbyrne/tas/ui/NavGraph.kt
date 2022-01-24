package com.augustbyrne.tas.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
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

@Composable
fun NavGraph(modifier: Modifier = Modifier, viewModel: NoteViewModel, coroutineScope: CoroutineScope, navController: NavHostController, snackbarState: SnackbarHostState) {
    val context = LocalContext.current
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
                { noteWithData ->
                    if (!noteWithData.dataItems.isNullOrEmpty()) {
                        TimerService.initTimerServiceValues(noteWithData)
                        navController.navigate("note_timer")
                        TimerService.startTimer()
                        Intent(context, TimerService::class.java).also {
                            it.action = "ACTION_START_OR_RESUME_SERVICE"
                            context.startService(it)
                        }
                    } else {
                        Toast.makeText(context, "Empty activity", Toast.LENGTH_SHORT).show()
                    }
                },
                {
                    navController.navigate("settings")
                },
                {
                    navController.navigate("general_timer")
                }
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
                viewModel,
                { noteWithData, index ->
                    if (!noteWithData.dataItems.isNullOrEmpty()) {
                        TimerService.initTimerServiceValues(noteWithData)
                        navController.navigate("note_timer")
                        TimerService.startTimer(index)
                        Intent(context, TimerService::class.java).also {
                            it.action = "ACTION_START_OR_RESUME_SERVICE"
                            context.startService(it)
                        }
                    }
                },
                {
                    navController.popBackStack()
                    coroutineScope.launch {
                        snackbarState.showSnackbar(
                            message = "Note deleted.",
                            actionLabel = " UNDO",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                {
                    navController.popBackStack()
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
                { noteWithData ->
                    TimerService.initTimerServiceValues(noteWithData)
                    navController.navigate("note_timer")
                    TimerService.startTimer()
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
