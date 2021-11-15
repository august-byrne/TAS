package com.example.protosuite.ui.notes

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.protosuite.data.db.entities.DataItem
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.data.db.entities.NoteWithItems
import com.example.protosuite.ui.timer.TimerService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedNoteUI (noteId: Int, myViewModel: NoteViewModel, onNavigateTimerStart: () -> Unit, onDeleteNote: () -> Unit, onNavBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val noteWithItems by myViewModel.getNoteWithItemsById(noteId).observeAsState(NoteWithItems(NoteItem(0, null, null, 0, "", ""), listOf()))
    val listState = rememberLazyListState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }
    if (!myViewModel.beginTyping) {
        //myViewModel.currentNote = noteWithItems.note
        myViewModel.currentNoteItems = noteWithItems.dataItems.toMutableStateList()
    }
    DisposableEffect(key1 = myViewModel) {
        onDispose {
            myViewModel.apply {
                if (noteWithItems.note.title.isEmpty() && noteWithItems.note.description.isEmpty() && currentNoteItems.isNullOrEmpty()) {
                    deleteNote(noteWithItems.note.id)
                    Toast.makeText(context, "Removed Empty Note", Toast.LENGTH_SHORT).show()
                } else {
                    if (!noteDeleted) {
                        if (currentNoteItems.toList() != noteWithItems.dataItems) {
                            upsertNoteAndData(
                                noteWithItems.note.copy(
                                    last_edited_on = Calendar.getInstance()
                                ),
                                currentNoteItems
                            )
                        }
                        beginTyping = false
                    } else {
                        noteDeleted = false
                    }
                }
            }
        }
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
                onNavBack = onNavBack,
                onDeleteNote = {
                    myViewModel.apply {
                        tempSavedNote = NoteWithItems(noteWithItems.note, currentNoteItems)
                        deleteNote(noteId)
                        noteDeleted = true
                    }
                    onDeleteNote()
                },
                onCloneNote = {
                    myViewModel.upsertNoteAndData(
                        noteWithItems.note.run {
                            copy(
                                title = title.plus(" - Copy"),
                                last_edited_on = Calendar.getInstance(),
                                creation_date = creation_date
                                    ?: Calendar.getInstance()
                            )
                        },
                        myViewModel.currentNoteItems.mapTo(mutableListOf()) { dataItem ->
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
            FloatingActionButton(
                onClick = {
                    myViewModel.beginTyping = true
                    myViewModel.currentNoteItems.add(
                        DataItem(
                            0,
                            noteId,
                            0,
                            "",
                            0,
                            myViewModel.prevTimeType
                        )
                    )
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "New Item",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
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
            itemsIndexed(myViewModel.currentNoteItems) { index: Int, item: DataItem ->
                DataItemUI(
                    dataItem = item,
                    onDataItemChanged = { dataItem ->
                        myViewModel.beginTyping = true
                        if (myViewModel.currentNoteItems[index].unit != dataItem.unit) {
                            myViewModel.setPrevTimeType(dataItem.unit)
                        }
                        myViewModel.currentNoteItems[index] = dataItem
                    },
                    onClickStart = {
                        if (!myViewModel.currentNoteItems.isNullOrEmpty()) {
                            TimerService.initTimerService(
                                noteWithItems.note,
                                myViewModel.currentNoteItems,
                                index
                            )
                            onNavigateTimerStart()
                            Intent(context, TimerService::class.java).also {
                                it.action = "ACTION_START_OR_RESUME_SERVICE"
                                context.startService(it)
                            }
                        }
                    }
                )
                Divider(modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            myViewModel.apply {
                if (openEditDialog != EditDialogType.DialogClosed) {
                    EditOneFieldDialog(
                        headerName = "Edit ${openEditDialog.name}",
                        fieldName = openEditDialog.name,
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
            }
        }
    }
}

@Composable
fun NoteExpandedTopBar(note: NoteItem, scrollBehavior: TopAppBarScrollBehavior, onClickTitle: () -> Unit, onNavBack: () -> Unit, onDeleteNote: () -> Unit, onCloneNote: () -> Unit) {
    SmallTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                modifier = Modifier.clickable { onClickTitle() },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                text = if (note.title.isNotEmpty()) note.title else "Title"
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
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Menu"
                )
            }
            DropdownMenu(
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
    onDataItemChanged: (DataItem) -> Unit,
    onClickStart: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val iconResource = if (expanded) {
        Icons.Rounded.ArrowDropUp
    } else {
        Icons.Rounded.ArrowDropDown
    }
    val timeTypeName: String =
        when (dataItem.unit) {
            0 -> "sec"
            1 -> "min"
            2 -> "hr"
            else -> "unit"
        }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            label = { Text("Activity") },
            modifier = Modifier
                .weight(weight = 0.5F)
                .padding(end = 8.dp, bottom = 8.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
            singleLine = true,
            value = dataItem.activity,
            onValueChange = { newValue ->
                onDataItemChanged(dataItem.copy(activity = newValue))
            }
        )
        OutlinedTextField(
            label = { Text("Time") },
            modifier = Modifier
                .weight(weight = 0.25F)
                .padding(bottom = 8.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            value = if (dataItem.time == 0) "" else dataItem.time.toString(),
            onValueChange = { newValue ->
                if (newValue.isEmpty()) {
                    onDataItemChanged(dataItem.copy(time = 0))
                } else {
                    newValue.toIntOrNull()?.let {
                        onDataItemChanged(dataItem.copy(time = it))
                    }
                }
            }
        )
        Box(
            modifier = Modifier
                //.weight(weight = 0.14F)
                .clickable(onClick = { expanded = true })
        ) {
            Row(
                modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = timeTypeName,
                    textAlign = TextAlign.Center
                )
                Icon(
                    modifier = Modifier.padding(start = 1.dp, top = 3.dp),
                    imageVector = iconResource,
                    contentDescription = "time increment selector"
                )
            }
            DropdownMenu(
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(onClick = {
                    expanded = false
                    onDataItemChanged(dataItem.copy(unit = 0))
                }) {
                    Text(text = "seconds")
                }
                DropdownMenuItem(onClick = {
                    expanded = false
                    onDataItemChanged(dataItem.copy(unit = 1))
                }) {
                    Text(text = "minutes")
                }
                DropdownMenuItem(onClick = {
                    expanded = false
                    onDataItemChanged(dataItem.copy(unit = 2))
                }) {
                    Text(text = "hours")
                }
            }
        }
        IconButton(
            //modifier = Modifier.weight(weight = 0.1F),
            onClick = onClickStart
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                tint = Color.Green,
                contentDescription = "Play"
            )
        }
    }
}

@Composable
fun DescriptionItemUI(note: NoteItem, onDescriptionClick: () -> Unit) {
    val dateFormatter = rememberSaveable { SimpleDateFormat.getDateTimeInstance() }
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDescriptionClick() },
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 4,
                text = if (note.description.isNotEmpty()) note.description else "Description"
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
                style = MaterialTheme.typography.bodyMedium,
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
    DataItemUI(dataItem, {}, {})
}