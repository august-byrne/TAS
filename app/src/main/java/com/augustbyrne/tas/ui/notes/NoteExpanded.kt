package com.augustbyrne.tas.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.augustbyrne.tas.data.db.entities.DataItem
import com.augustbyrne.tas.data.db.entities.NoteItem
import com.augustbyrne.tas.data.db.entities.NoteWithItems
import com.augustbyrne.tas.ui.components.EditDataItemDialog
import com.augustbyrne.tas.ui.components.EditExpandedNoteHeaderDialog
import com.augustbyrne.tas.ui.timer.TimerService
import com.augustbyrne.tas.ui.values.AppTheme
import com.augustbyrne.tas.util.BarType
import com.augustbyrne.tas.util.TimerState
import com.augustbyrne.tas.util.classicSystemBarScrollBehavior
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.pow

private val myDateTimeFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedNoteUI (
    noteId: Int,
    myViewModel: NoteViewModel,
    topAppBarState: TopAppBarState,
    onNavigateTimerStart: (noteWithItems: NoteWithItems, index: Int) -> Unit,
    onDeleteNote: (noteWithItems: NoteWithItems) -> Unit,
    onCloneNote: (noteWithItems: NoteWithItems) -> Unit,
    onNavBack: (noteWithItems: NoteWithItems) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val noteWithItems by myViewModel.getNoteWithItemsById(noteId)
        .observeAsState(initial = NoteWithItems(NoteItem(), listOf()))
    var draggableNotes by remember { mutableStateOf(noteWithItems.dataItems) }
    val prevTimeType by myViewModel.lastUsedTimeUnitLiveData.observeAsState(initial = 0)
    val timerState: TimerState by TimerService.timerState.observeAsState(TimerState.Stopped)
    var noteInfoToggle by rememberSaveable { mutableStateOf(true) }
    val fabPadding: Float by myViewModel.miniTimerPadding.observeAsState(0f)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    val state = rememberReorderableLazyListState(
        canDragOver = { to, _ ->
            to.index in 2..noteWithItems.dataItems.lastIndex + 2
        },
        onMove = { from, to ->
            draggableNotes = draggableNotes.toMutableList().apply {
                add(to.index - 2, removeAt(from.index - 2))
            }
        }, onDragEnd = { from, to ->
            if (from >= 0 && to >= 0) {
                myViewModel.upsertNoteAndData(
                    noteWithItems.note,
                    draggableNotes.toMutableList()
                )
            }
        }
    )

    val totalTime = noteWithItems.dataItems.sumOf {
        it.time * 60f.pow(
            it.unit
        ).toInt()
    }

    LaunchedEffect(Unit) {
        topAppBarState.heightOffset = 0f
    }

    LaunchedEffect(noteWithItems.dataItems) {
        if (noteWithItems.dataItems.isNotEmpty()) {
            draggableNotes = noteWithItems.dataItems
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            // attach as a parent to the nested scroll system
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    text = noteWithItems.note.title.ifEmpty { "Add title" }
                )
            },
                modifier = Modifier
                    .statusBarsPadding()
                    .classicSystemBarScrollBehavior(topAppBarState, BarType.Top),
                navigationIcon = {
                    IconButton(
                        onClick = { onNavBack(noteWithItems) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
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
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        content = {
                            DropdownMenuItem(
                                text = { Text("Clone note") },
                                onClick = {
                                    expanded = false
                                    onCloneNote(noteWithItems)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    expanded = false
                                    onDeleteNote(noteWithItems)
                                }
                            )
                        }
                    )
                }
            )
        }
    ) { statusBarsPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(top = statusBarsPadding.calculateTopPadding())
                .fillMaxSize()
                .reorderable(state),
            state = state.listState,
            contentPadding = PaddingValues(bottom = if (timerState != TimerState.Stopped) 170.dp else 88.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                        )
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
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
                            onNavigateTimerStart(
                                NoteWithItems(
                                    noteWithItems.note,
                                    noteWithItems.dataItems.shuffled()
                                ), 0
                            )
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
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(top = 16.dp, end = 16.dp),
                            textAlign = TextAlign.End,

                            text =
                                if(totalTime < 60) {
                                    "total time: $totalTime sec"
                                } else if (totalTime < 3600) {
                                    "total time: ${"%.2f".format(totalTime/60f).toFloat()} min"
                                } else {
                                    "total time: ${"%.2f".format(totalTime/3600f).toFloat()} hr"
                                }
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "${noteWithItems.dataItems.size} item" +
                                if (noteWithItems.dataItems.size != 1) {
                                    "s"
                                } else {
                                    ""
                                }
                    )
                    Text(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { noteInfoToggle = !noteInfoToggle }
                            .padding(8.dp),
                        text = if (noteInfoToggle && noteWithItems.note.last_edited_on != null) {
                            noteWithItems.note.last_edited_on?.let { dateTime ->
                                "last edited: ${dateTime.format(myDateTimeFormat)}"
                            } ?: ""
                        } else {
                            noteWithItems.note.creation_date?.let { dateTime ->
                                "created: ${dateTime.format(myDateTimeFormat)}"
                            } ?: ""
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            items(
                items = draggableNotes,
                key = { it.id }
            ) { item ->
                ReorderableItem(reorderableState = state, key = item.id) {
                    DataItemUI(
                        dataItem = item,
                        state = state,
                        onClickToEdit = { myViewModel.initialDialogDataItem = item },
                        onClickCloneItem = {
                            coroutineScope.launch {
                                myViewModel.upsertDataItem(item.copy(id = 0, order = 0))
                                myViewModel.updateNote(
                                    noteWithItems.note.copy(
                                        last_edited_on = LocalDateTime.now()
                                    )
                                )
                            }
                        },
                        onClickStartFromHere = {
                            onNavigateTimerStart(noteWithItems, draggableNotes.indexOf(item))
                        },
                        onClickDelete = {
                            coroutineScope.launch {
                                myViewModel.deleteDataItem(item.id)
                                myViewModel.updateNote(noteWithItems.note.copy(last_edited_on = LocalDateTime.now()))
                            }
                        }
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(vertical = 8.dp)
                        .clip(CircleShape)
                        .clickable {
                            myViewModel.initialDialogDataItem = DataItem(
                                parent_id = noteId,
                                unit = prevTimeType
                            )
                        }
                        .border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.padding(8.dp),
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "New item"
                    )
                    Text(text = "Add item")
                    Spacer(Modifier.padding(horizontal = 8.dp))
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
                onClick = { onNavigateTimerStart(noteWithItems, 0) },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play note"
                )
            }
        }
    }
}

@Composable
fun DataItemUI (
    modifier: Modifier = Modifier,
    dataItem: DataItem,
    state: ReorderableLazyListState,
    onClickToEdit: () -> Unit,
    onClickCloneItem: () -> Unit,
    onClickStartFromHere: () -> Unit,
    onClickDelete: () -> Unit
) {
    var itemExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
            .clickable(
                onClick = { onClickToEdit() },
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .detectReorder(state)
                .padding(8.dp),
            imageVector = Icons.Rounded.DragHandle,
            contentDescription = "drag and drop icon"
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight()
        ) {
            Text(
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                text = dataItem.activity
            )
        }
        Spacer(modifier = Modifier.padding(horizontal = 16.dp))
        Text(
            modifier = Modifier
                .wrapContentSize(),
            text = dataItem.time.toString() +
                    when (dataItem.unit) {
                        0 -> {
                            " sec"
                        }
                        1 -> {
                            " min"
                        }
                        2 -> {
                            " hr"
                        }
                        else -> {
                            " unit"
                        }
                    }
        )
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Box(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Top),
        ) {
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
                        text = { Text("Clone item") },
                        onClick = {
                            itemExpanded = false
                            onClickCloneItem()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Start from here") },
                        onClick = {
                            itemExpanded = false
                            onClickStartFromHere()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete item") },
                        onClick = {
                            itemExpanded = false
                            onClickDelete()
                        }
                    )
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
        DataItemUI(Modifier, dataItem, rememberReorderableLazyListState(onMove = { to, from ->

        }), {}, {}, {}, {})
    }
}