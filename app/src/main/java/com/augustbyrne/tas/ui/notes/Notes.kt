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
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.augustbyrne.tas.data.db.entities.NoteItem
import com.augustbyrne.tas.ui.MainAppBar
import com.augustbyrne.tas.ui.timer.PreferenceManager
import com.augustbyrne.tas.ui.timer.TimerService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListUI(myViewModel: NoteViewModel, onNavigate: (noteId: Int) -> Unit, onNavigateTimerStart: () -> Unit, onDrawerOpen: () -> Unit, onNavSettings: () -> Unit) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }
    val sortType by PreferenceManager(context).sortTypeFlow.collectAsState(initial = SortType.Default.ordinal)
    val sortedNotes by myViewModel.sortedAllNotesWithItems(SortType.values()[sortType]).observeAsState(initial = listOf())

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
            items(sortedNotes) { notesWithData ->
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
                        Toast.makeText(context, "Empty Activity", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            if (myViewModel.openSortPopup) {
                myViewModel.saveListPosition(listState)
                SortNotesByDialog(
                    currentSortType = SortType.values()[sortType],
                    onValueSelected = {
                        if (it != null) {
                            coroutineScope.launch {
                                PreferenceManager(context).setSortType(it.ordinal)
                            }
                        }
                        myViewModel.openSortPopup = false
                    }
                )
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        text = creationDate
                    )
                }
                FilledTonalButton(
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(40.dp),
                    contentPadding = PaddingValues(
                        start = 24.dp,
                        end = 24.dp
                    ),
                    onClick = onClickStart
                ) {
                    Text(
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        text = "Start",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
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