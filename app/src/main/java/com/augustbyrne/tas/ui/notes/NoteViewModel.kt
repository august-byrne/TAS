package com.augustbyrne.tas.ui.notes

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.augustbyrne.tas.data.PreferenceManager
import com.augustbyrne.tas.data.db.entities.DataItem
import com.augustbyrne.tas.data.db.entities.NoteItem
import com.augustbyrne.tas.data.db.entities.NoteWithItems
import com.augustbyrne.tas.data.repositories.NoteRepository
import com.augustbyrne.tas.util.DarkMode
import com.augustbyrne.tas.util.SortType
import com.augustbyrne.tas.util.TimerTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    fun upsertNoteAndData(noteItem: NoteItem, dataItems: MutableList<DataItem>) =
        CoroutineScope(Dispatchers.Main).launch {
            if (noteItem.id == 0) {
                val noteDataId = repo.upsert(noteItem).toInt()
                dataItems.replaceAll {
                    it.copy(
                        parent_id = noteDataId,
                        order = dataItems.lastIndex - dataItems.indexOf(it)
                    )
                }
            } else {
                repo.upsert(noteItem)
                dataItems.replaceAll {
                    it.copy(
                        order = dataItems.lastIndex - dataItems.indexOf(it)
                    )
                }
            }
            repo.upsertData(dataItems)
        }

    fun updateAllNotes(items: MutableList<NoteItem>) =
        CoroutineScope(Dispatchers.Main).launch {
            items.replaceAll {
                it.copy(
                    order = items.lastIndex - items.indexOf(it)
                )
            }
            repo.updateNotes(items)
        }

    fun deleteNote(id: Int) = CoroutineScope(Dispatchers.Main).launch {
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
        repo.getNoteWithItemsByIdSynchronous(id).run {
            NoteWithItems(
                note = note,
                dataItems = dataItems.sortedByDescending { it.order }
            )
        }

    suspend fun getNumberOfNotes(): Int = repo.getNumberOfNotes()

    /**
     * PreferenceManager DataStore Preferences Here
     */
    val showAdsLiveData: LiveData<Boolean> = preferences.showAdsFlow.asLiveData()
    suspend fun setShowAds(value: Boolean) = preferences.setShowAds(value)

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

    private var listState: LazyListState = LazyListState()
    fun saveListPosition(newListState: LazyListState) {
        listState = newListState
    }

    fun loadListPosition(): LazyListState = listState
}
