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
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedNoteUI (noteId: Int, myViewModel: NoteViewModel, onNavigateTimerStart: () -> Unit, onDeleteNote: () -> Unit, onNavBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val noteWithItems by myViewModel.getNoteWithItemsById(noteId)
        .observeAsState(NoteWithItems(NoteItem(0, null, null, 0, "", ""), listOf()))
    val prevTimeType by myViewModel.lastUsedTimeUnitFlow.observeAsState(initial = 0)
    val listState = rememberLazyListState()
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = remember(decayAnimationSpec) {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
    }
    val dateFormatter = rememberSaveable { SimpleDateFormat.getDateTimeInstance() }

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
                            Toast.makeText(context, "Removed Empty Note", Toast.LENGTH_SHORT).show()
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
                                title = title.plus(" - Copy"),
                                last_edited_on = Calendar.getInstance(),
                                creation_date = Calendar.getInstance()
                            )
                        },
                        noteWithItems.dataItems.mapTo(mutableListOf()) { dataItem ->
                            dataItem.copy(id = 0)
                        }
                    )
                    onNavBack()
                },
                onClickTitle = {
                    myViewModel.openEditDialog = EditDialogType.Title
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text(text = "Add Activity")
                       },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "New Item",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                onClick = {
                    myViewModel.initialDialogDataItem = DataItem(
                        id = 0,
                        parent_id = noteId,
                        order = 0,
                        activity = "",
                        time = 0,
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
                DescriptionItemUI(noteWithItems.note) {
                    myViewModel.openEditDialog = EditDialogType.Description
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
                                    last_edited_on = Calendar.getInstance()
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
                            .padding(8.dp),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyMedium,
                        text = "created: ${dateFormatter.format(it.time)}"
                    )
                }
            }
        }
        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.Center
        ) {
            myViewModel.apply {
                if (openEditDialog != EditDialogType.DialogClosed) {
                    EditOneFieldDialog(
                        headerName = "Edit ${openEditDialog.name}",
                        fieldName = openEditDialog.name,
                        maxChars = if (openEditDialog == EditDialogType.Title) 26 else 100,
                        singleLine = false,
                        initialValue = if (openEditDialog == EditDialogType.Title) noteWithItems.note.title else noteWithItems.note.description,
                        onDismissRequest = { openEditDialog = EditDialogType.DialogClosed }
                    ) { returnedValue ->
                        coroutineScope.launch {
                            updateNote(
                                if (openEditDialog == EditDialogType.Title) {
                                    noteWithItems.note.copy(
                                        title = returnedValue,
                                        last_edited_on = Calendar.getInstance()
                                    )
                                } else {
                                    noteWithItems.note.copy(
                                        description = returnedValue,
                                        last_edited_on = Calendar.getInstance()
                                    )
                                }
                            )
                            openEditDialog = EditDialogType.DialogClosed
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
                                        last_edited_on = Calendar.getInstance()
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
fun NoteExpandedTopBar(note: NoteItem, scrollBehavior: TopAppBarScrollBehavior, onClickTitle: () -> Unit, onNavBack: () -> Unit, onClickStart: () -> Unit, onDeleteNote: () -> Unit, onCloneNote: () -> Unit) {
    MediumTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CornerSize(30.dp)))
                    .clickable(
                        onClick = { onClickTitle() },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = if (note.title.isNotEmpty()) note.title else "Add Title Here"
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
                        Text("Clone Note")
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

@Composable
fun DescriptionItemUI(note: NoteItem, onDescriptionClick: () -> Unit) {
    val dateFormatter = rememberSaveable { SimpleDateFormat.getDateTimeInstance() }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                onClick = { onDescriptionClick() },
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            ),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                text = "Description"
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 4,
                text = if (note.description.isNotEmpty()) note.description else "Add Description Here"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "last edited: ${
                    if (note.last_edited_on?.time != null) {
                        dateFormatter.format(note.last_edited_on.time)
                    } else {
                        "never"
                    }
                }",
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodyMedium
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