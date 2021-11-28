package com.augustbyrne.tas.ui.notes

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.augustbyrne.tas.data.db.entities.DataItem
import com.augustbyrne.tas.data.db.entities.NoteItem
import com.augustbyrne.tas.data.db.entities.NoteWithItems
import com.augustbyrne.tas.data.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repo: NoteRepository
): ViewModel() {

    // returns noteId. Be aware this does not update nicely with one to many relations
    suspend fun upsert(item: NoteItem?): Long {
        return if (item != null) {
            repo.upsert(item)
        } else {
            0
        }
    }

    suspend fun updateNote(item: NoteItem) {
        repo.updateNote(item)
    }

    suspend fun upsertDataItem(item: DataItem) {
        repo.upsertDataItem(item)
    }

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
        repo.cascadeDeleteNote(id)
    }

    fun sortedAllNotesWithItems(sortType: SortType): LiveData<List<NoteWithItems>> {
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

    var initialDialogDataItem: DataItem? by mutableStateOf(null)
    var tempSavedNote: NoteWithItems? = null
    var openSortPopup by mutableStateOf(false)
    var openEditDialog by mutableStateOf(EditDialogType.DialogClosed)

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
