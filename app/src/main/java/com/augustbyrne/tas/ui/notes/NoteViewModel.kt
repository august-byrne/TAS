package com.augustbyrne.tas.ui.notes

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.augustbyrne.tas.data.PreferenceManager
import com.augustbyrne.tas.data.db.entities.DataItem
import com.augustbyrne.tas.data.db.entities.NoteItem
import com.augustbyrne.tas.data.db.entities.NoteWithItems
import com.augustbyrne.tas.data.repositories.NoteRepository
import com.augustbyrne.tas.util.DarkMode
import com.augustbyrne.tas.util.SortType
import com.augustbyrne.tas.util.TimerTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repo: NoteRepository,
    private val preferences: PreferenceManager
): ViewModel() {

    /**
     * Room Local Database Here, LiveData for view access, since Flow gives me lifecycle bugs
     */
    // returns noteId. Be aware this does not update nicely with one to many relations
    suspend fun upsert(item: NoteItem): Long = repo.upsert(item)

    suspend fun updateNote(item: NoteItem) = repo.updateNote(item)

    suspend fun upsertDataItem(item: DataItem) = repo.upsertDataItem(item)

    fun upsertNoteAndData(noteItem: NoteItem, dataItems: List<DataItem>) =
        viewModelScope.launch {
            val parentId = if (noteItem.id == 0) repo.upsert(noteItem).toInt() else {
                repo.upsert(noteItem)
                noteItem.id
            }
            val lastIndex = dataItems.lastIndex
            val ordered = dataItems.mapIndexed { idx, item ->
                item.copy(parent_id = parentId, order = lastIndex - idx)
            }
            repo.upsertData(ordered)
        }

    fun updateAllNotes(items: List<NoteItem>) =
        viewModelScope.launch {
            val lastIndex = items.lastIndex
            val ordered = items.mapIndexed { idx, item ->
                item.copy(order = lastIndex - idx)
            }
            repo.updateNotes(ordered)
        }

    fun deleteNote(id: Int) = viewModelScope.launch {
        repo.cascadeDeleteNote(id)
    }

    suspend fun deleteDataItem(id: Int) = repo.deleteDataItem(id)

    fun sortedAllNotes(sortType: SortType?): LiveData<List<NoteItem>?> =
        repo.allNotes.map { list ->
            when (sortType) {
                SortType.Creation -> {
                    list.sortedByDescending { it.creation_date }
                }
                SortType.LastEdited -> {
                    list.sortedByDescending { it.last_edited_on }
                }
                SortType.Order -> {
                    list.sortedByDescending { it.order }
                }
                SortType.Default -> {
                    list
                }
                else -> {
                    null
                }
            }
        }.asLiveData()

    fun getNoteWithItemsById(id: Int): LiveData<NoteWithItems> =
        repo.getNoteWithItemsById(id).map { value: NoteWithItems? ->
            if (value != null) {
                NoteWithItems(value.note, value.dataItems.sortedByDescending { it.order })
            } else {
                NoteWithItems(NoteItem(), listOf())
            }
        }.asLiveData()

    suspend fun getStaticNoteWithItemsById(id: Int): NoteWithItems =
        repo.getNoteWithItemsByIdSynchronous(id)?.run {
            NoteWithItems(
                note = note,
                dataItems = dataItems.sortedByDescending { it.order }
            )
        } ?: NoteWithItems(NoteItem(), listOf())

    suspend fun getNumberOfNotes(): Int = repo.getNumberOfNotes()

    /**
     * PreferenceManager DataStore Preferences Here
     */
    val isDarkThemeLiveData: LiveData<DarkMode> =
        preferences.isDarkThemeFlow.map { DarkMode.getMode(it) }.asLiveData()

    suspend fun setIsDarkTheme(value: DarkMode) = preferences.setIsDarkTheme(value.mode)

    val timerThemeLiveData: LiveData<TimerTheme> =
        preferences.timerThemeFlow.map { TimerTheme.getTheme(it) }.asLiveData()

    suspend fun setTimerTheme(value: TimerTheme) = preferences.setTimerTheme(value.theme)

    val sortTypeLiveData: LiveData<SortType> =
        preferences.sortTypeFlow.map { SortType.getType(it) }.asLiveData()

    suspend fun setSortType(value: SortType) = preferences.setSortType(value.type)

    val lastUsedTimeUnitLiveData: LiveData<Int> = preferences.lastUsedTimeUnitFlow.asLiveData()
    suspend fun setLastUsedTimeUnit(value: Int) = preferences.setLastUsedTimeUnit(value)

    val vibrationLiveData: LiveData<Boolean> = preferences.vibrationFlow.asLiveData()
    suspend fun setVibration(value: Boolean) = preferences.setVibration(value)

    val startDelayPrefLiveData: LiveData<Int> = preferences.startDelay.asLiveData()
    suspend fun setStartDelayPref(value: Int) = preferences.setStartDelay(value)

    /**
     * Other Cross-Composable Variables Here
     */
    var initialDialogDataItem: DataItem? by mutableStateOf(null)
    var tempSavedNote: NoteWithItems? = null
    var openSortPopup by mutableStateOf(false)
    var openEditDialog by mutableStateOf(false)

    private var savedListIndex: Int = 0
    private var savedListOffset: Int = 0

    fun saveListPosition(state: LazyListState) {
        savedListIndex = state.firstVisibleItemIndex
        savedListOffset = state.firstVisibleItemScrollOffset
    }

    fun resetListPosition() {
        savedListIndex = 0
        savedListOffset = 0
    }

    fun listPositionIndex(): Int = savedListIndex
    fun listPositionOffset(): Int = savedListOffset

    var miniTimerPadding: MutableLiveData<Float> = MutableLiveData(0f)
    fun updateFabPadding(miniTimerHeight: Float, miniTimerOffset: Float) {
        miniTimerPadding.value = (miniTimerHeight + miniTimerOffset).coerceAtLeast(0f)
    }
}
