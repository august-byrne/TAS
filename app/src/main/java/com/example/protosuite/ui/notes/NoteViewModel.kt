package com.example.protosuite.ui.notes

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.protosuite.data.db.entities.DataItem
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.data.db.entities.NoteWithItems
import com.example.protosuite.data.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repo: NoteRepository
): ViewModel() {

    /*
    var noteDataId: Long = 0
    fun upsert(item: NoteItem?) = CoroutineScope(Dispatchers.Main).launch {
        if (item != null) {
            noteDataId = repo.upsert(item)
        } else {
            Log.d("DB Interactions", "upsert failed: item was null")
        }
    }

    fun upsertData(items: List<DataItem>) = CoroutineScope(Dispatchers.Main).launch {
        repo.upsertData(items)
    }
    */

    fun upsertNoteAndData(noteItem: NoteItem, dataItems: MutableList<DataItem>) =
        CoroutineScope(Dispatchers.Main).launch {
            if (noteItem.id == 0) {
                val noteDataId = repo.upsert(noteItem)
                dataItems.replaceAll { dataItem ->
                    dataItem.copy(
                        id = dataItem.id,
                        parent_id = noteDataId.toInt(),
                        activity = dataItem.activity,
                        order = dataItems.lastIndex - dataItems.indexOf(dataItem),
                        time = dataItem.time
                    )
                }
                repo.upsertData(dataItems)
            } else {
                repo.upsert(noteItem)
                repo.upsertData(dataItems)
            }
        }

    fun deleteNote(id: Int) = CoroutineScope(Dispatchers.Main).launch {
        repo.deleteNote(id)
    }

    fun updateNoteItemOrderInDatabase(noteListCopy: MutableList<NoteItem>) =
        CoroutineScope(Dispatchers.Main).launch {
            noteListCopy.replaceAll { noteItem ->
                noteItem.copy(
                    id = noteItem.id,
                    creation_date = noteItem.creation_date,
                    last_edited_on = noteItem.last_edited_on,
                    order = noteListCopy.lastIndex - noteListCopy.indexOf(noteItem),
                    title = noteItem.title,
                    description = noteItem.description
                )
            }
            repo.updateNoteItems(noteListCopy)
        }

    //var allNotes: LiveData<List<NoteItem>> = repo.allNotes.asLiveData()

    var allNotesWithItems: LiveData<List<NoteWithItems>> = repo.allNotesWithItems.asLiveData()

    fun sortedAllNotesWithItems(): LiveData<List<NoteWithItems>> {
        return repo.allNotesWithItems.map { list ->
            when (sortType) {
                SortType.Creation -> {
                    list.sortedByDescending { it.note.creation_date }
                }
                SortType.LastEdited -> {
                    list.sortedByDescending { it.note.last_edited_on }
                }
                SortType.Order -> {
                    list.sortedByDescending { it.note.order }
                }
                SortType.Default -> {
                    list
                }
            }
        }.asLiveData()
    }

    fun getNoteWithItemsById(id: Int): LiveData<NoteWithItems> =
        repo.getNoteWithItemsById(id).asLiveData()

    var beginTyping by mutableStateOf(false)
    var currentNote by mutableStateOf(NoteItem(0, null, null, 0, "", ""))
    var currentNoteItems = mutableStateListOf<DataItem>()
    var tempSavedNote: NoteWithItems? = null

    var openSortPopup by mutableStateOf(false)
    var sortType by mutableStateOf(SortType.Default)

    val simpleDateFormat: DateFormat = SimpleDateFormat.getDateInstance()

    private var _prevTimeType = 0
    val prevTimeType: Int
        get() = _prevTimeType

    fun setPrevTimeType(timeType: Int) {
        _prevTimeType = if (timeType in 0..2) {
            timeType
        } else {
            0
        }
    }

    private var listState: LazyListState = LazyListState()
    fun saveListPosition(newListState: LazyListState) {
        listState = newListState
    }

    fun loadListPosition(): LazyListState {
        return listState
    }
}
