package com.augustbyrne.tas.ui.notes

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.augustbyrne.tas.data.db.entities.DataItem
import com.augustbyrne.tas.data.db.entities.NoteItem
import com.augustbyrne.tas.data.db.entities.NoteWithItems
import com.augustbyrne.tas.ui.timer.TimerService
import com.augustbyrne.tas.ui.values.AppTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val myDateTimeFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedNoteUI (noteId: Int, myViewModel: NoteViewModel, onNavigateTimerStart: () -> Unit, onDeleteNote: () -> Unit, onNavBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val noteWithItems by myViewModel.getNoteWithItemsById(noteId)
        .observeAsState(NoteWithItems(NoteItem(), listOf()))
    val prevTimeType by myViewModel.lastUsedTimeUnitFlow.observeAsState(initial = 0)
    val listState = rememberLazyListState()
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = remember(decayAnimationSpec) {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            // attach as a parent to the nested scroll system
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            NoteExpandedTopBar(
                note = noteWithItems.note,
                scrollBehavior = scrollBehavior,
                onNavBack = {
                    onNavBack()
                    noteWithItems.apply {
                        if (note.title.isEmpty() && note.description.isEmpty() && dataItems.isNullOrEmpty()) {
                            myViewModel.deleteNote(note.id)
                            Toast.makeText(context, "Removed empty note", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onClickStart = {
                    if (!noteWithItems.dataItems.isNullOrEmpty()) {
                        TimerService.initTimerService(
                            noteWithItems.note,
                            noteWithItems.dataItems
                        )
                        onNavigateTimerStart()
                        Intent(context, TimerService::class.java).also {
                            it.action = "ACTION_START_OR_RESUME_SERVICE"
                            context.startService(it)
                        }
                    }
                },
                onDeleteNote = {
                    myViewModel.tempSavedNote = noteWithItems
                    onDeleteNote()
                    myViewModel.deleteNote(noteId)
                },
                onCloneNote = {
                    myViewModel.upsertNoteAndData(
                        noteWithItems.note.run {
                            copy(
                                id = 0,
                                title = title.plus(" - copy"),
                                last_edited_on = LocalDateTime.now(),
                                creation_date = LocalDateTime.now()
                            )
                        },
                        noteWithItems.dataItems.mapTo(mutableListOf()) { dataItem ->
                            dataItem.copy(id = 0)
                        }
                    )
                    onNavBack()
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text(text = "Add activity")
                },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "New item"
                    )
                },
                onClick = {
                    myViewModel.initialDialogDataItem = DataItem(
                        parent_id = noteId,
                        unit = prevTimeType
                    )
                }
            )
        }
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 4,
                        text = noteWithItems.note.description
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = {
                            if (!noteWithItems.dataItems.isNullOrEmpty()) {
                                TimerService.initTimerService(
                                    noteWithItems.note,
                                    noteWithItems.dataItems.shuffled()
                                )
                                onNavigateTimerStart()
                                Intent(context, TimerService::class.java).also {
                                    it.action = "ACTION_START_OR_RESUME_SERVICE"
                                    context.startService(it)
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = "shuffle play"
                            )
                        }
                        IconButton(onClick = {
                            myViewModel.openEditDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "edit title and description"
                            )
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${noteWithItems.dataItems.size} item" +
                                if (noteWithItems.dataItems.size != 1) {
                                    "s"
                                } else {
                                    ""
                                }
                    )
                    noteWithItems.note.last_edited_on?.let {
                        Text(
                            text = "last edited: ${it.format(myDateTimeFormat)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            itemsIndexed(noteWithItems.dataItems) { index: Int, item: DataItem ->
                DataItemUI(
                    dataItem = item,
                    onClickToEdit = { myViewModel.initialDialogDataItem = item },
                    onClickStart = {
                        if (!noteWithItems.dataItems.isNullOrEmpty()) {
                            TimerService.initTimerService(
                                noteWithItems.note,
                                noteWithItems.dataItems,
                                index
                            )
                            onNavigateTimerStart()
                            Intent(context, TimerService::class.java).also {
                                it.action = "ACTION_START_OR_RESUME_SERVICE"
                                context.startService(it)
                            }
                        }
                    },
                    onClickDelete = {
                        coroutineScope.launch {
                            myViewModel.deleteDataItem(item.id)
                            myViewModel.updateNote(
                                noteWithItems.note.copy(
                                    last_edited_on = LocalDateTime.now()
                                )
                            )
                        }
                    }
                )
                Divider(modifier = Modifier.padding(horizontal = 8.dp))
            }
            noteWithItems.note.creation_date?.let {
                item {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyMedium,
                        text = "created: ${it.format(myDateTimeFormat)}"
                    )
                }
            }
        }
        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.Center
        ) {
            myViewModel.apply {
                if (openEditDialog) {
                    EditExpandedNoteHeaderDialog(
                        initialValue = noteWithItems.note,
                        onDismissRequest = { openEditDialog = false }
                    ) { returnedValue ->
                        coroutineScope.launch {
                            updateNote(
                                noteWithItems.note.copy(
                                    title = returnedValue.title,
                                    description = returnedValue.description,
                                    last_edited_on = LocalDateTime.now()
                                )
                            )
                            openEditDialog = false
                        }
                    }
                }
                if (initialDialogDataItem != null) {
                    initialDialogDataItem?.let { initialDataItem ->
                        EditDataItemDialog(
                            initialDataItem = initialDataItem,
                            onDismissRequest = { initialDialogDataItem = null }
                        ) { returnedValue ->
                            initialDialogDataItem = null
                            coroutineScope.launch {
                                myViewModel.setLastUsedTimeUnit(returnedValue.unit)
                                upsertDataItem(returnedValue)
                                updateNote(
                                    noteWithItems.note.copy(
                                        last_edited_on = LocalDateTime.now()
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteExpandedTopBar(note: NoteItem, scrollBehavior: TopAppBarScrollBehavior, onNavBack: () -> Unit, onClickStart: () -> Unit, onDeleteNote: () -> Unit, onCloneNote: () -> Unit) {
    MediumTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = if (note.title.isNotEmpty()) note.title else "Add title here"
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onNavBack
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            var expanded by remember { mutableStateOf(false) }
            IconButton(onClick = onClickStart) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play"
                )
            }
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Menu"
                )
            }
            DropdownMenu(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                expanded = expanded,
                onDismissRequest = { expanded = false },
                content = {
                    DropdownMenuItem(onClick = onCloneNote) {
                        Text("Clone note")
                    }
                    DropdownMenuItem(onClick = onDeleteNote) {
                        Text("Delete")
                    }
                }
            )
        }
    )
}

@Composable
fun DataItemUI (
    dataItem: DataItem,
    onClickToEdit: () -> Unit,
    onClickStart: () -> Unit,
    onClickDelete: () -> Unit
) {
    var itemExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onClickToEdit() },
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Activity:")
            Text(text = dataItem.activity)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = "Time:")
            Text(
                text = dataItem.time.toString() +
                        when (dataItem.unit) {
                            0 -> {
                                " second"
                            }
                            1 -> {
                                " minute"
                            }
                            2 -> {
                                " hour"
                            }
                            else -> {
                                " unit"
                            }
                        } +
                        if (dataItem.time != 1) {
                            "s"
                        } else {
                            ""
                        }
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box {
            IconButton(onClick = { itemExpanded = true }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Menu"
                )
            }
            DropdownMenu(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                expanded = itemExpanded,
                onDismissRequest = { itemExpanded = false },
                content = {
                    DropdownMenuItem(
                        onClick = {
                            itemExpanded = false
                            onClickStart()
                        }
                    ) {
                        Text("Start from Here")
                    }
                    DropdownMenuItem(
                        onClick = {
                            itemExpanded = false
                            onClickDelete()
                        }
                    ) {
                        Text("Delete Item")
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun DataItemUITest() {
    val dataItem = DataItem(
        id = 1,
        activity = "Activity",
        parent_id = 1,
        order = 1,
        time = 12,
        unit = 1
    )
    AppTheme {
        DataItemUI(dataItem, {}, {}, {})
    }
}