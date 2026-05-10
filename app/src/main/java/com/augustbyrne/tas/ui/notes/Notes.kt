package com.augustbyrne.tas.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.augustbyrne.tas.data.db.entities.NoteItem
import com.augustbyrne.tas.ui.components.AutoSizingText
import com.augustbyrne.tas.ui.components.EditExpandedNoteHeaderDialog
import com.augustbyrne.tas.ui.components.RadioItemsDialog
import com.augustbyrne.tas.ui.timer.TimerService
import com.augustbyrne.tas.util.BarType
import com.augustbyrne.tas.util.SortType
import com.augustbyrne.tas.util.TimerState
import com.augustbyrne.tas.util.classicSystemBarScrollBehavior
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListUI(
    myViewModel: NoteViewModel,
    onNavigateToItem: (noteId: Int) -> Unit,
    onNavigateTimerStart: (noteId: Int) -> Unit,
    appBarScrollState: TopAppBarState
) {
    val coroutineScope = rememberCoroutineScope()
    val sortType by myViewModel.sortTypeLiveData.observeAsState()
    val sortedNotes by myViewModel.sortedAllNotes(sortType).observeAsState()
    var draggableNotes by remember { mutableStateOf(sortedNotes) }
    val timerState: TimerState by TimerService.timerState.observeAsState(TimerState.Stopped)
    val fabPadding: Float by myViewModel.miniTimerPadding.observeAsState(0f)
    val appBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(appBarScrollState)
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            draggableNotes = draggableNotes?.toMutableList()?.apply {
                add(to.index, removeAt(from.index))
            }
        }
    )

    LaunchedEffect(Unit) {
        appBarScrollState.heightOffset = 0f
        lazyListState.scrollToItem(
            myViewModel.listPositionIndex(),
            myViewModel.listPositionOffset()
        )
    }

    LaunchedEffect(sortedNotes) {
        if (!sortedNotes.isNullOrEmpty()) {
            draggableNotes = sortedNotes
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            // attach as a parent to the nested scroll system
            .nestedScroll(appBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    AutoSizingText(text = "Timed Activity System")
                },
                modifier = Modifier
                    .statusBarsPadding()
                    .classicSystemBarScrollBehavior(appBarScrollState, BarType.Top),
                actions = {
                    IconButton(onClick = {
                        myViewModel.saveListPosition(lazyListState)
                        myViewModel.openSortPopup = true
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Sort,
                            contentDescription = "Sort"
                        )
                    }
                })
        }
    ) { statusBarPadding ->
        // our list with build in nested scroll support that will notify us about its scroll
        LazyColumn(
            modifier = Modifier
                .padding(top = statusBarPadding.calculateTopPadding())
                .fillMaxSize(),
            state = lazyListState,
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = 8.dp,
                bottom = if (timerState != TimerState.Stopped) 170.dp else 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = draggableNotes ?: listOf(),
                key = { it.id }
            ) { note ->
                ReorderableItem(
                    state = reorderableLazyListState,
                    key = note.id
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    NoteItemUI(
                        modifier = Modifier.longPressDraggableHandle(
//                            onDragStarted = {},
                            onDragStopped = {
                                if (!draggableNotes.isNullOrEmpty()) {
                                    myViewModel.updateAllNotes(draggableNotes!!.toMutableList())
                                }
                                if (sortType != SortType.Order) {
                                    coroutineScope.launch {
                                        myViewModel.setSortType(SortType.Order)
                                    }
                                }
                            },
                            interactionSource = interactionSource
                        ),
                        note = note,
                        interactionSource = interactionSource,
                        onClickItem = {
                            myViewModel.saveListPosition(lazyListState)
                            onNavigateToItem(note.id)
                        },
                        onClickStart = {
                            myViewModel.saveListPosition(lazyListState)
                            onNavigateTimerStart(note.id)
                        }
                    )
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
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    end = 16.dp,
                    bottom = with(LocalDensity.current) {
                        fabPadding.toDp()
                    } + 16.dp
                )
        ) {
            FloatingActionButton(
                modifier = Modifier.align(Alignment.BottomEnd),
                onClick = {
                    myViewModel.resetListPosition()
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

@Composable
fun NoteItemUI (
    modifier: Modifier = Modifier,
    note: NoteItem,
    interactionSource: MutableInteractionSource,
    onClickItem: () -> Unit,
    onClickStart: () -> Unit
    ) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClickItem
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                text = note.title.ifBlank {
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
                    text = note.description.ifBlank {
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

