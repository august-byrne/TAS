package com.augustbyrne.tas.ui.notes

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.augustbyrne.tas.data.db.entities.NoteItem
import com.augustbyrne.tas.ui.MainNavDrawer
import com.augustbyrne.tas.ui.timer.TimerService
import com.augustbyrne.tas.ui.values.AppTheme
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListUI(myViewModel: NoteViewModel, onNavigate: (noteId: Int) -> Unit, onNavigateTimerStart: () -> Unit, onNavSettings: () -> Unit, onNavQuickTimer: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }
    val sortType by myViewModel.sortTypeFlow.observeAsState(initial = SortType.Default)
    val sortedNotes by myViewModel.sortedAllNotesWithItems(sortType)
        .observeAsState(initial = null)
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
            },
            floatingActionButton = {
                FloatingActionButton(
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
            },
            floatingActionButtonPosition = FabPosition.End
        ) {
            // our list with build in nested scroll support that will notify us about its scroll
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = 8.dp,
                    bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                coroutineScope.launch {
                    myViewModel.loadListPosition().run {
                        listState.scrollToItem(
                            firstVisibleItemIndex,
                            firstVisibleItemScrollOffset
                        )
                    }
                }
                items(sortedNotes ?: listOf()) { notesWithData ->
                    NoteItemUI(
                        note = notesWithData.note,
                        onClickItem = {
                            myViewModel.saveListPosition(listState)
                            onNavigate(notesWithData.note.id)
                        }
                    ) {
                        if (!notesWithData.dataItems.isNullOrEmpty()) {
                            myViewModel.saveListPosition(listState)
                            TimerService.initTimerService(
                                notesWithData.note,
                                notesWithData.dataItems
                            )
                            onNavigateTimerStart()
                            Intent(context, TimerService::class.java).also {
                                it.action = "ACTION_START_OR_RESUME_SERVICE"
                                context.startService(it)
                            }
                        } else {
                            Toast.makeText(context, "Empty activity", Toast.LENGTH_SHORT).show()
                        }
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
                        saveListPosition(listState)
                        SortNotesByDialog(
                            currentSortType = sortType,
                            onValueSelected = {
                                if (it != null) {
                                    coroutineScope.launch {
                                        setSortType(it)
                                    }
                                }
                                openSortPopup = false
                            }
                        )
                    }
                    if (openEditDialog) {
                        EditExpandedNoteHeaderDialog(
                            onDismissRequest = { openEditDialog = false }
                        ) { returnedValue ->
                            coroutineScope.launch {
                                onNavigate(
                                    myViewModel.upsert(
                                        NoteItem(
                                            0,
                                            Calendar.getInstance(),
                                            null,
                                            0,
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
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NoteItemUI (
    note: NoteItem,
    onClickItem: () -> Unit,
    onClickStart: () -> Unit
    ) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClickItem,
        indication = rememberRipple(),
        shape = androidx.compose.material.MaterialTheme.shapes.medium.copy(CornerSize(12.dp)),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        elevation = 1.dp
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
        last_edited_on = Calendar.getInstance(),
        creation_date = Calendar.getInstance()
    )
    AppTheme {
        NoteItemUI(note, {}) {}
    }
}