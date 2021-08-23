package com.example.protosuite.ui.notes

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.ui.SortPopupUI
import com.example.protosuite.ui.values.blue500
import com.example.protosuite.ui.values.yellow100
import com.example.protosuite.ui.values.yellow50
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NoteListUI(myViewModel: NoteViewModel, onNavigate: (noteId: Int) -> Unit, onNavigateStart: (noteId: Int) -> Unit) {
    val noteList: List<NoteItem> by myViewModel.allNotes.observeAsState(listOf())
    //val noteList: List<NoteItem> by myViewModel.noteListFlow.collectAsState(listOf())
    Log.d("Flow vs LiveData","Recomp ${myViewModel.run { recompCounter++ }}")
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val notes = when(myViewModel.sortType) {
        1 -> {
            noteList.sortedByDescending { it.creation_date }
        }
        2 -> {
            noteList.sortedByDescending { it.last_edited_on }
        }
        3 -> {
            noteList.sortedByDescending { it.order }
        }
        else -> {
            noteList
        }
    }
    SortPopupUI(myViewModel)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(yellow50),
        state = listState,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        coroutineScope.launch {
            listState.scrollToItem(
                myViewModel.noteListScrollIndex,
                myViewModel.noteListScrollOffset
            )
        }
        items(notes) { note ->
            NoteItemUI(
                note,
                {
                    myViewModel.noteListScrollIndex = listState.firstVisibleItemIndex
                    myViewModel.noteListScrollOffset = listState.firstVisibleItemScrollOffset
                    onNavigate(note.id)
                },
                {
                    //myViewModel.setActiveDataAndStartTimer(
                    //    myViewModel.currentNote,
                    //    myViewModel.currentNoteItems
                    //)
                    onNavigateStart(note.id)
                    // Disable for now TODO
                    //setNoteAlarm(
                    //    context,
                    //    myViewModel.currentNote,
                    //    myViewModel.currentNoteItems
                    //)
                }
            )
        }
        item {
            Spacer(modifier = Modifier.size(80.dp))
        }
    }
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        FloatingActionButton(
            onClick = {
                onNavigate(0)
            },
            shape = RoundedCornerShape(
                topStart = 16.dp
            ),
            backgroundColor = blue500
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Note"
            )
        }
    }
}

@Composable
fun NoteItemUI (
    note: NoteItem,
    onClick: () -> Unit,
    onClickStart: () -> Unit
    ) {
    val simpleDateFormat = remember { SimpleDateFormat.getDateInstance() }
    val creationDate = simpleDateFormat.format(note.creation_date!!.time)
    Card(
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 4.dp,
        backgroundColor = yellow100
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
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
                        .fillMaxWidth(0.76F),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.h6,
                        text = if (note.title.isNotBlank()) {
                            note.title
                        } else {
                            "Title"
                        }
                    )
                    Text(
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.body1,
                        text = creationDate
                    )
                }
                TextButton(
                    modifier = Modifier.padding(start = 4.dp),
                    border = BorderStroke(1.dp, Color.Green),
                    shape = MaterialTheme.shapes.small,
                    onClick = onClickStart
                ) {
                    Text(
                        color = Color.Green,
                        text = "Start"
                    )
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        tint = Color.Green,
                        contentDescription = "Play"
                    )
                }
            }
            Text(
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.body2,
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
    NoteItemUI(note,{},{})
}