package com.augustbyrne.tas.ui

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NavGraph(modifier: Modifier = Modifier, viewModel: NoteViewModel, coroutineScope: CoroutineScope, navController: NavHostController, snackbarState: SnackbarHostState) {
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
                {
                    navController.navigate("note_timer")
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
                {
                    navController.navigate("note_timer")
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
                {
                    navController.navigate("note_timer")
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
