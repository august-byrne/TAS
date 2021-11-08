package com.example.protosuite.ui.notes

import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.data.db.entities.NoteWithItems
import com.example.protosuite.ui.MainAppBar
import com.example.protosuite.ui.SortPopupUI
import com.example.protosuite.ui.timer.TimerService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListUI(myViewModel: NoteViewModel, onNavigate: (noteId: Int) -> Unit, onNavigateTimerStart: () -> Unit, onDrawerOpen: () -> Unit, onNavSettings: () -> Unit) {
    val notes: List<NoteWithItems> by myViewModel.sortedAllNotesWithItems().observeAsState(listOf())
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }
    // TODO: Fix listState re-scrolling when rotated or when miniTimerView is clicked
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            // attach as a parent to the nested scroll system
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MainAppBar(
                myViewModel = myViewModel,
                scrollBehavior = scrollBehavior,
                onDrawerOpen = onDrawerOpen,
                onNavSettings = onNavSettings
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        onNavigate(
                            myViewModel.upsert(
                                NoteItem(0, Calendar.getInstance(), null, 0, "", "")
                            ).toInt()
                        )
                        myViewModel.saveListPosition(LazyListState())
                    }

                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "New Note"
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
            items(notes) { notesWithData ->
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
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            if (myViewModel.openSortPopup) {
                myViewModel.saveListPosition(listState)
                SortPopupUI(myViewModel)
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
    val simpleDateFormat = remember { SimpleDateFormat.getDateInstance() }
    val creationDate = simpleDateFormat.format(note.creation_date!!.time)
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClickItem,
        shape = androidx.compose.material.MaterialTheme.shapes.medium.copy(CornerSize(12.dp)),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 8.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.Top
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
                    Text(
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge,
                        text = creationDate
                    )
                }
                OutlinedButton(
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(40.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 24.dp
                    ),
                    border = BorderStroke(1.dp, Color.Green),
                    onClick = onClickStart
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        tint = Color.Green,
                        contentDescription = "Play"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        color = Color.Green,
                        text = "Start"
                    )
                }/*
                TextButton(
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp),
                    border = BorderStroke(1.dp, Color.Green),
                    shape = androidx.compose.material.MaterialTheme.shapes.small,
                    onClick = onClickStart
                ) {
                    Text(
                        color = Color.Green,
                        text = "Start"
                    )
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        tint = Color.Green,
                        contentDescription = "Play"
                    )
                }*/
            }
            Text(
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                text = if (note.description.isNotBlank()) {
                    note.description
                } else {
                    "Description"
                }
            )
        }
    }
}

@Preview
@Composable
fun NoteItemUITest() {
    val note = NoteItem(
        id = 1,
        title = "This is a title that is way too long",
        description = "Description that is reagdfsgdfg sdfgsdg sdfgsdfgsdfgs fdsdfgsdf gsdfgsdsdf hashd fhadhf as dhasdf hasd fhddfd",
        order = 1,
        last_edited_on = Calendar.getInstance(),
        creation_date = Calendar.getInstance()
    )
    NoteItemUI(note, {}) {}
}