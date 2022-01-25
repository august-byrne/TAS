package com.augustbyrne.tas.ui.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.augustbyrne.tas.data.db.entities.NoteItem
import com.augustbyrne.tas.ui.components.EditExpandedNoteHeaderDialog
import com.augustbyrne.tas.ui.components.MainNavDrawer
import com.augustbyrne.tas.ui.components.RadioItemsDialog
import com.augustbyrne.tas.ui.timer.TimerService
import com.augustbyrne.tas.ui.values.AppTheme
import com.augustbyrne.tas.util.SortType
import com.augustbyrne.tas.util.TimerState
import com.google.accompanist.insets.statusBarsPadding
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.draggedItem
import org.burnoutcrew.reorderable.rememberReorderState
import org.burnoutcrew.reorderable.reorderable
import java.time.LocalDateTime
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListUI(myViewModel: NoteViewModel, onNavigateToItem: (noteId: Int) -> Unit, onNavigateTimerStart: (noteId: Int) -> Unit, onNavSettings: () -> Unit, onNavQuickTimer: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val state = rememberReorderState()
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }
    val sortType by myViewModel.sortTypeFlow.observeAsState()
    val sortedNotes by myViewModel.sortedAllNotes(sortType).observeAsState()
    val timerState: TimerState by TimerService.timerState.observeAsState(TimerState.Stopped)

    LaunchedEffect(Unit) {
        drawerState.snapTo(DrawerValue.Closed)
    }
    MainNavDrawer(
        drawerState = drawerState,
        onNavSettings = {
            onNavSettings()
        },
        onNavTimer = {
            onNavQuickTimer()
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                // attach as a parent to the nested scroll system
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text("Timed Activity System")
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Open Navigation Drawer",
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            myViewModel.openSortPopup = true
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Sort,
                                contentDescription = "Sort"
                            )
                        }
                    }
                )
            }
        ) {
            // our list with build in nested scroll support that will notify us about its scroll
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .reorderable(
                        state = state,
                        onMove = { from, to ->
                            if (!sortedNotes.isNullOrEmpty()) {
                                Collections.swap(sortedNotes!!, from.index, to.index)
                            }
                        }, onDragEnd = { from, to ->
                            if (from >= 0 && to >= 0 && !sortedNotes.isNullOrEmpty()) {
                                myViewModel.updateAllNotes(sortedNotes!!.toMutableList())
                            }
                            if (sortType != SortType.Order) {
                                coroutineScope.launch {
                                    myViewModel.setSortType(SortType.Order)
                                }
                            }
                        }

                    ),
                state = state.listState,
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = 8.dp,
                    bottom = if (timerState != TimerState.Stopped) 160.dp else 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                coroutineScope.launch {
                    myViewModel.loadListPosition().run {
                        state.listState.scrollToItem(
                            firstVisibleItemIndex,
                            firstVisibleItemScrollOffset
                        )
                    }
                }
                itemsIndexed(sortedNotes ?: listOf()) { index, note ->
                    NoteItemUI(
                        modifier = Modifier
                            .draggedItem(state.offsetByIndex(index))
                            .detectReorderAfterLongPress(state),
                        note = note,
                        onClickItem = {
                            myViewModel.saveListPosition(state.listState)
                            onNavigateToItem(note.id)
                        }
                    ) {
                        myViewModel.saveListPosition(state.listState)
                        onNavigateTimerStart(note.id)
                    }
                }
                if (sortedNotes?.isEmpty() == true) {
                    item {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            text = "You have no routines.\nClick the + below to make one.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                myViewModel.apply {
                    if (openSortPopup) {
                        saveListPosition(state.listState)
                        RadioItemsDialog(
                            title = "Sort by",
                            radioItemNames = listOf("Creation date", "Last edited", "Custom"),
                            currentState = sortType?.type,
                            onClickItem = {
                                coroutineScope.launch {
                                    setSortType(SortType.getType(it))
                                }
                                openSortPopup = false
                            },
                            onDismissRequest = { openSortPopup = false }
                        )
                    }
                    if (openEditDialog) {
                        EditExpandedNoteHeaderDialog(
                            onDismissRequest = { openEditDialog = false }
                        ) { returnedValue ->
                            coroutineScope.launch {
                                onNavigateToItem(
                                    myViewModel.upsert(
                                        NoteItem(
                                            0,
                                            LocalDateTime.now(),
                                            LocalDateTime.now(),
                                            myViewModel.getNumberOfNotes(),
                                            returnedValue.title,
                                            returnedValue.description
                                        )
                                    ).toInt()
                                )
                                openEditDialog = false
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier.fillMaxSize().padding(
                    end = 16.dp,
                    bottom = if (timerState != TimerState.Stopped) 88.dp else 16.dp
                )
            ) {
                FloatingActionButton(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    onClick = {
                        myViewModel.saveListPosition(LazyListState())
                        myViewModel.openEditDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "New note"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NoteItemUI (
    modifier: Modifier = Modifier,
    note: NoteItem,
    onClickItem: () -> Unit,
    onClickStart: () -> Unit
    ) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClickItem,
        indication = rememberRipple(),
        shape = androidx.compose.material.MaterialTheme.shapes.medium.copy(CornerSize(12.dp)),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
                text = if (note.title.isNotBlank()) {
                    note.title
                } else {
                    "Title"
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.Top)
                        .weight(1f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    text = if (note.description.isNotBlank()) {
                        note.description
                    } else {
                        "Description"
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    modifier = Modifier.align(Alignment.Bottom),
                    onClick = onClickStart
                ) {
                    Text(
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        text = "Start",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun NoteItemUITest() {
    val note = NoteItem(
        id = 1,
        title = "This is a title that is way too long",
        description = "Description dsdf hashd fhadhf as dhasdf hasd fhddfd",
        order = 1,
        last_edited_on = LocalDateTime.now(),
        creation_date = LocalDateTime.now()
    )
    AppTheme {
        NoteItemUI(Modifier, note, {}) {}
    }
}