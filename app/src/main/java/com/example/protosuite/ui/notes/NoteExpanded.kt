package com.example.protosuite.ui.notes

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.protosuite.data.db.entities.DataItem
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.ui.timer.NoteBroadcastReceiver
import com.example.protosuite.ui.timer.TimerService
import com.example.protosuite.ui.values.blue100
import com.example.protosuite.ui.values.blue500
import com.example.protosuite.ui.values.yellow100
import com.example.protosuite.ui.values.yellow200
import java.util.*
import kotlin.math.pow

// about 150 lines of compose code
// vs
// 250 (NoteExpanded.kt) - 50 (imports) + 100 (data_item.xml) + 50 (note_data.xml) + 50 (note_data_header.xml) + 200 (NoteDataAdapter.kt)
// = 600 for old fragment xml ui design
@SuppressLint("UnrememberedMutableState")
@Composable
fun ExpandedNoteUI (noteId: Int, myViewModel: NoteViewModel, onNavigateTimerStart: () -> Unit, onDeleteNote: () -> Unit, onNavBack: () -> Unit) {
    val context = LocalContext.current
    Log.d("noteId.toString()",noteId.toString())
    val noteWithItems by myViewModel.getNoteWithItemsById(noteId).observeAsState()
    val listState = rememberLazyListState()
    if (!myViewModel.beginTyping) {
        myViewModel.currentNote = noteWithItems?.note ?: NoteItem(0, null, null, 0, "", "")
        myViewModel.currentNoteItems = noteWithItems?.dataItems?.toMutableStateList() ?: mutableStateListOf()
    }
    DisposableEffect(key1 = myViewModel) {
        onDispose {
            Log.d("Side Effect Tracker", "navigating away from ExpandedNoteUI")
            myViewModel.apply {
                if (beginTyping) {
                    if (currentNote.title.isEmpty() && currentNote.description.isEmpty() && currentNoteItems.isNullOrEmpty()) {
                        if (currentNote.id != 0) {
                            deleteNote(currentNote.id)
                        }
                    } else {
                        upsertNoteAndData(
                            currentNote.copy(
                                //title = currentNoteTitle,
                                //description = currentNoteDescription,
                                last_edited_on = Calendar.getInstance(),
                                creation_date = currentNote.creation_date ?: Calendar.getInstance()
                            ),
                            currentNoteItems.toMutableList()
                        )
                        beginTyping = false
                    }
                    //currentNote = NoteItem(0, null, null, 0, "", "")
                    //currentNoteItems.clear()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(yellow100)) {
            TopAppBar(
                title = {
                    BasicTextField(
                        modifier = Modifier
                            .background(blue100, MaterialTheme.shapes.small)
                            .fillMaxWidth(0.9F)
                            .border(
                                border = BorderStroke(
                                    0.5.dp,
                                    color = Color.Black
                                ),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        textStyle = MaterialTheme.typography.subtitle1,
                        value = myViewModel.currentNote.title,
                        onValueChange = { newValue ->
                            myViewModel.beginTyping = true
                            myViewModel.currentNote =
                                myViewModel.currentNote.copy(title = newValue)
                        },
                        //placeholder = { Text("Title", color = Color.White) },
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (myViewModel.currentNote.title.isEmpty()) {
                                Text(
                                    text = "Title",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.subtitle1
                                )
                            }
                            innerTextField()
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavBack
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        content = {
                            DropdownMenuItem(onClick = onDeleteNote) {
                                Text("Delete")
                            }
                        }
                    )
                }
            )

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(yellow200)
                            .padding(8.dp),
                        textStyle = MaterialTheme.typography.body1,
                        maxLines = 4,
                        value = myViewModel.currentNote.description,
                        onValueChange = { newValue ->
                            myViewModel.beginTyping = true
                            myViewModel.currentNote =
                                myViewModel.currentNote.copy(description = newValue)
                        },
                        decorationBox = { innerTextField ->
                            if (myViewModel.currentNote.description.isEmpty()) {
                                Text(
                                    text = "Description",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.body1
                                )
                            }
                            innerTextField()
                        }
                    )
                    Text(
                        text = "last edited: ${
                            if (myViewModel.currentNote.last_edited_on?.time != null) {
                                myViewModel.simpleDateFormat.format(myViewModel.currentNote.last_edited_on!!.time)
                            } else {
                                "never"
                            }
                        }",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(yellow200)
                            .padding(4.dp),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.body2
                    )
                    Divider()
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
                            //myViewModel.startTimerWithIndex(index)
                            TimerService.initTimerService(
                                myViewModel.currentNote,
                                myViewModel.currentNoteItems,
                                index
                            )
                            // Disable for now TODO
                            //setNoteAlarm(
                            //    context,
                            //    myViewModel.currentNote,
                            //    myViewModel.currentNoteItems,
                            //    index
                            //)
                            onNavigateTimerStart()
                            Intent(context, TimerService::class.java).also {
                                it.action = "ACTION_START_OR_RESUME_SERVICE"
                                context.startService(it)
                            }
                        })
                    Divider(modifier = Modifier.padding(horizontal = 8.dp))
                }

                item {
                    Spacer(modifier = Modifier.size(80.dp))
                }
            }
/*            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

            }*/
        }
        FloatingActionButton(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomCenter),
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
            backgroundColor = blue500
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Item"
            )
        }
    }
}

@Composable
fun DataItemUI (
    dataItem: DataItem,
    onDataItemChanged: (DataItem) -> Unit,
    onClickStart: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val iconResource = if (expanded) {
        Icons.Default.ArrowDropUp
    } else {
        Icons.Default.ArrowDropDown
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
            .background(yellow100)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedTextField(
            label = { Text("Activity") },
            modifier = Modifier
                .weight(weight = 0.5F)
                .padding(end = 8.dp, bottom = 8.dp),
            textStyle = MaterialTheme.typography.body1,
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
            textStyle = MaterialTheme.typography.body1,
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
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
                imageVector = Icons.Default.PlayArrow,
                tint = Color.Green,
                contentDescription = "Play"
            )
        }
    }
}


fun setNoteAlarm(context: Context, note: NoteItem, items: MutableList<DataItem>, itemIndex: Int = 0) {
    val secondsRemaining = items[itemIndex].time * 60F.pow(items[itemIndex].unit).toLong()
    val wakeUpTime = Calendar.getInstance().timeInMillis + (secondsRemaining * 1000)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NoteBroadcastReceiver::class.java)
        .apply {
            putExtra("com.example.protosuite.NoteName", note.title)
            putExtra("com.example.protosuite.NoteId", note.id)
            putExtra(
                "com.example.protosuite.ItemNameList",
                items.map { it.activity }.toTypedArray()
            )
            putExtra(
                "com.example.protosuite.ItemTimeList",
                items.map { it.time.toLong() }.toLongArray()
            )
            putExtra(
                "com.example.protosuite.ItemTimeTypeList",
                items.map { it.unit }.toIntArray()
            )
            putExtra("com.example.protosuite.ItemListIndex", itemIndex)
        }
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, FLAG_UPDATE_CURRENT)
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(
        42,
    NoteBroadcastReceiver().notification(note.title, items[itemIndex].activity, wakeUpTime, context)
    )
    //PrefUtil.setAlarmEndTime(wakeUpTime, context)
}

@ExperimentalAnimationApi
fun removeNoteAlarm(context: Context) {
    val intent = Intent(context, NoteBroadcastReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
    //PrefUtil.setAlarmEndTime(0, context)
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